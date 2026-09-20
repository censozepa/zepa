#!/bin/bash
# Script para consultar la base de datos Censo ZEPA mediante SQL desde la terminal
# Uso: ./scripts/query.sh "SELECT * FROM ccaa;"

DB_PATH="../app/src/main/assets/database/censozepa.db"

if [ -z "$1" ]; then
    echo "Uso: $0 \"CONSULTA_SQL\""
    echo "Ejemplo: $0 \"SELECT * FROM ccaa;\""
    exit 1
fi

python3 -c "
import sqlite3
conn = sqlite3.connect('$DB_PATH')
cursor = conn.cursor()
try:
    cursor.execute('''$1''')
    rows = cursor.fetchall()
    if cursor.description:
        cols = [d[0] for d in cursor.description]
        print(' | '.join(cols))
        print('-' * 60)
        for row in rows:
            print(' | '.join(str(v) for v in row))
        print(f'\n({len(rows)} filas)')
    else:
        conn.commit()
        print('Consulta ejecutada con éxito.')
except Exception as e:
    print('Error SQL:', e)
conn.close()
"
