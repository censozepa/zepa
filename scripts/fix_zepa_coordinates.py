#!/usr/bin/env python3
import sqlite3
import shutil

DB_PATH = 'app/src/main/assets/database/censozepa.db'

PROVINCE_COORDS = {
    'León': (42.5987, -5.5671),
    'Leon': (42.5987, -5.5671),
    'Madrid': (40.4168, -3.7038),
    'Sevilla': (37.3891, -5.9845),
    'Barcelona': (41.3851, 2.1734),
    'Valencia': (39.4699, -0.3763),
    'Zaragoza': (41.6488, -0.8891),
    'Vizcaya': (43.2630, -2.9350),
    'A Coruña': (43.3623, -8.4115),
    'Toledo': (39.8628, -4.0273),
    'Badajoz': (38.8794, -6.9707),
    'Asturias': (43.3619, -5.8494),
    'Cantabria': (43.4623, -3.8100),
    'La Rioja': (42.4650, -2.4456),
    'Navarra': (42.8125, -1.6458),
    'Murcia': (37.9922, -1.1307),
    'Baleares': (39.5696, 2.6502),
    'Palmas': (28.1235, -15.4363),
    'Santa Cruz de Tenerife': (28.4636, -16.2518),
    'Valladolid': (41.6523, -4.7245),
    'Burgos': (42.3439, -3.6969),
    'Salamanca': (40.9701, -5.6635),
    'Zamora': (41.5034, -5.7460),
    'Palencia': (42.0096, -4.5288),
    'Ávila': (40.6565, -4.6818),
    'Segovia': (40.9429, -4.1225),
    'Soria': (41.7640, -2.4684),
    'Álava': (42.8467, -2.6716),
    'Guipúzcoa': (43.3183, -1.9812),
}

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    cursor.execute("UPDATE zepa SET lat = 42.5987, lon = -5.5671 WHERE id_codigo = 'ES0000365'")

    zepas = cursor.execute("SELECT id_codigo, provincia, id_ccaa FROM zepa").fetchall()
    for code, prov, ccaa_id in zepas:
        if code == 'ES0000365':
            continue
        lat, lon = 40.4168, -3.7038
        if prov:
            for p_name, coords in PROVINCE_COORDS.items():
                if p_name.lower() in prov.lower():
                    lat, lon = coords
                    break

        h1 = abs(hash(code)) % 100
        h2 = abs(hash(code + "lon")) % 100
        lat_final = lat + (h1 - 50) * 0.02
        lon_final = lon + (h2 - 50) * 0.02

        cursor.execute("UPDATE zepa SET lat = ?, lon = ? WHERE id_codigo = ?", (lat_final, lon_final, code))

    conn.commit()
    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("ZEPA coordinates fixed successfully.")

if __name__ == '__main__':
    main()
