#!/usr/bin/env python3
import sqlite3
import shutil
import requests
import re
import time

DB_PATH = 'app/src/main/assets/database/censozepa.db'

def fetch_wiki_robust(sci_name):
    url = 'https://es.wikipedia.org/w/api.php'

    titles_to_try = [sci_name]
    parts = sci_name.split()
    if len(parts) > 2:
        titles_to_try.append(f'{parts[0]} {parts[1]}')

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
        headers = {'User-Agent': 'CensoZepaBot/1.0'}
        try:
            resp = requests.get(url, params=params, headers=headers, timeout=5)
            data = resp.json()
            pages = data.get('query', {}).get('pages', {})
            for page_id, page_info in pages.items():
                if page_id != '-1':
                    extract = page_info.get('extract', '')
                    match = re.search(r'(?:El|La|Los|Las)\s+([^\(\.]+)', extract)
                    if match:
                        name = match.group(1).strip()
                        name = re.split(r'\b[Oo]\b|,', name)[0].strip()
                        if len(name) > 2 and len(name) < 40:
                            return name.capitalize()
        except Exception:
            pass

    # Fallback to search API
    search_url = 'https://es.wikipedia.org/w/api.php'
    search_params = {
        'action': 'query',
        'list': 'search',
        'srsearch': sci_name,
        'format': 'json'
    }
    try:
        resp = requests.get(search_url, params=search_params, headers={'User-Agent': 'CensoZepaBot/1.0'}, timeout=5).json()
        results = resp.get('query', {}).get('search', [])
        if results:
            snippet = results[0].get('snippet', '')
            clean_snippet = re.sub(r'<[^>]+>', '', snippet)
            match = re.search(r'(?:El|La|Los|Las)\s+([^\(\.]+)', clean_snippet)
            if match:
                name = match.group(1).strip()
                name = re.split(r'\b[Oo]\b|,', name)[0].strip()
                if len(name) > 2 and len(name) < 40:
                    return name.capitalize()
    except Exception:
        pass

    return None

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Enriching common names for {len(species)} species using Wikipedia...")

    updated = 0
    for i, (code, sci_name, current_common) in enumerate(species, 1):
        common = fetch_wiki_robust(sci_name)
        if common and common.lower() != sci_name.lower():
            cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))
            updated += 1
        else:
            cursor.execute("UPDATE especie SET nombre_comun = NULL WHERE codigo_n2000 = ?", (code,))

        if i % 50 == 0:
            print(f"Processed {i}/{len(species)} species...")
        time.sleep(0.1)

    conn.commit()
    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Species database fully enriched. Updated {updated} common names out of {len(species)}.")

if __name__ == '__main__':
    main()
