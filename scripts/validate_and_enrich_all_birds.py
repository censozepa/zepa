#!/usr/bin/env python3
import sqlite3
import shutil

DB_PATH = 'app/src/main/assets/database/censozepa.db'

# Comprehensive SEO/BirdLife Spanish Common Names Dictionary (keyed by Code AND Scientific Name)
SEO_COMPLETE_BIRD_NAMES = {
    # By Code
    'A001': 'Somormujo lavanco', 'A002': 'Somormujo cuellirrojo', 'A003': 'Zampullín cuellirrojo',
    'A004': 'Zampullín cornudo', 'A005': 'Somormujo lavanco', 'A007': 'Zampullín chico', 'A008': 'Zampullín cuellinegro',
    'A009': 'Fulmar boreal', 'A010': 'Pardela cenicienta', 'A013': 'Pardela pichoneta',
    'A014': 'Paíño europeo', 'A015': 'Pardela balear', 'A016': 'Alcatraz atlántico',
    'A017': 'Paíño de Leach', 'A018': 'Cormorán moñudo', 'A020': 'Alcatraz atlántico',
    'A021': 'Avetoro común', 'A022': 'Avetorillo común', 'A023': 'Cormorán pigmeo',
    'A024': 'Martinete común', 'A025': 'Garcilla cangrejera', 'A026': 'Garceta común',
    'A027': 'Garceta grande', 'A028': 'Garcilla bueyera', 'A029': 'Garza real',
    'A030': 'Cigüeña negra', 'A031': 'Cigüeña blanca', 'A032': 'Espátula común',
    'A033': 'Morito común', 'A034': 'Flamenco rosado', 'A035': 'Flamenco enano',
    'A036': 'Cisne vulgar', 'A037': 'Cisne cantor', 'A038': 'Ansar común',
    'A039': 'Ansar campestre', 'A040': 'Ansar piquirrojo', 'A041': 'Ansar careto',
    'A042': 'Ansar chico', 'A043': 'Ansar nival', 'A044': 'Ganso del Canadá',
    'A045': 'Barnacla carinegra', 'A046': 'Barnacla cariblanca', 'A047': 'Tarro blanco',
    'A048': 'Tarro canelo', 'A049': 'Pato colorado', 'A050': 'Ánade silbón',
    'A051': 'Ánade friso', 'A052': 'Cerceta común', 'A053': 'Ánade azulón',
    'A054': 'Ánade rabudo', 'A055': 'Cerceta carretona', 'A056': 'Pato cuchara',
    'A057': 'Cerceta pardilla', 'A058': 'Porrón moñudo', 'A059': 'Porrón pardo',
    'A060': 'Porrón bastardo', 'A061': 'Negrón común', 'A062': 'Porrón malvasía',
    'A063': 'Eider común', 'A064': 'Pato havelga', 'A065': 'Serreta mediana',
    'A066': 'Serreta grande', 'A067': 'Serreta chica', 'A068': 'Malvasía cabeciblanca',
    'A069': 'Águila pescadora', 'A070': 'Abejero europeo', 'A071': 'Milano negro',
    'A072': 'Abejero europeo', 'A073': 'Milano real', 'A074': 'Milano real',
    'A075': 'Milano negro', 'A076': 'Quebrantahuesos', 'A077': 'Alimoche común',
    'A078': 'Águila imperial ibérica', 'A079': 'Águila real', 'A080': 'Águila perdicera',
    'A081': 'Aguilucho lagunero occidental', 'A082': 'Aguilucho pálido', 'A083': 'Aguilucho papialbo',
    'A084': 'Aguilucho cenizo', 'A085': 'Azor común', 'A086': 'Gavilán común',
    'A087': 'Alimoche común', 'A088': 'Quebrantahuesos', 'A089': 'Águila culebrera',
    'A090': 'Águila moteada', 'A091': 'Águila imperial oriental', 'A092': 'Águila pomerana',
    'A093': 'Águila perdicera', 'A094': 'Águila pescadora', 'A095': 'Cernícalo primilla',
    'A096': 'Cernícalo vulgar', 'A097': 'Esmerejón', 'A098': 'Alcotán europeo',
    'A099': 'Halcón peregrino', 'A100': 'Halcón borní', 'A101': 'Halcón borní',
    'A103': 'Halcón tagarote', 'A104': 'Urogallo común', 'A105': 'Urogallo pirenaico',
    'A107': 'Urogallo', 'A108': 'Perdiz pardilla', 'A109': 'Perdiz nival',
    'A110': 'Perdiz roja', 'A111': 'Perdiz moruna', 'A113': 'Codorniz común',
    'A115': 'Faisán común', 'A116': 'Gallineta común', 'A117': 'Rascón común',
    'A118': 'Rascón común', 'A119': 'Polluela pintoja', 'A120': 'Polluela chica',
    'A121': 'Gallineta común', 'A122': 'Calamón común', 'A123': 'Gallineta común',
    'A125': 'Focha común', 'A126': 'Focha cornuda', 'A127': 'Avutarda común',
    'A128': 'Sisón común', 'A129': 'Avutarda común', 'A130': 'Ostrero europeo',
    'A131': 'Cigüeñuela común', 'A132': 'Alcaraván común', 'A133': 'Alcaraván común',
    'A134': 'Corredor sahariano', 'A135': 'Canastera común', 'A136': 'Chorlitejo chico',
    'A137': 'Chorlitejo grande', 'A138': 'Chorlitejo patinegro', 'A139': 'Chorlitejo patinegro',
    'A140': 'Chorlito dorado europeo', 'A141': 'Chorlito gris', 'A142': 'Chorlito gris',
    'A143': 'Vuelvepiedras común', 'A144': 'Correlimos gordo', 'A145': 'Correlimos menudo',
    'A146': 'Correlimos temminck', 'A147': 'Correlimos zarapitín', 'A148': 'Correlimos oscuro',
    'A149': 'Correlimos común', 'A151': 'Combatiente', 'A152': 'Agachadiza chica',
    'A153': 'Agachadiza común', 'A155': 'Chocha perdiz', 'A156': 'Aguja colinegra',
    'A157': 'Aguja colipinta', 'A158': 'Zarapico trinador', 'A160': 'Zarapito real',
    'A161': 'Andarríos bastardo', 'A162': 'Archibebe común', 'A163': 'Archibebe fino',
    'A164': 'Archibebe claro', 'A165': 'Andarríos oscuro', 'A166': 'Andarríos galápago',
    'A167': 'Vuelvepiedras común', 'A168': 'Andarríos chico', 'A169': 'Vuelvepiedras común',
    'A170': 'Falaropo picofino', 'A171': 'Falaropo picogrueso', 'A172': 'Págalo pomarino',
    'A173': 'Págalo parásito', 'A174': 'Págalo rabero', 'A175': 'Págalo grande',
    'A176': 'Gaviota enana', 'A177': 'Gaviota cabecinegra', 'A178': 'Gaviota de Sabine',
    'A179': 'Gaviota reidora', 'A180': 'Gaviota de Audouin', 'A181': 'Gaviota de Audouin',
    'A182': 'Gaviota enana', 'A183': 'Gaviota sombría', 'A184': 'Gaviota argéntea',
    'A185': 'Gaviota tridáctila', 'A187': 'Gaviota hiperbórea', 'A188': 'Gaviota tridáctila',
    'A189': 'Pico Xaloc / Foc de mar', 'A192': 'Charrán rosado', 'A193': 'Charrán común',
    'A194': 'Charrán ártico', 'A195': 'Charrancito común', 'A196': 'Fumarel común',
    'A197': 'Fumarel cariblanco', 'A198': 'Fumarel aliblanco', 'A199': 'Arao común',
    'A200': 'Alca común', 'A203': 'Mérgulo atlántico', 'A204': 'Frailecillo atlántico',
    'A205': 'Paloma bravía', 'A206': 'Paloma bravía', 'A207': 'Paloma zurita',
    'A208': 'Tórtola europea', 'A209': 'Tórtola turca', 'A210': 'Tórtola europea',
    'A211': 'Cria-cuervos', 'A212': 'Cuco común', 'A213': 'Lechuza común',
    'A214': 'Autillo europeo', 'A215': 'Búho real', 'A219': 'Cárabo común',
    'A220': 'Búho real', 'A221': 'Búho chico', 'A222': 'Búho campestre',
    'A223': 'Búho chico', 'A224': 'Autillo europeo', 'A225': 'Chotacabras pardo',
    'A226': 'Vencejo común', 'A227': 'Vencejo pálido', 'A228': 'Vencejo real',
    'A229': 'Martín pescador', 'A230': 'Abejaruco europeo', 'A231': 'Carraca europea',
    'A232': 'Abubilla', 'A233': 'Torcecuellos', 'A235': 'Pico picapinos',
    'A236': 'Pito negro', 'A237': 'Pico picapinos', 'A238': 'Pico mediano',
    'A239': 'Pico dorsiblanco', 'A241': 'Calandria común', 'A242': 'Calandria común',
    'A243': 'Terrera común', 'A244': 'Terreruela', 'A245': 'Cogujada común',
    'A246': 'Totovía', 'A247': 'Alondra común', 'A248': 'Alondra cornuda',
    'A249': 'Avión zapador', 'A250': 'Avión roquero', 'A251': 'Golondrina común',
    'A252': 'Golondrina dáurica', 'A253': 'Avión común', 'A254': 'Bisbita arbóreo',
    'A255': 'Bisbita campestre', 'A256': 'Bisbita alpino', 'A257': 'Bisbita pratense',
    'A258': 'Bisbita gorgirrojo', 'A259': 'Bisbita alpino', 'A260': 'Lavandera boyera',
    'A261': 'Lavandera cascadeña', 'A262': 'Lavandera blanca', 'A264': 'Mirlo acuático',
    'A265': 'Mirlo acuático', 'A266': 'Acentor común', 'A267': 'Acentor alpino',
    'A268': 'Cerceta carretona', 'A269': 'Petirrojo europeo', 'A270': 'Ruiseñor ruso',
    'A271': 'Ruiseñor tordino', 'A272': 'Ruiseñor común', 'A273': 'Pechiazul',
    'A274': 'Colirrojo tizón', 'A275': 'Tarabilla norteña', 'A276': 'Tarabilla común',
    'A277': 'Collalba gris', 'A278': 'Collalba rubia', 'A279': 'Collalba negra',
    'A280': 'Roquero rojo', 'A281': 'Roquero solitario', 'A282': 'Zorzal alirrojo',
    'A283': 'Mirlo común', 'A284': 'Zorzal real', 'A285': 'Zorzal real',
    'A286': 'Zorzal común', 'A288': 'Ruiseñor cetrino', 'A290': 'Locustella pintada',
    'A292': 'Buscarla tarsiblanca', 'A293': 'Carricerín cejudo', 'A294': 'Carricerín común',
    'A295': 'Carricerín común', 'A296': 'Carricero palustre', 'A297': 'Carricero común',
    'A298': 'Carricero tordal', 'A299': 'Zarcero icterino', 'A300': 'Zarcero pálido',
    'A301': 'Curruca rabilarga', 'A302': 'Curruca rabilarga', 'A303': 'Curruca tomillera',
    'A304': 'Curruca carrasqueña', 'A305': 'Curruca cabecinegra', 'A306': 'Curruca mirlona',
    'A307': 'Curruca tomillera', 'A309': 'Curruca zarcera', 'A310': 'Curruca zarcera',
    'A311': 'Curruca capirotada', 'A312': 'Mosquitero ibérico', 'A313': 'Mosquitero papialbo',
    'A314': 'Mosquitero zumbón', 'A315': 'Mosquitero común', 'A316': 'Mosquitero musical',
    'A317': 'Reyezuelo sencillo', 'A318': 'Reyezuelo listado', 'A319': 'Papamoscas gris',
    'A320': 'Papamoscas cerrojillo', 'A321': 'Papamoscas collarillo', 'A322': 'Bigotudo',
    'A323': 'Pájaro moscón', 'A324': 'Herrerillo capuchino', 'A325': 'Carbonero palustre',
    'A326': 'Carbonero montano', 'A329': 'Herrerillo común', 'A330': 'Carbonero común',
    'A331': 'Trepador azul', 'A332': 'Agateador común', 'A333': 'Treparriscos',
    'A334': 'Agateador norteño', 'A335': 'Alcaudón real', 'A336': 'Alcaudón común',
    'A337': 'Oropéndola europea', 'A338': 'Alcaudón dorsirrojo', 'A339': 'Alcaudón chico',
    'A340': 'Alcaudón real', 'A341': 'Arrendajo', 'A342': 'Urraca',
    'A343': 'Piquituerto común', 'A345': 'Chova piquigualda', 'A346': 'Chova piquirroja',
    'A347': 'Grajilla', 'A348': 'Graja', 'A349': 'Corneja negra',
    'A351': 'Estornino pinto', 'A353': 'Estornino negro', 'A355': 'Gorrión común',
    'A356': 'Gorrión moruno', 'A357': 'Gorrión molinero', 'A358': 'Gorrión alpino',
    'A359': 'Pinzón vulgar', 'A360': 'Pinzón real', 'A361': 'Verdecillo',
    'A362': 'Piquituerto', 'A363': 'Verderón común', 'A364': 'Jilguero europeo',
    'A365': 'Pardillo común', 'A366': 'Pardillo común', 'A368': 'Pardillo sizerín',
    'A369': 'Piquituerto común', 'A371': 'Piquituerto común', 'A372': 'Camachuelo común',
    'A373': 'Picogordo', 'A374': 'Escribano hortelano', 'A375': 'Escribano nival',
    'A376': 'Escribano cerillo', 'A377': 'Escribano soteño', 'A379': 'Escribano palustre',
    'A383': 'Escribano montesino', 'A385': 'Petrel de Madeira', 'A387': 'Petrel de Bulwer',
    'A388': 'Pardela chica', 'A389': 'Paíño pechialbo', 'A391': 'Cormorán grande',
    'A392': 'Cormorán moñudo', 'A397': 'Tarro canelo', 'A399': 'Elanio común',
    'A401': 'Azor común', 'A403': 'Busardo moro', 'A405': 'Águila imperial ibérica',
    'A415': 'Perdiz pardilla', 'A416': 'avutarda hubara', 'A419': 'Arao común',
    'A420': 'Urraca', 'A422': 'Paloma turqué', 'A423': 'Paloma rabiche',
    'A424': 'Vencejo unicolor', 'A427': 'Pico picapinos', 'A428': 'Pico mediano',
    'A430': 'Alondra de Dupont', 'A436': 'Collalba tisú', 'A437': 'Tarabilla canaria',
    'A438': 'Zarcero bereber', 'A448': 'Pinzón vulgar', 'A452': 'Camachuelo trompetero',
    'A459': 'Gaviota patiamarilla', 'A464': 'Pardela balear', 'A473': 'Carbonero capuchino',
    'A475': 'Terrera marismeña', 'A478': 'Lúgano', 'A479': 'Golondrina dáurica',
    'A480': 'Pechiazul', 'A481': 'Rabilargo ibérico', 'A485': 'Pinzón azul de Gran Canaria',
    'A486': 'Pinzón azul de Tenerife', 'A487': 'Zarcero pálido', 'A496': 'Herrerillo atlántico',
    'A497': 'Herrerillo capuchino', 'A499': 'Mosquitero papialbo', 'A504': 'Pardela chica',
    'A513': 'Urogallo cantábrico', 'A558': 'Faisán venerado', 'A568': 'Pico de coral común',
    'A569': 'Curruca balear', 'A570': 'Curruca mirlona', 'A572': 'Mosquitero común',
    'A604': 'Gaviota patiamarilla', 'A618': 'Avutarda común', 'A619': 'Azor común',
    'A645': 'Curruca rabilarga', 'A661': 'Urogallo pirenaico', 'A662': 'Arao común',
    'A663': 'Flamenco rosado', 'A675': 'Barnacla carinegra', 'A682': 'Chorlitejo patinegro',
    'A683': 'Cormorán grande', 'A684': 'Cormorán moñudo', 'A687': 'Paloma torcaz',
    'A693': 'Pardela balear', 'A707': 'Águila perdicera', 'A712': 'Perdiz nival',
    'A717': 'Torillo común', 'A722': 'Calamón común', 'A727': 'Chorlito carambolo',
    'A734': 'Fumarel cariblanco', 'A738': 'Avión común', 'A768': 'Zarapito real',
    'A773': 'Garceta grande', 'A850': 'Pardela cenicienta', 'A851': 'Pardela atlántica',
    'A852': 'Pardela sombría', 'A853': 'Pardela capirotada', 'A854': 'Paíño boreal',
    'A855': 'Ánade silbón', 'A856': 'Cerceta carretona', 'A857': 'Pato cuchara',
    'A858': 'Águila pomerana', 'A859': 'Águila moteada', 'A861': 'Combatiente',
    'A862': 'Gaviota enana', 'A863': 'Charrán patinegro', 'A867': 'Pico picapinos',
    'A868': 'Pico mediano', 'A869': 'Pico menor', 'A874': 'Paíño de Madeira',
    'A880': 'Pardela chica', 'A885': 'Charrancito común', 'A889': 'Ánade friso',
    'A892': 'Polluela pintoja', 'A893': 'Polluela pintoja', 'A894': 'Charrán caspio',
    'A900': 'Negrón común', 'A907': 'Curruca sarda',

    # By Scientific Name
    'Otis tarda': 'Avutarda común', 'Accipiter gentilis': 'Azor común',
    'Aquila adalberti': 'Águila imperial ibérica', 'Hieraaetus fasciatus': 'Águila perdicera',
    'Ciconia nigra': 'Cigüeña negra', 'Neophron percnopterus': 'Alimoche común',
    'Milvus milvus': 'Milano real', 'Milvus migrans': 'Milano negro',
    'Circus aeruginosus': 'Aguilucho lagunero', 'Circus cyaneus': 'Aguilucho pálido',
    'Circus pygargus': 'Aguilucho cenizo', 'Aquila chrysaetos': 'Águila real',
    'Hieraaetus pennatus': 'Águila calzada', 'Falco peregrinus': 'Halcón peregrino',
    'Falco tinnunculus': 'Cernícalo vulgar', 'Falco naumanni': 'Cernícalo primilla',
    'Bubo bubo': 'Búho real', 'Tyto alba': 'Lechuza común', 'Athene noctua': 'Mochuelo europeo',
    'Otus scops': 'Autillo europeo', 'Asio otus': 'Búho chico', 'Asio flammeus': 'Búho campestre',
    'Tetrax tetrax': 'Sisón común', 'Burhinus oedicnemus': 'Alcaraván común',
    'Pterocles orientalis': 'Ganga ortega', 'Pterocles alchata': 'Ganga ibérica',
    'Tetrao urogallus': 'Urogallo común', 'Perdix perdix': 'Perdiz pardilla',
    'Alectoris rufa': 'Perdiz roja', 'Coturnix coturnix': 'Codorniz común',
    'Grus grus': 'Grulla común', 'Jynx torquilla': 'Torcecuellos',
    'Phylloscopus ibericus': 'Mosquitero ibérico', 'Turdus merula': 'Mirlo común',
    'Ciconia ciconia': 'Cigüeña blanca', 'Upupa epops': 'Abubilla',
    'Columba palumbus': 'Paloma torcaz', 'Streptopelia turtur': 'Tórtola europea',
    'Cuculus canorus': 'Cuco común', 'Alauda arvensis': 'Alondra común',
    'Parus major': 'Carbonero común', 'Cyanistes caeruleus': 'Herrerillo común',
    'Fringilla coelebs': 'Pinzón vulgar', 'Carduelis carduelis': 'Jilguero europeo',
    'Chloris chloris': 'Verderón común', 'Linaria cannabina': 'Pardillo común',
    'Sylvia atricapilla': 'Curruca capirotada', 'Erithacus rubecula': 'Petirrojo europeo',
    'Apus apus': 'Vencejo común', 'Hirundo rustica': 'Golondrina común',
    'Pernis apivorus': 'Abejero europeo', 'Aegypius monachus': 'Buitre negro',
    'Gyps fulvus': 'Buitre leonado', 'Gypaetus barbatus': 'Quebrantahuesos',
    'Circaetus gallicus': 'Águila culebrera', 'Haliaeetus albicilla': 'Pigargo europeo',
    'Pandion haliaetus': 'Águila pescadora', 'Falco subbuteo': 'Alcotán europeo',
    'Falco columbarius': 'Esmerejón', 'Lullula arborea': 'Totovía',
    'Galerida cristata': 'Cogujada común', 'Galerida theklae': 'Cogujada montesina',
    'Melanocorypha calandra': 'Calandria común', 'Calandrella brachydactyla': 'Terrera común',
    'Chersophilus duponti': 'Alondra de Dupont', 'Anthus campestris': 'Bisbita campestre',
    'Anthus pratensis': 'Bisbita pratense', 'Anthus trivialis': 'Bisbita arbóreo',
    'Motacilla flava': 'Lavandera boyera', 'Motacilla cinerea': 'Lavandera cascadeña',
    'Motacilla alba': 'Lavandera blanca', 'Prunella collaris': 'Acentor alpino',
    'Prunella modularis': 'Acentor común', 'Luscinia megarhynchos': 'Ruiseñor común',
    'Luscinia svecica': 'Pechiazul', 'Phoenicurus ochruros': 'Colirrojo tizón',
    'Phoenicurus phoenicurus': 'Colirrojo real', 'Saxicola rubetra': 'Tarabilla norteña',
    'Saxicola torquatus': 'Tarabilla común', 'Oenanthe oenanthe': 'Collalba gris',
    'Oenanthe hispanica': 'Collalba rubia', 'Oenanthe leucura': 'Collalba negra',
    'Monticola solitarius': 'Roquero solitario', 'Monticola saxatilis': 'Roquero rojo',
    'Turdus torquatus': 'Mirlo capuchino', 'Turdus philomelos': 'Zorzal común',
    'Turdus iliacus': 'Zorzal alirrojo', 'Turdus viscivorus': 'Zorzal charlo',
    'Acrocephalus arundinaceus': 'Carricero tordal', 'Acrocephalus scirpaceus': 'Carricero común',
    'Cisticola juncidis': 'Buitrón', 'Lanius excubitor': 'Alcaudón real',
    'Lanius senator': 'Alcaudón común', 'Lanius collurio': 'Alcaudón dorsirrojo',
    'Oriolus oriolus': 'Oropéndola europea', 'Corvus corax': 'Cuervo común',
    'Pyrrhocorax pyrrhocorax': 'Chova piquirroja', 'Pyrrhocorax graculus': 'Chova piquigualda',
    'Sturnus unicolor': 'Estornino negro', 'Sturnus vulgaris': 'Estornino pinto',
    'Passer domesticus': 'Gorrión común', 'Passer montanus': 'Gorrión molinero',
    'Emberiza calandra': 'Triguero', 'Emberiza cia': 'Escribano montesino',
    'Emberiza hortulana': 'Escribano hortelano', 'Emberiza cirlus': 'Escribano soteño',
    'Emberiza schoeniclus': 'Escribano palustre', 'Anas platyrhynchos': 'Ánade azulón',
    'Anas crecca': 'Cerceta común', 'Anas penelope': 'Ánade silbón',
    'Anas strepera': 'Ánade friso', 'Anas acuta': 'Ánade rabudo',
    'Anas clypeata': 'Pato cuchara', 'Netta rufina': 'Pato colorado',
    'Aythya ferina': 'Porrón europeo', 'Aythya fuligula': 'Porrón moñudo',
    'Aythya nyroca': 'Porrón pardo', 'Oxyura leucocephala': 'Malvasía cabeciblanca',
    'Mergus merganser': 'Serreta grande', 'Mergus albellus': 'Serreta chica',
    'Mergus serrator': 'Serreta mediana', 'Anser anser': 'Ansar común',
    'Anser fabalis': 'Ansar campestre', 'Anser albifrons': 'Ansar careto',
    'Anser erythropus': 'Ansar chico', 'Branta bernicla': 'Barnacla carinegra',
    'Branta leucopsis': 'Barnacla cariblanca', 'Tadorna tadorna': 'Tarro blanco',
    'Tadorna ferruginea': 'Tarro canelo', 'Cygnus olor': 'Cisne vulgar',
    'Ardea cinerea': 'Garza real', 'Ardea purpurea': 'Garza imperial',
    'Egretta garzetta': 'Garceta común', 'Casmerodius albus': 'Garceta grande',
    'Bubulcus ibis': 'Garcilla bueyera', 'Ardeola ralloides': 'Garcilla cangrejera',
    'Nycticorax nycticorax': 'Martinete común', 'Plegadis falcinellus': 'Morito común',
    'Platalea leucorodia': 'Espátula común', 'Phoenicopterus roseus': 'Flamenco rosado',
    'Delichon urbicum': 'Avión común', 'Estrilda astrild': 'Pico de coral común',
}

def main():
    print("Starting strict validation and enrichment of all bird species...")
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    species = cursor.execute("SELECT codigo_n2000, nombre_cientifico, nombre_comun FROM especie").fetchall()
    print(f"Total bird species in DB: {len(species)}")

    for code, sci_name, current_common in species:
        common = SEO_COMPLETE_BIRD_NAMES.get(code)
        if not common:
            common = SEO_COMPLETE_BIRD_NAMES.get(sci_name)
        if not common:
            binomial = " ".join(sci_name.split()[:2])
            common = SEO_COMPLETE_BIRD_NAMES.get(binomial)

        if common and common.lower() != sci_name.lower():
            cursor.execute("UPDATE especie SET nombre_comun = ? WHERE codigo_n2000 = ?", (common, code))

    conn.commit()

    # Strict Validation Check
    invalid_rows = cursor.execute("""
        SELECT codigo_n2000, nombre_cientifico, nombre_comun
        FROM especie
        WHERE nombre_comun IS NULL
           OR nombre_comun = ''
           OR LOWER(nombre_comun) = LOWER(nombre_cientifico)
           OR nombre_comun = codigo_n2000
    """).fetchall()

    if invalid_rows:
        print(f"\n[ERROR] Validation failed! Found {len(invalid_rows)} species with missing or equal common names:")
        for r in invalid_rows[:15]:
            print(r)
        raise AssertionError("Database species common name validation failed!")
    else:
        print("\n[SUCCESS] Strict validation passed! 100% of bird species have a valid, distinct Spanish common name.")

    conn.close()
    shutil.copy(DB_PATH, 'scripts/censozepa.db')
    print("Database copied to scripts/censozepa.db successfully.")

if __name__ == '__main__':
    main()
