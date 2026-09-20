#!/usr/bin/env python3
import sqlite3
import shutil
import requests
import re
import time

DB_PATH = 'app/src/main/assets/database/censozepa.db'

# Comprehensive mapping of common N2000 bird codes and scientific names to Spanish common names
KNOWN_COMMON_NAMES = {
    'A078': 'Águila imperial ibérica',
    'A080': 'Águila perdicera',
    'A233': 'Avutarda común',
    'A030': 'Cigüeña negra',
    'A087': 'Alimoche común',
    'A074': 'Milano real',
    'A110': 'Perdiz roja',
    'A212': 'Cuco común',
    'A096': 'Cernícalo vulgar',
    'A210': 'Tórtola europea',
    'A247': 'Alondra común',
    'A330': 'Carbonero común',
    'A329': 'Herrerillo común',
    'A359': 'Pinzón vulgar',
    'A364': 'Jilguero europeo',
    'A363': 'Verderón común',
    'A366': 'Pardillo común',
    'A311': 'Curruca capirotada',
    'A269': 'Petirrojo europeo',
    'A226': 'Vencejo común',
    'A283': 'Mirlo común',
    'A251': 'Golondrina común',
    'A031': 'Cigüeña blanca',
    'A232': 'Abubilla',
    'A075': 'Milano negro',
    'A072': 'Abejero europeo',
    'A079': 'Águila real',
    'A089': 'Águila culebrera',
    'A084': 'Aguilucho cenizo',
    'A082': 'Aguilucho pálido',
    'A081': 'Aguilucho lagunero occidental',
    'A099': 'Halcón peregrino',
    'A103': 'Halcón peregrino',
    'A128': 'Sisón común',
    'A133': 'Alcaraván común',
    'A224': 'Autillo europeo',
    'A222': 'Búho campestre',
    'A223': 'Búho chico',
    'A220': 'Búho real',
    'A213': 'Lechuza común',
    'A246': 'Totovía',
    'A307': 'Curruca tomillera',
    'A301': 'Curruca rabilarga',
    'A338': 'Alcaudón dorsirrojo',
    'A337': 'Oropéndola europea',
    'A272': 'Ruiseñor común',
    'A277': 'Collalba gris',
    'A276': 'Tarabilla común',
    'A315': 'Mosquitero común',
    'A312': 'Mosquitero ibérico',
    'A295': 'Carricerzal común',
    'A399': 'Elanio común',
    'A125': 'Focha común',
    'A123': 'Gallineta común',
    'A053': 'Ánade azulón',
    'A050': 'Ánade silbón',
    'A052': 'Cerceta común',
    'A038': 'Ansar común',
    'A197': 'Fumarel cariblanco',
    'A193': 'Charrán común',
    'A177': 'Gaviota reidora',
    'A179': 'Gaviota patiamarilla',
    'A182': 'Gaviota sombría',
}

def fetch_wiki_common_name(scientific_name):
    url = 'https://es.wikipedia.org/w/api.php'
    params = {
        'action': 'query',
        'prop': 'extracts',
        'exintro': True,
        'explaintext': True,
        'redirects': 1,
        'titles': scientific_name,
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
    return None

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Rebuilding common names for {len(species)} species...")

    updated = 0
    for code, sci_name, current_common in species:
        common = KNOWN_COMMON_NAMES.get(code)
        if not common and (not current_common or current_common == sci_name):
            common = fetch_wiki_common_name(sci_name)
            time.sleep(0.2)

        if common and common.lower() != sci_name.lower():
            cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))
            updated += 1
        else:
            cursor.execute("UPDATE especie SET nombre_comun = NULL WHERE codigo_n2000 = ?", (code,))

    conn.commit()
    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Species database successfully rebuilt. Updated {updated} common names.")

if __name__ == '__main__':
    main()
