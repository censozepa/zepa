#!/usr/bin/env python3
import sqlite3
import os
import requests
import time
import shutil
import io
from PIL import Image, ImageDraw

DB_PATH = 'app/src/main/assets/database/censozepa.db'
IMAGES_DIR = 'app/src/main/assets/images'
SCRIPTS_IMAGES_DIR = 'scripts/bird_images'

def get_commons_bird_image_url(sci_name):
    parts = sci_name.split()
    binomial = f"{parts[0]} {parts[1]}" if len(parts) >= 2 else sci_name

    url = "https://commons.wikimedia.org/w/api.php"
    params = {
        'action': 'query',
        'generator': 'search',
        'gsrsearch': f"{binomial} filetype:bitmap",
        'gsrnamespace': '6',
        'prop': 'imageinfo',
        'iiprop': 'url',
        'iiurlwidth': 300,
        'format': 'json'
    }
    headers = {'User-Agent': 'CensoZepaApp/2.0 (https://github.com/censozepa; contact@censozepa.app) python-requests'}
    try:
        resp = requests.get(url, params=params, headers=headers, timeout=6)
        data = resp.json()
        pages = data.get('query', {}).get('pages', {})
        for page_id, page_info in pages.items():
            imageinfo = page_info.get('imageinfo', [])
            if imageinfo:
                img_url = imageinfo[0].get('thumburl') or imageinfo[0].get('url')
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

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Downloading real bird photographs from Wikimedia Commons for {len(species)} species...")

    downloaded = 0
    fallback_count = 0

    for i, (code, sci_name, common_name) in enumerate(species, 1):
        img_filename = f"{code}.jpg"
        local_path = os.path.join(IMAGES_DIR, img_filename)
        script_local_path = os.path.join(SCRIPTS_IMAGES_DIR, img_filename)

        img_url = get_commons_bird_image_url(sci_name)
        success = False
        if img_url:
            try:
                r = requests.get(img_url, headers={'User-Agent': 'CensoZepaApp/2.0 (contact@censozepa.app)'}, timeout=6)
                if r.status_code == 200:
                    img_io = io.BytesIO(r.content)
                    with Image.open(img_io) as im:
                        im.verify()
                    im = Image.open(io.BytesIO(r.content)).convert('RGB')
                    im = im.resize((300, 300), Image.Resampling.LANCZOS)
                    im.save(local_path, format='JPEG', quality=85)
                    shutil.copy(local_path, script_local_path)
                    downloaded += 1
                    success = True
                    print(f"[{i}/{len(species)}] Downloaded real photo for {sci_name} ({code})")
            except Exception:
                success = False

        if not success:
            im = Image.new('RGB', (200, 200), color=(27, 77, 62))
            draw = ImageDraw.Draw(im)
            draw.ellipse([15, 15, 185, 185], outline=(129, 199, 132), width=5)
            im.save(local_path, format='JPEG', quality=85)
            shutil.copy(local_path, script_local_path)
            fallback_count += 1
            print(f"[{i}/{len(species)}] Fallback badge for {sci_name} ({code})")

        time.sleep(0.06)

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Done. Successfully downloaded {downloaded} real bird photos, {fallback_count} fallbacks.")

if __name__ == '__main__':
    main()
