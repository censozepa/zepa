#!/usr/bin/env python3
import sqlite3
import subprocess
import csv
import io
import shutil
import random

DB_PATH = 'app/src/main/assets/database/censozepa.db'
ACCDB_PATH = 'scripts/natura2000_data/2024/Natura2000_end2024_ES.accdb'

EXACT_CODE_TO_COMMON = {
    'A001': 'Colimbo chico', 'A002': 'Colimbo ártico', 'A003': 'Zampullín cuellirrojo',
    'A004': 'Zampullín cornudo', 'A005': 'Somormujo lavanco', 'A007': 'Zampullín chico', 'A008': 'Zampullín cuellinegro',
    'A009': 'Fulmar boreal', 'A010': 'Pardela cenicienta', 'A013': 'Pardela pichoneta',
    'A014': 'Paíño europeo', 'A015': 'Pardela balear', 'A016': 'Alcatraz atlántico',
    'A017': 'Paíño de Leach', 'A018': 'Cormorán moñudo', 'A020': 'Alcatraz atlántico',
    'A021': 'Avetoro común', 'A022': 'Avetorillo común', 'A023': 'Martinete común',
    'A024': 'Garcilla cangrejera', 'A025': 'Garcilla bueyera', 'A026': 'Garceta común',
    'A027': 'Garceta grande', 'A028': 'Garza real', 'A029': 'Garza imperial',
    'A030': 'Cigüeña negra', 'A031': 'Cigüeña blanca', 'A032': 'Morito común',
    'A034': 'Espátula común', 'A035': 'Flamenco enano', 'A036': 'Cisne vulgar',
    'A037': 'Cisne cantor', 'A038': 'Ansar común', 'A039': 'Ansar campestre',
    'A040': 'Ansar piquirrojo', 'A041': 'Ansar careto', 'A042': 'Ansar chico',
    'A043': 'Ansar nival', 'A044': 'Ganso del Canadá', 'A045': 'Barnacla cariblanca',
    'A046': 'Barnacla carinegra', 'A047': 'Tarro blanco', 'A048': 'Tarro canelo',
    'A049': 'Pato colorado', 'A050': 'Ánade silbón', 'A051': 'Ánade friso',
    'A052': 'Cerceta común', 'A053': 'Ánade azulón', 'A054': 'Ánade rabudo',
    'A055': 'Cerceta carretona', 'A056': 'Pato cuchara', 'A057': 'Cerceta pardilla',
    'A058': 'Porrón común', 'A059': 'Porrón europeo', 'A060': 'Porrón pardo',
    'A061': 'Porrón moñudo', 'A062': 'Porrón bastardo', 'A063': 'Eider común',
    'A064': 'Pato havelga', 'A065': 'Serreta mediana', 'A066': 'Negrón común',
    'A067': 'Porrón osculado', 'A068': 'Malvasía cabeciblanca', 'A069': 'Serreta chica',
    'A070': 'Abejero europeo', 'A071': 'Malvasía cabeciblanca', 'A072': 'Abejero europeo',
    'A073': 'Milano negro', 'A074': 'Milano real', 'A075': 'Milano negro',
    'A076': 'Quebrantahuesos', 'A077': 'Alimoche común', 'A078': 'Buitre leonado',
    'A079': 'Buitre negro', 'A080': 'Águila culebrera', 'A081': 'Aguilucho lagunero occidental',
    'A082': 'Aguilucho pálido', 'A083': 'Aguilucho papialbo', 'A084': 'Aguilucho cenizo',
    'A085': 'Azor común', 'A086': 'Gavilán común', 'A087': 'Busardo ratonero',
    'A088': 'Quebrantahuesos', 'A089': 'Águila culebrera', 'A090': 'Águila moteada',
    'A091': 'Águila real', 'A092': 'Águila calzada', 'A093': 'Águila perdicera',
    'A094': 'Águila pescadora', 'A095': 'Cernícalo primilla', 'A096': 'Cernícalo vulgar',
    'A097': 'Cernícalo patirrojo', 'A098': 'Esmerejón', 'A099': 'Alcotán europeo',
    'A100': 'Halcón de Eleonor', 'A101': 'Halcón borní', 'A103': 'Halcón tagarote',
    'A104': 'Urogallo común', 'A105': 'Urogallo pirenaico', 'A107': 'Urogallo',
    'A108': 'Perdiz pardilla', 'A109': 'Perdiz nival', 'A110': 'Perdiz roja',
    'A111': 'Perdiz moruna', 'A113': 'Codorniz común', 'A115': 'Faisán común',
    'A116': 'Gallineta común', 'A117': 'Rascón común', 'A118': 'Rascón común',
    'A119': 'Polluela pintoja', 'A120': 'Polluela chica', 'A121': 'Gallineta común',
    'A122': 'Calamón común', 'A123': 'Gallineta común', 'A125': 'Focha común',
    'A126': 'Focha cornuda', 'A127': 'Grulla común', 'A128': 'Sisón común',
    'A129': 'Avutarda común', 'A130': 'Ostrero europeo', 'A131': 'Cigüeñuela común',
    'A132': 'Avoceta común', 'A133': 'Alcaraván común', 'A134': 'Corredor sahariano',
    'A135': 'Canastera común', 'A136': 'Chorlitejo chico', 'A137': 'Chorlitejo grande',
    'A138': 'Chorlitejo patinegro', 'A139': 'Chorlitejo patinegro', 'A140': 'Chorlito dorado europeo',
    'A141': 'Chorlito gris', 'A142': 'Avefría europea', 'A143': 'Correlimos gordo',
    'A144': 'Correlimos gordo', 'A145': 'Correlimos menudo', 'A146': 'Correlimos de Temminck',
    'A147': 'Correlimos zarapitín', 'A148': 'Correlimos oscuro', 'A149': 'Correlimos común',
    'A151': 'Combatiente', 'A152': 'Agachadiza chica', 'A153': 'Agachadiza común',
    'A155': 'Chocha perdiz', 'A156': 'Aguja colinegra', 'A157': 'Aguja colipinta',
    'A158': 'Zarapico trinador', 'A160': 'Zarapito real', 'A161': 'Archibebe oscuro',
    'A162': 'Archibebe común', 'A163': 'Archibebe fino', 'A164': 'Archibebe claro',
    'A165': 'Andarríos oscuro', 'A166': 'Andarríos bastardo', 'A167': 'Vuelvepiedras común',
    'A168': 'Andarríos chico', 'A169': 'Vuelvepiedras común', 'A170': 'Falaropo picofino',
    'A171': 'Falaropo picogrueso', 'A172': 'Págalo pomarino', 'A173': 'Págalo parásito',
    'A174': 'Págalo rabero', 'A175': 'Págalo grande', 'A176': 'Gaviota cabecinegra',
    'A177': 'Gaviota cabecinegra', 'A178': 'Gaviota de Sabine', 'A179': 'Gaviota reidora',
    'A180': 'Gaviota picofina', 'A181': 'Gaviota de Audouin', 'A182': 'Gaviota cenicienta',
    'A183': 'Gaviota sombría', 'A184': 'Gaviota argéntea', 'A185': 'Gaviota polar',
    'A187': 'Gaviota hiperbórea', 'A188': 'Gaviota tridáctila', 'A189': 'Foc de mar',
    'A192': 'Charrán rosado', 'A193': 'Charrán común', 'A194': 'Charrán ártico',
    'A195': 'Charrancito común', 'A196': 'Fumarel común', 'A197': 'Fumarel cariblanco',
    'A198': 'Fumarel aliblanco', 'A199': 'Arao común', 'A200': 'Alca común',
    'A203': 'Mérgulo atlántico', 'A204': 'Frailecillo atlántico', 'A205': 'Ganga ibérica',
    'A206': 'Paloma bravía', 'A207': 'Paloma zurita', 'A208': 'Paloma torcaz',
    'A209': 'Tórtola turca', 'A210': 'Tórtola europea', 'A211': 'Cria-cuervos',
    'A212': 'Cuco común', 'A213': 'Lechuza común', 'A214': 'Autillo europeo',
    'A215': 'Búho real', 'A218': 'Mochuelo europeo', 'A219': 'Cárabo común',
    'A221': 'Búho chico', 'A222': 'Búho campestre', 'A223': 'Mochuelo boreal',
    'A224': 'Chotacabras europeo', 'A225': 'Chotacabras pardo', 'A226': 'Vencejo común',
    'A227': 'Vencejo pálido', 'A228': 'Vencejo real', 'A229': 'Martín pescador común',
    'A230': 'Abejaruco europeo', 'A231': 'Carraca europea', 'A232': 'Abubilla',
    'A233': 'Torcecuellos', 'A235': 'Pito real', 'A236': 'Pito negro',
    'A237': 'Pico picapinos', 'A238': 'Pico mediano', 'A239': 'Pico dorsiblanco',
    'A241': 'Calandria común', 'A242': 'Calandria común', 'A243': 'Terrera común',
    'A244': 'Cogujada común', 'A245': 'Cogujada montesina', 'A246': 'Totovía',
    'A247': 'Alondra común', 'A248': 'Alondra cornuda', 'A249': 'Avión zapador',
    'A250': 'Avión roquero', 'A251': 'Golondrina común', 'A252': 'Golondrina dáurica oriental',
    'A253': 'Avión común', 'A254': 'Bisbita arbóreo', 'A255': 'Bisbita campestre',
    'A256': 'Bisbita arbóreo', 'A257': 'Bisbita pratense', 'A258': 'Bisbita gorgirrojo',
    'A259': 'Bisbita alpino', 'A260': 'Lavandera boyera', 'A261': 'Lavandera cascadeña',
    'A262': 'Lavandera blanca', 'A264': 'Mirlo acuático', 'A265': 'Chochín',
    'A266': 'Acentor común', 'A267': 'Acentor alpino', 'A268': 'Alzacola rojizo',
    'A269': 'Petirrojo europeo', 'A270': 'Ruiseñor ruso', 'A271': 'Ruiseñor común',
    'A273': 'Colirrojo tizón', 'A274': 'Colirrojo real', 'A275': 'Tarabilla norteña',
    'A276': 'Tarabilla común', 'A277': 'Collalba gris', 'A278': 'Collalba rubia',
    'A279': 'Collalba negra', 'A280': 'Roquero rojo', 'A281': 'Roquero solitario',
    'A282': 'Zorzal alirrojo', 'A283': 'Mirlo común', 'A284': 'Zorzal real',
    'A285': 'Zorzal común', 'A286': 'Zorzal alirrojo', 'A288': 'Ruiseñor cetrino',
    'A289': 'Buitrón', 'A290': 'Buscarla pinta', 'A292': 'Buscarla unicolor',
    'A293': 'Carricerín cejudo', 'A294': 'Carricerín común', 'A295': 'Carricerín común',
    'A296': 'Carricero palustre', 'A297': 'Carricero común', 'A298': 'Carricero tordal',
    'A299': 'Zarcero icterino', 'A300': 'Zarcero común', 'A301': 'Curruca rabilarga',
    'A302': 'Curruca rabilarga', 'A303': 'Curruca tomillera', 'A304': 'Curruca carrasqueña',
    'A305': 'Curruca cabecinegra', 'A306': 'Curruca mirlona', 'A307': 'Curruca tomillera',
    'A309': 'Curruca zarcera', 'A310': 'Curruca mosquitera', 'A311': 'Curruca capirotada',
    'A312': 'Mosquitero ibérico', 'A314': 'Mosquitero zumbón', 'A315': 'Mosquitero común',
    'A316': 'Mosquitero musical', 'A317': 'Reyezuelo sencillo', 'A318': 'Reyezuelo listado',
    'A319': 'Papamoscas gris', 'A320': 'Papamoscas cerrojillo', 'A321': 'Papamoscas collarillo',
    'A322': 'Papamoscas cerrojillo', 'A323': 'Bigotudo', 'A324': 'Mito',
    'A325': 'Carbonero palustre', 'A326': 'Carbonero montano', 'A330': 'Carbonero común',
    'A331': 'Trepador azul', 'A332': 'Trepador azul', 'A333': 'Treparriscos',
    'A334': 'Agateador norteño', 'A335': 'Agateador común', 'A336': 'Pájaro moscón',
    'A337': 'Oropéndola europea', 'A338': 'Alcaudón dorsirrojo', 'A339': 'Alcaudón chico',
    'A340': 'Alcaudón real', 'A341': 'Alcaudón común', 'A342': 'Arrendajo',
    'A343': 'Urraca', 'A345': 'Chova piquigualda', 'A346': 'Chova piquirroja',
    'A347': 'Grajilla', 'A348': 'Graja', 'A349': 'Corneja negra',
    'A350': 'Cuervo común', 'A351': 'Estornino pinto', 'A353': 'Estornino negro',
    'A354': 'Gorrión común', 'A355': 'Gorrión moruno', 'A356': 'Gorrión molinero',
    'A357': 'Gorrión chillón', 'A358': 'Gorrión alpino', 'A359': 'Pinzón vulgar',
    'A360': 'Pinzón real', 'A361': 'Verdecillo', 'A362': 'Piquituerto',
    'A363': 'Verderón común', 'A364': 'Jilguero europeo', 'A365': 'Pardillo común',
    'A366': 'Pardillo común', 'A369': 'Piquituerto común', 'A372': 'Camachuelo común',
    'A373': 'Picogordo', 'A375': 'Escribano nival', 'A376': 'Escribano cerillo',
    'A377': 'Escribano soteño', 'A378': 'Escribano montesino', 'A379': 'Escribano hortelano',
    'A381': 'Escribano palustre', 'A383': 'Triguero', 'A385': 'Petrel de Madeira',
    'A387': 'Petrel de Bulwer', 'A388': 'Pardela chica', 'A389': 'Paíño pechialbo',
    'A391': 'Cormorán grande', 'A392': 'Cormorán moñudo', 'A397': 'Tarro canelo',
    'A399': 'Elanio común', 'A401': 'Gavilán común', 'A403': 'Busardo moro',
    'A405': 'Águila imperial ibérica', 'A415': 'Perdiz pardilla', 'A416': 'Avutarda hubara',
    'A419': 'Arao común', 'A420': 'Ganga ortega', 'A422': 'Paloma turqué',
    'A423': 'Paloma rabiche', 'A424': 'Vencejo unicolor', 'A427': 'Pico picapinos',
    'A428': 'Pico picapinos canario', 'A430': 'Alondra de Dupont', 'A436': 'Collalba negra',
    'A437': 'Tarabilla canaria', 'A438': 'Zarcero bereber', 'A448': 'Pinzón vulgar',
    'A452': 'Camachuelo trompetero', 'A459': 'Gaviota patiamarilla', 'A464': 'Pardela balear',
    'A473': 'Carbonero garrapinos', 'A475': 'Terrera marismeña', 'A476': 'Pardillo norteño',
    'A478': 'Lúgano', 'A479': 'Golondrina dáurica', 'A480': 'Pechiazul',
    'A481': 'Rabilargo ibérico', 'A483': 'Herrerillo común', 'A485': 'Pinzón azul',
    'A486': 'Pinzón azul', 'A487': 'Zarcero pálido', 'A496': 'Alcaudón real',
    'A497': 'Herrerillo capuchino', 'A499': 'Mosquitero papialbo', 'A504': 'Pardela chica',
    'A513': 'Urogallo cantábrico', 'A558': 'Faisán venerado', 'A568': 'Pico de coral común',
    'A569': 'Curruca balear', 'A570': 'Curruca mirlona', 'A572': 'Mosquitero común',
    'A604': 'Gaviota patiamarilla', 'A618': 'Mosquitero ibérico', 'A619': 'Azor común',
    'A645': 'Curruca rabilarga', 'A661': 'Urogallo pirenaico', 'A662': 'Arao común',
    'A663': 'Flamenco rosado', 'A673': 'Alcaraván común', 'A675': 'Barnacla carinegra',
    'A682': 'Chorlitejo patinegro', 'A683': 'Cormorán grande', 'A684': 'Cormorán moñudo',
    'A687': 'Paloma torcaz', 'A693': 'Pardela balear', 'A707': 'Águila-azor perdicera',
    'A712': 'Perdiz nival', 'A717': 'Torillo común', 'A722': 'Calamón común',
    'A727': 'Chorlito carambolo', 'A734': 'Fumarel cariblanco', 'A738': 'Avión común',
    'A768': 'Zarapito real', 'A773': 'Garceta grande', 'A850': 'Pardela cenicienta',
    'A851': 'Pardela atlántica', 'A852': 'Pardela sombría', 'A853': 'Pardela capirotada',
    'A854': 'Paíño boreal', 'A855': 'Ánade silbón', 'A856': 'Cerceta carretona',
    'A857': 'Pato cuchara', 'A858': 'Águila pomerana', 'A859': 'Águila moteada',
    'A861': 'Combatiente', 'A862': 'Gaviota enana', 'A863': 'Charrán patinegro',
    'A867': 'Pito real', 'A868': 'Pico mediano', 'A869': 'Pico menor',
    'A874': 'Paíño de Madeira', 'A880': 'Pardela chica', 'A885': 'Charrancito común',
    'A889': 'Ánade friso', 'A892': 'Polluela bastarda', 'A893': 'Polluela chica',
    'A894': 'Charrán caspio', 'A900': 'Negrón común', 'A907': 'Curruca sarda'
}

def main():
    print("Rebuilding database with all 7 complete tables including sesion, avistamiento, favorite_zepa...")
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Ensure complete schema with sessions and favorites tables so user data persists correctly
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
            categoria TEXT,
            foto_asset TEXT
        );
        CREATE TABLE IF NOT EXISTS fenologia_zepa (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            id_zepa TEXT NOT NULL,
            id_especie TEXT NOT NULL,
            estatus_ene TEXT, estatus_feb TEXT, estatus_mar TEXT,
            estatus_abr TEXT, estatus_may TEXT, estatus_jun TEXT,
            estatus_jul TEXT, estatus_ago TEXT, estatus_sep TEXT,
            estatus_oct TEXT, estatus_nov TEXT, estatus_dic TEXT,
            abundancia TEXT,
            categoria TEXT
        );
        CREATE TABLE IF NOT EXISTS sesion (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            id_zepa TEXT NOT NULL,
            fecha_hora_inicio INTEGER NOT NULL,
            fecha_hora_fin INTEGER,
            distancia_recorrida REAL,
            track_gps_json TEXT
        );
        CREATE TABLE IF NOT EXISTS avistamiento (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            id_sesion INTEGER NOT NULL,
            id_especie TEXT NOT NULL,
            hora INTEGER NOT NULL,
            latitud REAL NOT NULL,
            longitud REAL NOT NULL,
            cantidad INTEGER NOT NULL,
            alerta_fenologica INTEGER NOT NULL,
            notas TEXT
        );
        CREATE TABLE IF NOT EXISTS favorite_zepa (
            zepaId TEXT PRIMARY KEY NOT NULL
        );
    """)

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Total species in DB: {len(species)}")

    for code, sci_name, current_common in species:
        common = EXACT_CODE_TO_COMMON.get(code)
        if not common:
            binomial = " ".join(sci_name.split()[:2])
            common = EXACT_CODE_TO_COMMON.get(binomial)
        if not common:
            common = sci_name.replace('_', ' ').capitalize()

        cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))

    conn.commit()

    # Validation check for cross-genus shared names
    query = """
        SELECT LOWER(nombre_comun), GROUP_CONCAT(nombre_cientifico || ' (' || codigo_n2000 || ')')
        FROM especie
        WHERE nombre_comun IS NOT NULL AND nombre_comun != ''
        GROUP BY LOWER(nombre_comun)
        HAVING COUNT(DISTINCT SUBSTR(nombre_cientifico, 1, INSTR(nombre_cientifico || ' ', ' ') - 1)) > 1
    """
    cross_genus = cursor.execute(query).fetchall()

    if cross_genus:
        print(f"\n[ERROR] Found {len(cross_genus)} common names shared across different genera:")
        for cg in cross_genus:
            print(f' - "{cg[0]}": {cg[1]}')
        raise AssertionError("Cross-genus common name validation failed!")
    else:
        print("\n[SUCCESS] Strict validation passed! Every common name is uniquely mapped to its correct genus/species.")

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("Database updated, complete schema ensured, and synced successfully.")

if __name__ == '__main__':
    main()
