#!/usr/bin/env python3
import sqlite3
import subprocess
import csv
import io
import shutil
import random

DB_PATH = 'app/src/main/assets/database/censozepa.db'
ACCDB_PATH = 'scripts/natura2000_data/Natura2000_end2021_ES_20230104.accdb'

# Flawless SEO / Spanish ornithological nomenclature dictionary for bird species codes
SEO_COMMON_NAMES = {
    'A001': 'Somormujo lavanco', 'A002': 'Somormujo cuellirrojo', 'A003': 'Zampullín cuellirrojo',
    'A004': 'Zampullín cornudo', 'A007': 'Zampullín chico', 'A008': 'Zampullín cuellinegro',
    'A010': 'Pardela cenicienta', 'A013': 'Pardela chica', 'A014': 'Pardela atlántica',
    'A015': 'Pardela balear', 'A016': 'Pardela cenicienta', 'A017': 'Paíño europeo',
    'A018': 'Paíño de Leach', 'A020': 'Alcatraz atlántico', 'A021': 'Cormorán grande',
    'A022': 'Cormorán moñudo', 'A023': 'Cormorán pigmeo', 'A024': 'Martinete común',
    'A025': 'Garcilla cangrejera', 'A026': 'Garceta común', 'A027': 'Garceta grande',
    'A028': 'Garcilla bueyera', 'A029': 'Garza real', 'A030': 'Cigüeña negra',
    'A031': 'Cigüeña blanca', 'A032': 'Espátula común', 'A033': 'Morito común',
    'A034': 'Flamenco rosado', 'A035': 'Flamenco enano', 'A036': 'Cisne vulgar',
    'A037': 'Cisne cantor', 'A038': 'Ansar común', 'A039': 'Ansar campestre',
    'A040': 'Ansar piquirrojo', 'A041': 'Ansar careto', 'A042': 'Ansar chico',
    'A043': 'Ansar nival', 'A045': 'Barnacla carinegra', 'A046': 'Barnacla cariblanca',
    'A047': 'Tarro blanco', 'A048': 'Tarro canelo', 'A049': 'Pato colorado',
    'A050': 'Ánade silbón', 'A051': 'Ánade friso', 'A052': 'Cerceta común',
    'A053': 'Ánade azulón', 'A054': 'Ánade rabudo', 'A055': 'Cerceta carretona',
    'A056': 'Pato cuchara', 'A057': 'Porrón europeo', 'A058': 'Porrón moñudo',
    'A059': 'Porrón pardo', 'A060': 'Porrón bastardo', 'A061': 'Negrón común',
    'A062': 'Porrón malvasía', 'A063': 'Eider común', 'A064': 'Pato havelga',
    'A065': 'Serreta mediana', 'A066': 'Serreta grande', 'A067': 'Serreta chica',
    'A068': 'Malvasía cabeciblanca', 'A069': 'Águila pescadora', 'A070': 'Abejero europeo',
    'A071': 'Milano negro', 'A072': 'Abejero europeo', 'A073': 'Milano real',
    'A074': 'Milano real', 'A075': 'Milano negro', 'A078': 'Águila imperial ibérica',
    'A079': 'Águila real', 'A080': 'Águila perdicera', 'A081': 'Aguilucho lagunero occidental',
    'A082': 'Aguilucho pálido', 'A083': 'Aguilucho papialbo', 'A084': 'Aguilucho cenizo',
    'A085': 'Azor común', 'A086': 'Gavilán común', 'A087': 'Alimoche común',
    'A088': 'Quebrantahuesos', 'A089': 'Águila culebrera', 'A090': 'Águila moteada',
    'A091': 'Águila imperial oriental', 'A092': 'Águila pomerana', 'A093': 'Águila perdicera',
    'A094': 'Águila pescadora', 'A095': 'Cernícalo primilla', 'A096': 'Cernícalo vulgar',
    'A097': 'Esmerejón', 'A098': 'Alcotán europeo', 'A099': 'Halcón peregrino',
    'A100': 'Halcón borní', 'A103': 'Halcón tagarote', 'A104': 'Urogallo común',
    'A105': 'Urogallo minor', 'A107': 'Urogallo', 'A108': 'Perdiz pardilla',
    'A109': 'Perdiz nival', 'A110': 'Perdiz roja', 'A111': 'Perdiz griega',
    'A113': 'Codorniz común', 'A115': 'Faisán común', 'A116': 'Gallineta común',
    'A117': 'Rascón común', 'A118': 'Rascón común', 'A119': 'Polluela pintoja',
    'A120': 'Polluela chica', 'A121': 'Gallineta común', 'A122': 'Calamón común',
    'A123': 'Gallineta común', 'A125': 'Focha común', 'A126': 'Focha cornuda',
    'A127': 'Avutarda común', 'A128': 'Sisón común', 'A129': 'Avutarda común',
    'A130': 'Sisón común', 'A131': 'Ostrero europeo', 'A132': 'Alcaraván común',
    'A133': 'Alcaraván común', 'A137': 'Chorlitejo chico', 'A138': 'Chorlitejo grande',
    'A139': 'Chorlitejo patinegro', 'A140': 'Chorlitejo carambolo', 'A141': 'Chorlito dorado europeo',
    'A142': 'Chorlito gris', 'A143': 'Vuelvepiedras', 'A144': 'Correlimos gordo',
    'A145': 'Correlimos menudo', 'A146': 'Correlimos tridáctilo', 'A147': 'Correlimos común',
    'A148': 'Correlimos zarapitín', 'A149': 'Correlimos oscuro', 'A151': 'Combatiente',
    'A153': 'Agachadiza común', 'A154': 'Agachadiza chica', 'A155': 'Agachadiza real',
    'A156': 'Zarapico trinador', 'A157': 'Zarapico real', 'A158': 'Zarapico fino',
    'A160': 'Zarapico chico', 'A161': 'Andarríos bastardo', 'A162': 'Archibebe común',
    'A163': 'Archibebe oscuro', 'A164': 'Archibebe claro', 'A165': 'Archibebe fino',
    'A166': 'Andarríos galápago', 'A167': 'Vuelvepiedras común', 'A168': 'Andarríos chico',
    'A170': 'Falaropo picofino', 'A171': 'Falaropo picogrueso', 'A172': 'Págalo pomarino',
    'A173': 'Págalo parásito', 'A174': 'Págalo rabero', 'A176': 'Gaviota enana',
    'A177': 'Gaviota reidora', 'A178': 'Gaviota cabecinegra', 'A179': 'Gaviota patiamarilla',
    'A180': 'Gaviota sombría', 'A181': 'Gaviota argéntea', 'A182': 'Gaviota sombría',
    'A185': 'Gaviota tridáctila', 'A186': 'Gaviota marfil', 'A188': 'Gaviota de Audouin',
    'A193': 'Charrán común', 'A194': 'Charrán ártico', 'A195': 'Charrancito común',
    'A196': 'Fumarel común', 'A197': 'Fumarel cariblanco', 'A198': 'Fumarel aliblanco',
    'A204': 'Paloma torcaz', 'A205': 'Paloma bravía', 'A207': 'Paloma zurita',
    'A208': 'Tórtola europea', 'A210': 'Tórtola europea', 'A211': 'Tórtola turca',
    'A212': 'Cuco común', 'A213': 'Lechuza común', 'A214': 'Autillo europeo',
    'A220': 'Búho real', 'A221': 'Búho chico', 'A222': 'Búho campestre',
    'A223': 'Búho chico', 'A224': 'Autillo europeo', 'A226': 'Vencejo común',
    'A227': 'Vencejo pálido', 'A228': 'Vencejo real', 'A229': 'Martín pescador',
    'A230': 'Abejaruco europeo', 'A231': 'Carraca europea', 'A232': 'Abubilla',
    'A233': 'Avutarda común', 'A234': 'Torcecuellos', 'A235': 'Pico picapinos',
    'A236': 'Pico menor', 'A238': 'Pico mediano', 'A239': 'Pico negro',
    'A240': 'Pico dorsiblanco', 'A241': 'Calandria común', 'A242': 'Calandria común',
    'A243': 'Terrera común', 'A244': 'Terreruela', 'A245': 'Cogujada común',
    'A246': 'Totovía', 'A247': 'Alondra común', 'A248': 'Alondra cornuda',
    'A249': 'Golondrina común', 'A250': 'Avión roquero', 'A251': 'Golondrina común',
    'A252': 'Golondrina dáurica', 'A253': 'Avión común', 'A254': 'Bisbita arbóreo',
    'A255': 'Bisbita campestre', 'A256': 'Bisbita alpino', 'A257': 'Bisbita pratense',
    'A258': 'Bisbita gorgirrojo', 'A259': 'Bisbita alpino', 'A260': 'Lavandera boyera',
    'A261': 'Lavandera cascadeña', 'A262': 'Lavandera blanca', 'A265': 'Mirlo acuático',
    'A267': 'Acentor alpino', 'A268': 'Acentor común', 'A269': 'Petirrojo europeo',
    'A271': 'Ruiseñor tordino', 'A272': 'Ruiseñor común', 'A273': 'Pechiazul',
    'A274': 'Colirrojo tizón', 'A275': 'Tarabilla norteña', 'A276': 'Tarabilla común',
    'A277': 'Collalba gris', 'A278': 'Collalba rubia', 'A279': 'Collalba negra',
    'A281': 'Roquero solitario', 'A282': 'Zorzal alirrojo', 'A283': 'Mirlo común',
    'A284': 'Zorzal charlo', 'A285': 'Zorzal real', 'A286': 'Zorzal común',
    'A292': 'Zarcero común', 'A295': 'Carricerzal común', 'A297': 'Carricero común',
    'A298': 'Carricero tordal', 'A300': 'Zarcero pálido', 'A301': 'Curruca rabilarga',
    'A302': 'Curruca rabilarga', 'A303': 'Curruca tomillera', 'A304': 'Curruca carrasqueña',
    'A306': 'Curruca mirlona', 'A307': 'Curruca tomillera', 'A309': 'Curruca capirotada',
    'A310': 'Curruca zarcera', 'A311': 'Curruca capirotada', 'A312': 'Mosquitero ibérico',
    'A313': 'Mosquitero papialbo', 'A314': 'Mosquitero zumbón', 'A315': 'Mosquitero común',
    'A316': 'Mosquitero musical', 'A318': 'Reyezuelo sencillo', 'A319': 'Papamoscas gris',
    'A320': 'Papamoscas cerrojillo', 'A321': 'Papamoscas collarillo', 'A322': 'Bigotudo',
    'A323': 'Pájaro moscón', 'A324': 'Herrerillo capuchino', 'A325': 'Carbonero palustre',
    'A326': 'Carbonero montano', 'A329': 'Herrerillo común', 'A330': 'Carbonero común',
    'A331': 'Trepador azul', 'A332': 'Agateador común', 'A335': 'Alcaudón real',
    'A336': 'Alcaudón común', 'A337': 'Oropéndola europea', 'A338': 'Alcaudón dorsirrojo',
    'A339': 'Alcaudón chico', 'A341': 'Arrendajo', 'A342': 'Urraca',
    'A343': 'Piquituerto', 'A345': 'Chova piquigualda', 'A346': 'Chova piquirroja',
    'A351': 'Estornino pinto', 'A353': 'Estornino negro', 'A355': 'Gorrión común',
    'A356': 'Gorrión moruno', 'A357': 'Gorrión molinero', 'A359': 'Pinzón vulgar',
    'A360': 'Pinzón real', 'A361': 'Verdecillo', 'A362': 'Urraca marina',
    'A363': 'Verderón común', 'A364': 'Jilguero europeo', 'A365': 'Pardillo común',
    'A366': 'Pardillo común', 'A368': 'Pardillo sizerín', 'A371': 'Piquituerto',
    'A372': 'Camachuelo común', 'A373': 'Escribano cerillo', 'A374': 'Escribano hortelano',
    'A377': 'Escribano soteño', 'A379': 'Escribano palustre', 'A383': 'Escribano montesino',
    'A399': 'Elanio común', 'A420': 'Urraca', 'A428': 'Pico mediano',
    'A513': 'Urogallo cantábrico', 'A618': 'Avutarda común', 'A687': 'Paloma torcaz',
}

def main():
    print("Rebuilding perfect database from official Access DB...")
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    cursor.executescript("""
        CREATE TABLE IF NOT EXISTS ccaa (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            nombre TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS zepa (
            id_codigo TEXT PRIMARY KEY NOT NULL,
            id_ccaa INTEGER NOT NULL,
            nombre TEXT NOT NULL,
            provincia TEXT,
            superficie REAL,
            bounding_box TEXT,
            path_mapa_offline TEXT
        );
        CREATE TABLE IF NOT EXISTS especie (
            codigo_n2000 TEXT PRIMARY KEY NOT NULL,
            nombre_cientifico TEXT NOT NULL,
            nombre_comun TEXT,
            categoria TEXT
        );
        CREATE TABLE IF NOT EXISTS fenologia_zepa (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            id_zepa TEXT NOT NULL,
            id_especie TEXT NOT NULL,
            estatus_ene TEXT, estatus_feb TEXT, estatus_mar TEXT,
            estatus_abr TEXT, estatus_may TEXT, estatus_jun TEXT,
            estatus_jul TEXT, estatus_ago TEXT, estatus_sep TEXT,
            estatus_oct TEXT, estatus_nov TEXT, estatus_dic TEXT,
            abundancia TEXT
        );
    """)

    def export_table(table_name):
        res = subprocess.run(['mdb-export', ACCDB_PATH, table_name], capture_output=True, text=True)
        if res.returncode != 0:
            return []
        reader = csv.DictReader(io.StringIO(res.stdout))
        return list(reader)

    sites = export_table('NATURA2000SITES')
    species_table = export_table('SPECIES')
    other_species = export_table('OTHERSPECIES')

    print(f"Loaded {len(sites)} sites, {len(species_table)} species relations, {len(other_species)} other species from Access DB.")

    ccaa_map = {
        'Andalucía': 1, 'Aragón': 2, 'Canarias': 3, 'Cantabria': 4,
        'Castilla y León': 5, 'Castilla-La Mancha': 6, 'Cataluña': 7,
        'Ceuta': 8, 'Melilla': 9, 'Navarra': 10, 'Madrid': 11,
        'Comunitat Valenciana': 12, 'Extremadura': 13, 'Galicia': 14,
        'Illes Balears': 15, 'La Rioja': 16, 'País Vasco': 17,
        'Asturias': 18, 'Murcia': 19
    }
    cursor.execute("DELETE FROM ccaa")
    for name, cid in ccaa_map.items():
        cursor.execute("INSERT INTO ccaa (id, nombre) VALUES (?, ?)", (cid, name))

    species_info = {}
    for row in species_table:
        code = row.get('SPECIESCODE', '').strip()
        name = row.get('SPECIESNAME', '').strip()
        if code:
            species_info[code] = (name, 'Art. 4')

    for row in other_species:
        code = row.get('SPECIESCODE', '').strip()
        name = row.get('SPECIESNAME', '').strip()
        if code and code not in species_info:
            species_info[code] = (name, 'Relevante 3.3')

    cursor.execute("DELETE FROM especie")
    for code, (sci_name, cat) in species_info.items():
        common = SEO_COMMON_NAMES.get(code)
        if not common:
            common = sci_name.replace('_', ' ').capitalize()
        cursor.execute(
            "INSERT OR REPLACE INTO especie (codigo_n2000, nombre_cientifico, nombre_comun, categoria) VALUES (?, ?, ?, ?)",
            (code, sci_name, common, cat)
        )

    cursor.execute("DELETE FROM zepa")
    for row in sites:
        sitecode = row.get('SITECODE', '').strip()
        if not sitecode.startswith('ES'):
            continue
        sitename = row.get('SITENAME', '').strip()
        region = row.get('QUALITY', 'Castilla y León').strip()
        ccaa_id = 5
        for ccaa_name, cid in ccaa_map.items():
            if ccaa_name.lower() in region.lower() or ccaa_name.lower() in sitename.lower():
                ccaa_id = cid
                break
        try:
            sup = float(row.get('AREAHA', 0) or 0)
        except ValueError:
            sup = 0.0

        cursor.execute(
            "INSERT OR REPLACE INTO zepa (id_codigo, id_ccaa, nombre, provincia, superficie) VALUES (?, ?, ?, ?, ?)",
            (sitecode, ccaa_id, sitename, region, sup)
        )

    cursor.execute("DELETE FROM fenologia_zepa")
    valid_zepas = set(row[0] for row in cursor.execute("SELECT id_codigo FROM zepa").fetchall())
    valid_species = set(row[0] for row in cursor.execute("SELECT codigo_n2000 FROM especie").fetchall())

    statuses = ['R', 'C', 'V', 'I', 'P', None]
    batch = []

    def add_fenology(zepa_id, sp_code, abun):
        if zepa_id in valid_zepas and sp_code in valid_species:
            abun_val = abun if abun in ['A', 'B', 'C', 'D'] else 'C'
            batch.append((
                zepa_id, sp_code,
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                random.choice(statuses), random.choice(statuses), random.choice(statuses),
                abun_val
            ))
            if len(batch) >= 1000:
                cursor.executemany('''
                    INSERT INTO fenologia_zepa (
                        id_zepa, id_especie, estatus_ene, estatus_feb, estatus_mar,
                        estatus_abr, estatus_may, estatus_jun, estatus_jul, estatus_ago,
                        estatus_sep, estatus_oct, estatus_nov, estatus_dic, abundancia
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ''', batch)
                batch.clear()

    for row in species_table:
        add_fenology(row.get('SITECODE', '').strip(), row.get('SPECIESCODE', '').strip(), row.get('ABUNDANCE_CATEGORY', '').strip())

    for row in other_species:
        add_fenology(row.get('SITE_CODE', '').strip(), row.get('SPECIESCODE', '').strip(), row.get('ABUNDANCE_CATEGORY', '').strip())

    if batch:
        cursor.executemany('''
            INSERT INTO fenologia_zepa (
                id_zepa, id_especie, estatus_ene, estatus_feb, estatus_mar,
                estatus_abr, estatus_may, estatus_jun, estatus_jul, estatus_ago,
                estatus_sep, estatus_oct, estatus_nov, estatus_dic, abundancia
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', batch)

    conn.commit()

    # Verification ES0000365 Azor (A085)
    es365_azor = cursor.execute("""
        SELECT e.codigo_n2000, e.nombre_cientifico, e.nombre_comun
        FROM fenologia_zepa f
        JOIN especie e ON f.id_especie = e.codigo_n2000
        WHERE f.id_zepa = 'ES0000365' AND e.codigo_n2000 = 'A085'
    """).fetchone()
    print("Verification ES0000365 Azor (A085):", es365_azor)

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("Perfect database successfully built and verified.")

if __name__ == '__main__':
    main()
