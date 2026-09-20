#!/usr/bin/env python3
import sqlite3
import sys

DB_PATH = '/app/src/main/assets/database/censozepa.db'

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    print("=== Censo ZEPA Database CLI Helper ===")
    print("Base de datos:", DB_PATH)
    print("Escribe tus consultas SQL (ej: SELECT * FROM ccaa; o .exit para salir):")

    while True:
        try:
            query = input("sql> ").strip()
            if not query:
                continue
            if query.lower() in ['.exit', 'exit', 'quit']:
                break
            if query.lower() == '.tables':
                tables = cursor.execute("SELECT name FROM sqlite_master WHERE type='table';").fetchall()
                print("Tablas:", [t[0] for t in tables])
                continue

            cursor.execute(query)
            if query.lower().startswith('select'):
                rows = cursor.fetchall()
                cols = [description[0] for description in cursor.description]
                print(" | ".join(cols))
                print("-" * 40)
                for row in rows:
                    print(" | ".join(str(val) for val in row))
                print(f"({len(rows)} filas)")
            else:
                conn.commit()
                print("Consulta ejecutada con éxito. Cambios guardados.")
        except Exception as e:
            print("Error SQL:", e)

    conn.close()

if __name__ == '__main__':
    main()
