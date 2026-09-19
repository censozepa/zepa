import sqlite3
import requests
from bs4 import BeautifulSoup
import time
import os

DB_NAME = 'censozepa.db'
BASE_URL = 'https://www.miteco.gob.es'

def setup_database():
    if os.path.exists(DB_NAME):
        os.remove(DB_NAME)

    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    # 1. CCAA
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS ccaa (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL
        )
    ''')

    # 2. ZEPA
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS zepa (
            id_codigo TEXT PRIMARY KEY,
            id_ccaa INTEGER NOT NULL,
            nombre TEXT NOT NULL,
            provincia TEXT,
            superficie REAL,
            bounding_box TEXT,
            path_mapa_offline TEXT,
            FOREIGN KEY (id_ccaa) REFERENCES ccaa(id)
        )
    ''')

    # 3. Especie
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS especie (
            codigo_n2000 TEXT PRIMARY KEY,
            nombre_cientifico TEXT NOT NULL,
            nombre_comun TEXT,
            categoria TEXT
        )
    ''')

    # 4. FenologiaZEPA
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS fenologia_zepa (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_zepa TEXT NOT NULL,
            id_especie TEXT NOT NULL,
            estatus_ene TEXT, estatus_feb TEXT, estatus_mar TEXT,
            estatus_abr TEXT, estatus_may TEXT, estatus_jun TEXT,
            estatus_jul TEXT, estatus_ago TEXT, estatus_sep TEXT,
            estatus_oct TEXT, estatus_nov TEXT, estatus_dic TEXT,
            abundancia TEXT,
            FOREIGN KEY (id_zepa) REFERENCES zepa(id_codigo),
            FOREIGN KEY (id_especie) REFERENCES especie(codigo_n2000)
        )
    ''')

    # Insert some base species (Common birds in Spain - Annex I)
    species = [
        ('A078', 'Aquila adalberti', 'Águila imperial ibérica', 'Art. 4'),
        ('A080', 'Hieraaetus fasciatus', 'Águila perdicera', 'Art. 4'),
        ('A233', 'Otis tarda', 'Avutarda común', 'Art. 4'),
        ('A030', 'Ciconia nigra', 'Cigüeña negra', 'Art. 4'),
        ('A087', 'Neophron percnopterus', 'Alimoche común', 'Art. 4'),
        ('A074', 'Milvus milvus', 'Milano real', 'Art. 4'),
    ]
    cursor.executemany('INSERT INTO especie (codigo_n2000, nombre_cientifico, nombre_comun, categoria) VALUES (?, ?, ?, ?)', species)

    conn.commit()
    return conn

def scrape_miteco_zepas(conn):
    cursor = conn.cursor()

    main_url = f'{BASE_URL}/es/biodiversidad/temas/espacios-protegidos/red-natura-2000/zepa.html'
    print(f"Fetching CCAA list from {main_url} ...")
    resp = requests.get(main_url, timeout=10)
    soup = BeautifulSoup(resp.content, 'html.parser')

    # Find CCAA links
    ccaa_links = []
    for l in soup.find_all('a', href=True):
        if 'zepa_' in l['href'].lower() and 'miteco' not in l['href'].lower():
            name = l.text.strip()
            href = l['href']
            ccaa_links.append((name, href))

    zepa_count = 0

    for idx, (ccaa_name, ccaa_href) in enumerate(ccaa_links, 1):
        print(f"[{idx}/{len(ccaa_links)}] Scraping CCAA: {ccaa_name}")
        cursor.execute('INSERT INTO ccaa (nombre) VALUES (?)', (ccaa_name,))
        ccaa_id = cursor.lastrowid

        ccaa_url = BASE_URL + ccaa_href
        try:
            r = requests.get(ccaa_url, timeout=10)
            ccaa_soup = BeautifulSoup(r.content, 'html.parser')

            tables = ccaa_soup.find_all('table')
            if tables:
                for tr in tables[0].find_all('tr')[1:]: # Skip header
                    tds = [td.text.strip() for td in tr.find_all(['td', 'th'])]
                    if len(tds) >= 2:
                        codigo = tds[0]
                        nombre = tds[1]
                        if codigo.startswith('ES'):
                            cursor.execute('''
                                INSERT OR IGNORE INTO zepa (id_codigo, id_ccaa, nombre, superficie)
                                VALUES (?, ?, ?, ?)
                            ''', (codigo, ccaa_id, nombre, 0.0))

                            zepa_count += 1
                            # Assign random mock species to this ZEPA to test UI Fenology
                            cursor.execute('''
                                INSERT INTO fenologia_zepa (id_zepa, id_especie, estatus_ene, estatus_abr, estatus_jul, estatus_oct, abundancia)
                                VALUES (?, ?, 'p', 'r', 'p', 'p', 'C')
                            ''', (codigo, 'A078'))
        except Exception as e:
            print(f"  Error processing {ccaa_name}: {e}")

        time.sleep(0.5) # Be polite

    conn.commit()
    print(f"Inserted {zepa_count} ZEPAs across {len(ccaa_links)} CCAAs.")

def main():
    print("Starting CensoZEPA data scraper (Real MITECO version)...")
    conn = setup_database()
    scrape_miteco_zepas(conn)
    conn.close()
    print(f"Scraping completed. Pre-populated DB saved to {DB_NAME}.")

if __name__ == '__main__':
    main()
