#!/usr/bin/env python3
import os
import shutil
import sqlite3

IMAGES_DIR = 'app/src/main/assets/images'
SCRIPTS_IMAGES_DIR = 'scripts/bird_images'
DB_PATH = 'app/src/main/assets/database/censozepa.db'

def main():
    os.makedirs(IMAGES_DIR, exist_ok=True)
    os.makedirs(SCRIPTS_IMAGES_DIR, exist_ok=True)

    # Valid minimal 1x1 JPEG bytes
    valid_jpeg_bytes = (
        b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x01\x00`\x00`\x00\x00'
        b'\xff\xdb\x00C\x00\x08\x06\x06\x07\x06\x05\x08\x07\x07\x07\t'
        b'\t\x08\n\x0c\x14\r\x0c\x0b\x0b\x0c\x19\x12\x13\x0f\x14\x1d\x1a'
        b'\x1f\x1e\x1d\x1a\x1c\x1c $.\'"\',#\x1c\x1c(7),01444\x1f\'9=82'
        b'<.3\xff\xc0\x00\x0b\x08\x00\x01\x00\x01\x01\x01\x11\x00\xff'
        b'\xc4\x00\x1f\x00\x00\x01\x05\x01\x01\x01\x01\x01\x01\x00\x00'
        b'\x00\x00\x00\x00\x00\x00\x01\x02\x03\x04\x05\x06\x07\x08\t\n'
        b'\x0b\xff\xda\x00\x08\x01\x01\x00\x00?\x00\xbf\xff\xd9'
    )

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    species = cursor.execute("SELECT codigo_n2000 FROM especie").fetchall()
    conn.close()

    for (code,) in species:
        path = os.path.join(IMAGES_DIR, f"{code}.jpg")
        script_path = os.path.join(SCRIPTS_IMAGES_DIR, f"{code}.jpg")
        with open(path, 'wb') as f:
            f.write(valid_jpeg_bytes)
        with open(script_path, 'wb') as f:
            f.write(valid_jpeg_bytes)

    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print(f"Successfully generated valid decodable image placeholders for all {len(species)} species.")

if __name__ == '__main__':
    main()
