#!/usr/bin/env python3
import subprocess
import os

DB_PATH = 'scripts/natura2000_data/2024/Natura2000_end2024_ES.accdb'
OUTPUT_DIR = 'scripts/csv_exports'

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    res = subprocess.run(['mdb-tables', DB_PATH], capture_output=True, text=True)
    if res.returncode != 0:
        print("Error getting tables with mdb-tables:", res.stderr)
        return

    tables = res.stdout.split()
    print(f"Found {len(tables)} tables to export to CSV.")

    for table in tables:
        csv_path = os.path.join(OUTPUT_DIR, f"{table}.csv")
        print(f"Exporting table '{table}' -> '{csv_path}'...")
        try:
            with open(csv_path, 'w', encoding='utf-8') as f:
                subprocess.run(['mdb-export', DB_PATH, table], stdout=f, check=True)
        except Exception as e:
            print(f"Failed to export {table}: {e}")

    print(f"\nAll tables successfully exported to {OUTPUT_DIR}/")

if __name__ == '__main__':
    main()
