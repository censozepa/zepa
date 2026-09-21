#!/usr/bin/env python3
import sqlite3
import shutil

DB_PATH = 'app/src/main/assets/database/censozepa.db'

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    total_esp_before = cursor.execute("SELECT COUNT(*) FROM especie").fetchone()[0]
    total_fen_before = cursor.execute("SELECT COUNT(*) FROM fenologia_zepa").fetchone()[0]
    print(f"Before removal - Especie: {total_esp_before}, Fenologia: {total_fen_before}")

    cursor.execute("DELETE FROM especie WHERE codigo_n2000 NOT LIKE 'A%'")
    cursor.execute("DELETE FROM fenologia_zepa WHERE id_especie NOT LIKE 'A%'")

    conn.commit()
    conn.execute("VACUUM;")

    total_esp_after = cursor.execute("SELECT COUNT(*) FROM especie").fetchone()[0]
    total_fen_after = cursor.execute("SELECT COUNT(*) FROM fenologia_zepa").fetchone()[0]
    print(f"After removal - Especie: {total_esp_after}, Fenologia: {total_fen_after}")

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("Non-bird species successfully removed. Database updated.")

if __name__ == '__main__':
    main()
