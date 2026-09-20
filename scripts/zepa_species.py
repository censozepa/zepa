#!/usr/bin/env python3
import sqlite3
import sys

DB_PATH = 'app/src/main/assets/database/censozepa.db'

def main():
    if len(sys.argv) < 2:
        print(f"Uso: {sys.argv[0]} <ID_ZEPA> [-c] [-a] [-o]")
        print("Ejemplos:")
        print("  python3 scripts/zepa_species.py ES0000365")
        print("  python3 scripts/zepa_species.py ES0000365 -c")
        print("  python3 scripts/zepa_species.py ES0000365 -c -a")
        print("  python3 scripts/zepa_species.py ES0000365 -c -o")
        sys.exit(1)

    zepa_id = sys.argv[1].strip()
    args = set(sys.argv[2:])

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Get ZEPA info
    zepa = cursor.execute("SELECT id_codigo, nombre, provincia FROM zepa WHERE id_codigo = ?", (zepa_id,)).fetchone()
    if not zepa:
        print(f"No se encontró la ZEPA con código: {zepa_id}")
        conn.close()
        sys.exit(1)

    is_count = '-c' in args
    is_art4 = '-a' in args
    is_other = '-o' in args

    where_clause = "f.id_zepa = ?"
    params = [zepa_id]

    if is_art4 and not is_other:
        where_clause += " AND e.categoria = 'Art. 4'"
    elif is_other and not is_art4:
        where_clause += " AND e.categoria != 'Art. 4'"

    if is_count:
        count_query = f"""
            SELECT COUNT(*)
            FROM fenologia_zepa f
            JOIN especie e ON f.id_especie = e.codigo_n2000
            WHERE {where_clause}
        """
        total = cursor.execute(count_query, params).fetchone()[0]
        label = "Total especies"
        if is_art4:
            label = "Especies Art. 4"
        elif is_other:
            label = "Otras especies"
        print(f"ZEPA {zepa[0]} ({zepa[1]}): {label} = {total}")
        conn.close()
        return

    print(f"==================================================")
    print(f" Especies y Fenología para ZEPA: {zepa[0]} - {zepa[1]} ({zepa[2] or 'N/D'})")
    print(f"==================================================")

    query = f"""
        SELECT
            e.codigo_n2000,
            e.nombre_comun,
            e.nombre_cientifico,
            e.categoria,
            f.abundancia,
            f.estatus_ene, f.estatus_feb, f.estatus_mar, f.estatus_abr,
            f.estatus_may, f.estatus_jun, f.estatus_jul, f.estatus_ago,
            f.estatus_sep, f.estatus_oct, f.estatus_nov, f.estatus_dic
        FROM fenologia_zepa f
        JOIN especie e ON f.id_especie = e.codigo_n2000
        WHERE {where_clause}
        ORDER BY e.nombre_comun ASC
    """
    rows = cursor.execute(query, params).fetchall()
    print(f"Total especies listadas: {len(rows)}\n")

    for i, r in enumerate(rows, 1):
        codigo, com, cient, cat, abun = r[0], r[1], r[2], r[3], r[4]
        meses = ['E', 'F', 'M', 'A', 'My', 'Jn', 'Jl', 'Ag', 'S', 'O', 'N', 'D']
        estatus_meses = r[5:17]

        print(f"[{i}] {com or 'Sin nombre común'} ({cient}) [Código: {codigo}]")
        print(f"    • Categoría: {cat} | Abundancia global: {abun or 'N/D'}")
        meses_str = " | ".join(f"{m}: {est or '-'}" for m, est in zip(meses, estatus_meses))
        print(f"    • Estatus mensual (Ene-Dic): {meses_str}")
        print("-" * 50)

    conn.close()

if __name__ == '__main__':
    main()
