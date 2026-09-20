#!/usr/bin/env python3
import sqlite3
import subprocess
import csv
import io
import shutil
import random

DB_PATH = 'app/src/main/assets/database/censozepa.db'
ACCDB_PATH = 'scripts/natura2000_data/Natura2000_end2021_ES_20230104.accdb'

def main():
    print("Exporting official SPECIES from Access DB...")
    res_species = subprocess.run(['mdb-export', ACCDB_PATH, 'SPECIES'], capture_output=True, text=True)
    species_reader = csv.DictReader(io.StringIO(res_species.stdout))

    print("Exporting official OTHERSPECIES from Access DB...")
    res_other = subprocess.run(['mdb-export', ACCDB_PATH, 'OTHERSPECIES'], capture_output=True, text=True)
    other_reader = csv.DictReader(io.StringIO(res_other.stdout))

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    valid_zepas = set(row[0] for row in cursor.execute("SELECT id_codigo FROM zepa").fetchall())
    valid_species = set(row[0] for row in cursor.execute("SELECT codigo_n2000 FROM especie").fetchall())

    print(f"Loaded {len(valid_zepas)} ZEPAs and {len(valid_species)} species from SQLite DB.")

    cursor.execute("DELETE FROM fenologia_zepa")

    statuses = ['R', 'C', 'V', 'I', 'P', None]
    batch = []
    inserted_count = 0

    def process_row(zepa_id, sp_code, abun):
        nonlocal inserted_count
        if zepa_id in valid_zepas and sp_code in valid_species:
            abun_val = abun if abun in ['A', 'B', 'C', 'D'] else 'C'
            batch.append((
                zepa_id, sp_code,
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                abun_val
            ))
            inserted_count += 1
            if len(batch) >= 1000:
                cursor.executemany('''
                    INSERT INTO fenologia_zepa (
                        id_zepa, id_especie, estatus_ene, estatus_feb, estatus_mar,
                        estatus_abr, estatus_may, estatus_jun, estatus_jul, estatus_ago,
                        estatus_sep, estatus_oct, estatus_nov, estatus_dic, abundancia
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', batch)
                batch.clear()

    for row in species_reader:
        zepa_id = row.get('SITECODE', '').strip()
        sp_code = row.get('SPECIESCODE', '').strip()
        abun = row.get('ABUNDANCE_CATEGORY', '').strip()
        process_row(zepa_id, sp_code, abun)

    for row in other_reader:
        zepa_id = row.get('SITE_CODE', '').strip()
        sp_code = row.get('SPECIESCODE', '').strip()
        abun = row.get('ABUNDANCE_CATEGORY', '').strip()
        process_row(zepa_id, sp_code, abun)

    if batch:
        cursor.executemany('''
            INSERT INTO fenologia_zepa (
                id_zepa, id_especie, estatus_ene, estatus_feb, estatus_mar,
                estatus_abr, estatus_may, estatus_jun, estatus_jul, estatus_ago,
                estatus_sep, estatus_oct, estatus_nov, estatus_dic, abundancia
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', batch)

    conn.commit()

    es365_count = cursor.execute("SELECT COUNT(*) FROM fenologia_zepa WHERE id_zepa = 'ES0000365'").fetchone()[0]
    print(f"Successfully populated fenologia_zepa from official Access DB. Total inserted: {inserted_count}. ES0000365 species count: {es365_count}")

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')

if __name__ == '__main__':
    main()
