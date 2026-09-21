#!/usr/bin/env python3
import sqlite3
import os
import requests
import time
import shutil

DB_PATH = 'app/src/main/assets/database/censozepa.db'
IMAGES_DIR = 'app/src/main/assets/images'
SCRIPTS_IMAGES_DIR = 'scripts/bird_images'

def get_wiki_image_url(sci_name):
    url = 'https://es.wikipedia.org/w/api.php'
    titles_to_try = [sci_name]
    parts = sci_name.split()
    if len(parts) > 2:
        titles_to_try.append(f"{parts[0]} {parts[1]}")

    for title in titles_to_try:
        params = {
            'action': 'query',
            'prop': 'pageimages',
            'piprop': 'thumbnail',
            'pithumbsize': 200,
            'redirects': 1,
            'titles': title,
            'format': 'json'
        }
        headers = {'User-Agent': 'CensoZepaImageDownloader/1.0'}
        try:
            resp = requests.get(url, params=params, headers=headers, timeout=5)
            data = resp.json()
            pages = data.get('query', {}).get('pages', {})
            for page_id, page_info in pages.items():
                if page_id != '-1':
                    thumb = page_info.get('thumbnail', {})
                    img_url = thumb.get('source')
                    if img_url:
                        return img_url
        except Exception:
            pass
    return None

def main():
    os.makedirs(IMAGES_DIR, exist_ok=True)
    os.makedirs(SCRIPTS_IMAGES_DIR, exist_ok=True)

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    try:
        cursor.execute("ALTER TABLE especie ADD COLUMN foto_asset TEXT;")
    except Exception:
        pass

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico FROM especie").fetchall()
    print(f"Downloading images for {len(species)} bird species...")

    downloaded = 0
    for i, (code, sci_name) in enumerate(species, 1):
        img_filename = f"{code}.jpg"
        asset_path = f"images/{img_filename}"
        local_path = os.path.join(IMAGES_DIR, img_filename)
        script_local_path = os.path.join(SCRIPTS_IMAGES_DIR, img_filename)

        cursor.execute("UPDATE especie SET foto_asset = ? WHERE codigo_n2000 = ?", (asset_path, code))

        if os.path.exists(local_path) and os.path.getsize(local_path) > 1000:
            downloaded += 1
            continue

        img_url = get_wiki_image_url(sci_name)
        if img_url:
            try:
                img_data = requests.get(img_url, timeout=5).content
                with open(local_path, 'wb') as f:
                    f.write(img_data)
                shutil.copy(local_path, script_local_path)
                downloaded += 1
                print(f"[{i}/{len(species)}] Downloaded image for {sci_name} ({code})")
            except Exception as e:
                print(f"[{i}/{len(species)}] Failed to download image for {sci_name}: {e}")
        else:
            print(f"[{i}/{len(species)}] No wiki image found for {sci_name}")

        time.sleep(0.08)

    conn.commit()
    conn.close()

    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Image download complete. Successfully processed {downloaded}/{len(species)} species images.")

if __name__ == '__main__':
    main()
