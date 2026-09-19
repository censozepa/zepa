import sqlite3
import pandas as pd
import subprocess
import os

DB_NAME = 'censozepa.db'
ACCESS_DB = 'natura2000_data/Natura2000_end2021_ES_20230104.accdb'

def mdb_to_df(table_name):
    cmd = f'mdb-export {ACCESS_DB} {table_name}'
    process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    df = pd.read_csv(process.stdout)
    return df

def build_from_accdb():
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    print("Reading SPECIES table (Art 4)...")
    species_df = mdb_to_df('SPECIES')

    print("Reading OTHERSPECIES table (Art 3.3)...")
    otherspecies_df = mdb_to_df('OTHERSPECIES')
    
    print(f"Loaded {len(species_df)} Art 4 records and {len(otherspecies_df)} Other records.")

    # Filter for Birds
    birds_art4 = species_df[species_df['SPGROUP'] == 'Birds']
    birds_other = otherspecies_df[otherspecies_df['SPECIESGROUP'] == 'Birds']

    inserted_species = set()
    inserted_fenologia = 0

    cursor.execute('DELETE FROM fenologia_zepa')

    # Process Art 4
    for _, row in birds_art4.iterrows():
        site = str(row['SITECODE'])
        code = str(row['SPECIESCODE'])
        name = str(row['SPECIESNAME'])
        pop_type = str(row['POPULATION_TYPE']) if pd.notna(row['POPULATION_TYPE']) else ''
        abundance = str(row['ABUNDANCE_CATEGORY']) if pd.notna(row['ABUNDANCE_CATEGORY']) else ''
        
        if code and code != 'nan' and name and name != 'nan':
            # Insert Especie (Ignore if exists, update categoria if needed)
            cursor.execute('''
                INSERT OR IGNORE INTO especie (codigo_n2000, nombre_cientifico, nombre_comun, categoria)
                VALUES (?, ?, ?, ?)
            ''', (code, name, '', 'Art. 4'))
            
            cursor.execute('''
                INSERT INTO fenologia_zepa (id_zepa, id_especie, estatus_ene, abundancia)
                VALUES (?, ?, ?, ?)
            ''', (site, code, pop_type, abundance))
            inserted_fenologia += 1

    # Process Other Species
    for _, row in birds_other.iterrows():
        site = str(row['SITE_CODE'])
        code = str(row['SPECIESCODE'])
        name = str(row['SPECIESNAME'])
        abundance = str(row['ABUNDANCE_CATEGORY']) if pd.notna(row['ABUNDANCE_CATEGORY']) else ''

        # Sometimes OTHERSPECIES has no code, generate a hash or use name
        if code == 'nan' or not code:
            code = 'O_' + str(hash(name))[:6].replace('-', '')
            
        if name and name != 'nan':
            cursor.execute('''
                INSERT OR IGNORE INTO especie (codigo_n2000, nombre_cientifico, nombre_comun, categoria)
                VALUES (?, ?, ?, ?)
            ''', (code, name, '', 'Relevante 3.3'))
            
            cursor.execute('''
                INSERT INTO fenologia_zepa (id_zepa, id_especie, estatus_ene, abundancia)
                VALUES (?, ?, ?, ?)
            ''', (site, code, 'p', abundance))
            inserted_fenologia += 1

    conn.commit()
    conn.close()

    print(f"Insertion complete. Inserted {inserted_fenologia} fenologia records across all ZEPAs.")

if __name__ == '__main__':
    build_from_accdb()
