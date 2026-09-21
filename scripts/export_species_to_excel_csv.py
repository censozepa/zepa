#!/usr/bin/env python3
import sqlite3
import csv
import os

DB_PATH = 'app/src/main/assets/database/censozepa.db'
OUTPUT_CSV = 'scripts/csv_exports/listado_aves_zepa.csv'

def main():
    os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    query = """
        SELECT codigo_n2000, nombre_cientifico, nombre_comun
        FROM especie
        ORDER BY nombre_cientifico ASC
    """
    rows = cursor.execute(query).fetchall()

    # Write CSV with utf-8-sig (BOM) for seamless Excel import with Spanish accents/tildes
    with open(OUTPUT_CSV, 'w', encoding='utf-8-sig', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Codigo N2000', 'Nombre Cientifico', 'Nombre Comun'])
        for row in rows:
            writer.writerow(row)

    conn.close()
    print(f"Successfully exported {len(rows)} species to '{OUTPUT_CSV}'")

if __name__ == '__main__':
    main()
