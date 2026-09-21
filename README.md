# Censo ZEPA — Sistema Móvil de Censos Ornitológicos en la Red Natura 2000

## Memoria Metodológica y Rigor Científico de la Base de Datos

El presente documento detalla la fundamentación metodológica, la procedencia institucional y el proceso de construcción ETL (Extract, Transform, Load) de la base de datos relacional integrada en la aplicación móvil **Censo ZEPA**. 

Dada la naturaleza científica del proyecto y la necesidad de garantizar una trazabilidad bio-geográfica rigurosa en el seguimiento de Zonas de Especial Protección para las Aves (ZEPAs), todo el sistema de información se basa exclusivamente en los registros oficiales consolidados de la **Red Natura 2000**.

---

## 1. Procedencia Institucional y Fuentes Oficiales

La fuente primaria de datos alfanuméricos y geográficos que alimenta el motor SQLite de la aplicación (`censozepa.db`) procede de los repositorios públicos oficiales de la Unión Europea y del Estado Español:

* **Agencia Europea de Medio Ambiente (EEA) / EIONET & Comisión Europea:** 
  Se ha empleado la base de datos relacional consolidada de la Red Natura 2000 (*Natura 2000 Public Database*), la cual unifica e integra de forma estandarizada la totalidad de la información contenida en los Formularios Normalizados de Datos (*Standard Data Forms* - SDF) individuales de los Estados miembros.
* **Fecha de Referencia y Versión del Dataset Oficial:**
  * **Fecha de Publicación Institucional:** **4 de enero de 2023 (`20230104`)**.
  * **Ciclo Oficial Consolidado:** Datos oficiales correspondientes al cierre de **diciembre de 2021 (`end2021`)** para el territorio de España (`ES`).
* **MITECO (Banco de Datos de la Naturaleza):**
  Conforme a la Ley 42/2007 del Patrimonio Natural y de la Biodiversidad, estos registros integran los formularios SDF estatales remitidos por las comunidades autónomas al Ministerio para la Transición Ecológica y el Reto Demográfico.

---

## 2. Estructura Relacional y Arquitectura de Datos

La base de datos maestra de la aplicación (`censozepa.db`) se deriva directamente del volcado relacional original en formato Microsoft Access (`.accdb`), manteniendo las relaciones normalizadas entre entidades clave:

1. **`NATURA2000SITES` (Tabla `zepa` y `ccaa`):**
   Contiene el inventario completo de los espacios protegidos clasificados como ZEPA en España. Almacena el código oficial Natura 2000 (ej. `ES0000365`), la denominación oficial del espacio, la superficie geográfica en hectáreas (`AREAHA`), la adscripción regional (NUTS / CCAA) y las memorias descriptivas e importancias ecológicas extraídas de las secciones de calidad y características del SDF.
2. **`SPECIES` (Tabla `especie` y `fenologia_zepa` - Sección 3.2):**
   Agrupa las especies de aves de la Directiva Aves (Directiva 2009/147/CE) referidas en el Artículo 4 (especies del Anexo I y especies migratorias regulares), vinculando rigurosamente cada espacio con sus taxones evaluados, categorías de población (`POPULATION`), conservación (`CONSERVATION`), categorías de abundancia (`A`, `B`, `C`, `D`) y estatus fenológico mensual (Enero–Diciembre).
3. **`OTHERSPECIES` (Sección 3.3):**
   Integra las especies de avifauna y fauna relevante adicionales declaradas en las fichas oficiales.

---

## 3. Protocolo de Procesamiento y Estandarización Científica

Para garantizar la interoperabilidad en campo y el rigor taxonómico, se ha aplicado el siguiente pipeline automatizado de procesamiento mediante scripts de extracción (`scripts/`):

* **Filtrado Exclusivo de Avifauna:** 
  Se depuran y filtran estrictamente los taxones pertenecientes a la clase *Aves*, excluyendo otros grupos faunísticos o florísticos presentes en el dataset general de la Red Natura 2000, asegurando la coherencia biológica con el ámbito de las ZEPAs.
* **Nomenclatura Taxonómica y Vernácula (SEO/BirdLife):**
  Se cruzan los nombres científicos oficiales (`SPECIESNAME`) del estándar europeo con la nomenclatura ornitológica oficial en castellano avalada por **SEO/BirdLife**, garantizando que cada registro muestre simultáneamente el nombre común en negrita y el nombre científico en cursiva.
* **Mantenimiento y Actualización Futura (Pipeline ETL):**
  La arquitectura del proyecto en el directorio `scripts/` permite que, ante futuras actualizaciones del Banco de Datos de la Naturaleza del MITECO o de la EEA, la base de datos maestra (`censozepa.db`) pueda ser íntegramente regenerada y sincronizada de forma automatizada mediante scripts relacionales en Python.
