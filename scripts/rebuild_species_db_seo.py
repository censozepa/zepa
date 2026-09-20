#!/usr/bin/env python3
import sqlite3
import shutil
import requests
import re
import time

DB_PATH = 'app/src/main/assets/database/censozepa.db'

# Comprehensive mapping for all major N2000 bird codes and species in Spain
SPANISH_BIRD_NAMES = {
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
    'A103': 'Halcón borní',
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
    'A304': 'Curruca carrasqueña',
    'A255': 'Bisbita campestre',
    'A077': 'Alimoche común',
    'A214': 'Autillo europeo',
    'A339': 'Alcaudón chico',
    'A281': 'Roquero solitario',
    'A008': 'Zampullín cuellinegro',
    'A282': 'Zorzal alirrojo',
    'A228': 'Vencejo real',
    'A002': 'Somormujo lavanco',
    'A377': 'Escribano soteño',
    'A040': 'Ansar piquirrojo',
    'A093': 'Águila perdicera',
    'A278': 'Collalba rubia',
    'A345': 'Chova piquigualda',
    'A118': 'Rascón común',
    'A035': 'Flamenco rosado',
    'A207': 'Paloma zurita',
    'A195': 'Charrancito común',
    'A319': 'Papamoscas gris',
    'A042': 'Ansar chico',
    'A094': 'Águila pescadora',
    'A194': 'Charrán ártico',
    'A275': 'Tarabilla norteña',
    'A026': 'Garceta común',
    'A167': 'Vuelvepiedras común',
    'A145': 'Correlimos menudo',
    'A090': 'Águila moteada',
    'A164': 'Archibebe claro',
    'A095': 'Cernícalo primilla',
    'A168': 'Andarríos chico',
    'A314': 'Mosquitero zumbón',
    'A267': 'Acentor alpino',
    'A245': 'Cogujada montesina',
    'A513': 'Urogallo cantábrico',
    'A252': 'Golondrina dáurica',
    'A172': 'Págalo pomarino',
    'A120': 'Polluela pintoja',
    'A063': 'Eider común',
    'A428': 'Pico mediano',
    'A062': 'Porrón malvasía',
    'A869': 'Pico menor',
    'A368': 'Pardillo sizerín',
}

def get_spanish_name_from_wiki(sci_name):
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
    return None

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico FROM especie").fetchall()
    print(f"Processing {len(species)} species...")

    updated = 0
    for code, sci_name in species:
        common = SPANISH_BIRD_NAMES.get(code)
        if not common:
            common = get_spanish_name_from_wiki(sci_name)

        if not common:
            # Fallback: clean up scientific name or generate a readable name
            common = sci_name.split('_')[0].replace('_', ' ').capitalize()

        cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))
        updated += 1
        time.sleep(0.05)

    conn.commit()
    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Successfully updated {updated} species common names.")

if __name__ == '__main__':
    main()
