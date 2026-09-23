#!/usr/bin/env python3
import sqlite3
import csv
import shutil
import os

DB_PATH = 'app/src/main/assets/database/censozepa.db'
CSV_PATH = 'scripts/csv_exports/zepas_coordenadas_template.csv'

def main():
    if not os.path.exists(CSV_PATH):
        print(f"CSV file not found at {CSV_PATH}")
        return

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    try:
        cursor.execute("ALTER TABLE zepa ADD COLUMN localidad TEXT;")
    except Exception:
        pass

    updated_count = 0
    with open(CSV_PATH, 'r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f, delimiter=',')
        for row in reader:
            z_id = row.get('id_zepa', '').strip()
            coords_str = row.get('coordenadas_gps', '').strip()
            localidad = row.get('localidad', '').strip()

            lat, lon = None, None
            if coords_str and ',' in coords_str:
                try:
                    parts = coords_str.split(',')
                    lat = float(parts[0].strip())
                    lon = float(parts[1].strip())
                except ValueError:
                    pass

            if z_id:
                cursor.execute(
                    "UPDATE zepa SET lat = ?, lon = ?, localidad = ? WHERE id_codigo = ?",
                    (lat, lon, localidad if localidad != 'TODO' else None, z_id)
                )
                updated_count += 1

    conn.commit()
    conn.close()

    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Successfully imported coordinates and localities for {updated_count} ZEPAs from CSV into database.")

if __name__ == '__main__':
    main()
