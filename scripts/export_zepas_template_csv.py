#!/usr/bin/env python3
import sqlite3
import csv
import os

DB_PATH = 'app/src/main/assets/database/censozepa.db'
OUTPUT_CSV = 'scripts/csv_exports/zepas_coordenadas_template.csv'

def main():
    os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    query = """
        SELECT id_codigo, nombre, lat, lon
        FROM zepa
        ORDER BY id_codigo ASC
    """
    rows = cursor.execute(query).fetchall()

    with open(OUTPUT_CSV, 'w', encoding='utf-8-sig', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['id_zepa', 'nombre', 'coordenadas_gps', 'localidad'])
        for r in rows:
            z_id, name, lat, lon = r
            coords = f"{lat}, {lon}" if lat is not None and lon is not None else ""
            writer.writerow([z_id, name, coords, 'TODO'])

    conn.close()
    print(f"Successfully exported {len(rows)} ZEPAs template to '{OUTPUT_CSV}'")

if __name__ == '__main__':
    main()
