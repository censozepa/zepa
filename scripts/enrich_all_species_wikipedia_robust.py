#!/usr/bin/env python3
import sqlite3
import shutil
import requests
import re
import time

DB_PATH = 'app/src/main/assets/database/censozepa.db'

def get_spanish_common_name_wiki(sci_name):
    url = 'https://es.wikipedia.org/w/api.php'
    titles_to_try = [sci_name]
    parts = sci_name.split()
    if len(parts) > 2:
        titles_to_try.append(f"{parts[0]} {parts[1]}")

    for title in titles_to_try:
        params = {
            'action': 'query',
            'prop': 'extracts',
            'exintro': True,
            'explaintext': True,
            'redirects': 1,
            'titles': title,
            'format': 'json'
        }
        headers = {'User-Agent': 'CensoZepaEnricher/1.0'}
        try:
            resp = requests.get(url, params=params, headers=headers, timeout=5)
            data = resp.json()
            pages = data.get('query', {}).get('pages', {})
            for page_id, page_info in pages.items():
                if page_id != '-1':
                    extract = page_info.get('extract', '')
                    clean = re.sub(r'[\u200b\u200c\u200d\ufeff]', '', extract)
                    match = re.search(r'(?:El|La|Los|Las)\s+([^\(\.]+)', clean)
                    if match:
                        name = match.group(1).strip()
                        name = re.split(r'\b[Oo]\b|,', name)[0].strip()
                        if len(name) > 2 and len(name) < 45 and name.lower() != sci_name.lower():
                            return name.capitalize()
        except Exception:
            pass

    return None

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Total species in DB: {len(species)}")

    updated = 0
    for i, (code, sci_name, current_common) in enumerate(species, 1):
        if not current_common or current_common == sci_name:
            common = get_spanish_common_name_wiki(sci_name)
            if common:
                cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))
                print(f"[{i}/{len(species)}] Enriched '{sci_name}' -> '{common}'")
                updated += 1
        time.sleep(0.08)

    conn.commit()
    remaining = cursor.execute("SELECT COUNT(*) FROM especie WHERE nombre_comun IS NULL OR nombre_comun = '' OR nombre_comun = nombre_cientifico").fetchone()[0]
    print(f"\nEnrichment complete. Updated {updated} species. Remaining without common name: {remaining}")

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("Database updated successfully.")

if __name__ == '__main__':
    main()
