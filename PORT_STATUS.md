# Estado del port: 1.3.12 → Minecraft 1.20.1

## Objetivo y base

- Create Diesel Generators: **1.3.12**, sobre la base suministrada `9aa47e2`.
- Minecraft: **1.20.1**, mappings oficiales.
- Forge de compilación y pruebas: **47.1.30**.
- Create: **0.5.1.j**; Flywheel **0.6.11-13**; Registrate **MC1.20-1.3.3**.
- Java: **17**; Gradle wrapper **7.5.1**; ForgeGradle **5.1**.
- Rama de trabajo: `arena/01a0f970-create-diesel-generators`.

Se conservaron las correcciones válidas de la base. El objetivo no es 1.3.15,
Create 6 ni Minecraft 1.21. Las verificaciones actuales se ejecutan en GitHub
Actions: el entorno de edición de Arena no dispone de JDK local.

## Correcciones implementadas

### API, dependencias y ciclo de vida

- Sustituidos imports de Catnip moderno por las clases reales de
  `com.simibubi.create.foundation` presentes en los JAR suministrados.
- Usado `CreateClient.OUTLINER` y conservados los display sources, escenas
  Ponder, categorías/animaciones JEI e integraciones opcionales existentes.
- Eliminada la dependencia de Ponder independiente: 0.5.1.j incluye su API
  Ponder dentro de Create.
- Crafts & Additions de desarrollo pasa de 1.3.0 a **1.2.5**: el primero rechazó
  el arranque porque exige Create ≥6.0.1; el changelog del segundo confirma
  soporte para 0.5.1.j. No se cambiaron las versiones objetivo del port.
- Acotada la metadata a Minecraft 1.20.1 y Create 0.5.x desde 0.5.1.j.
- Trabajo cliente no seguro en paralelo trasladado a `enqueueWork`.
- Desactivado `forceExit` de ForgeGradle 5, que terminaba el daemon de Gradle
  incluso después de completar correctamente los GameTests.

### Recursos y empaquetado

- Overlay explícito: recursos manuales de `src/main/resources` prevalecen sobre
  los generados; otros duplicados hacen fallar el build. `.cache` no se empaqueta.
- Añadidas **188** claves inglesas y **23** entradas de pickaxe que estaban
  ocultas por recursos manuales. Se conservan los valores ingleses anteriores.
- Corregidos cuatro parents `cube_all`, referencias al atlas de tres fluidos
  y bindings `#missing` en los modelos medios de destilación.
- Cantidades de ingredientes expresadas mediante entradas repetidas, como
  requiere el serializer de procesamiento de Create 0.5.1.j.
- Eliminadas únicamente propiedades obsoletas de dos paletas Ponder; no se
  reescribieron sus DataVersions ni el resto del NBT.
- Verificación del JAR efectivo, duplicados, manifiesto generado, configuración
  de mixins y refmap no vacío.

### Mixins, renderizado y comportamiento

- Cuatro shadows/accessors de miembros propios de Create usan `remap=false`;
  los miembros de Minecraft conservan su remapeo.
- Se preserva `List<LootPool>` en el accessor de Forge: Forge modifica el array
  de vanilla, por lo que cambiarlo a array sería incorrecto.
- Registradas seis factories reales de Flywheel que faltaban: motores normal,
  modular y enorme, eje alimentado, bearing y crank de la bomba.
- El pistón enorme usa el ángulo real `getAngleForTe` en lugar de cero.
- Renderizador e instancia del crank comparten la geometría de la biela,
  incluyendo su conexión al bearing. Corregida la orientación vertical del
  conector enorme en el modo de respaldo.
- Capacidad de motores vacíos o con multiplicador de velocidad cero: **0**,
  no `NaN`. Se mantiene la fórmula de potencia de los motores con combustible.
- Flags de habilitación de motores y multiplicador de consumo turbo aplicados.
- Persistencia de la posición de la torreta en `saveWithoutId`/guardado de
  pasajeros, y eliminación de estado obsoleto al cargar NBT sin esa posición.
- El tinte no se consume en copycats con materiales ajenos al barril de aceite.
- El mechero comprueba combustible antes de encender bloques; encender un
  creeper comunica una interacción consumida y no acepta agua como combustible.
- Filtro: slot fantasma correcto, clave NBT de modo coherente, IDs inválidos y
  atributos duplicados rechazados; solucionado el registro nulo por iniciar
  primero el enum de atributos. Decodificación de paquetes validada.
- Bolsa de vías: cantidades enteras sin overflow del byte `Count`, lectura de
  bolsas antiguas, colocaciones fallidas sin pérdida y delegación real a Create
  para selección/conexión de vías. La bolsa se restaura incluso ante excepciones.
- Índice inverso de loot reconstruido como snapshot después de cargar loot,
  sin carrera con su fase asíncrona; sincronización al cliente al entrar y
  recargar datapacks, necesaria para filtros por drops en multijugador.

## Diferencias de compatibilidad explícitas

1. **Pulp:** Create 0.5.1.j no registra `create:pulp` ni `create:pulpifiable`.
   La receta de fermentación de fibras vegetales se conserva con salida
   `minecraft:paper` y tag propio `createdieselgenerators:pulpifiable`
   (bambú, caña y saplings). Se mantienen duración y probabilidades de salida.
2. **Networking:** protocolo del canal propio **4**. Se añade el índice de loot
   servidor→cliente; cliente y servidor deben instalar esta misma revisión.
   El formato `int + NBT` del paquete del filtro se conserva.
3. **Mounted storage:** 0.5.1.j no tiene el registro de storage types moderno.
   El soporte real de barriles sigue mediante `MountedFluidStorageMixin`,
   comprobado con serialización y desmontaje. Se eliminó el shim vacío, no el soporte.

## Verificación reproducible

```bash
python3 scripts/verify_create_api.py
python3 scripts/verify_mixins.py
python3 scripts/verify_resources.py
./gradlew --no-daemon --console=plain clean compileJava build
python3 scripts/verify_resources.py --jar build/libs/createdieselgenerators-1.20.1-1.3.12-create-0.5.1j.jar
./gradlew --no-daemon --console=plain runGameTestServer
./gradlew --no-daemon --console=plain -PportTests runClient
```

Los tests no entran en el JAR de distribución. GameTests usa `run-gametest/`;
la prueba cliente opt-in usa `run-client-smoke/`, crea su propio mundo y se
cierra al terminar. Preparación de EULA, opciones de cliente y Xvfb para CI
están en `.github/workflows/port.yml`.

La suite contiene **20 GameTests requeridos**: targets de mixins comunes,
recetas core, registry de combustibles, barril montado, moldes, filtros y
paquetes, capacidad/configuración de motores, persistencia, tintes, mechero,
bolsa de vías e índice de loot. Un lanzamiento con cero tests no se acepta.

La prueba cliente comprueba modelos de items y estados de bloques, sprites
block/item en el atlas, moldes, factories de instancing, animación del motor
enorme, mundo con Flywheel INSTANCING y OFF, **11 storyboards Ponder**
compilados/tickeados, sincronización del índice de loot y apertura/renderizado
real del menú de filtro por Forge.

Los resultados y el JAR quedan como artifacts de cada ejecución del workflow.

## Resultados verificados

La revisión funcional `e7a8aca` pasó [la ejecución completa 36942461051](https://github.com/Santi-PdR/Create---Diesel-Generators/actions/runs/36942461051):

- `compileJava` y build reobfuscado con Java 17: **correctos**.
- **746 imports** contrastados con los JAR reales; **12 firmas exactas** de
  Create y los **16 mixins configurados** revisados.
- **1008 recursos efectivos**, **342 modelos**, **135 recetas**: validación
  estática y overlay empaquetado correctos.
- **20/20 GameTests requeridos**: correctos.
- Prueba cliente con mundo, ambas rutas de renderizado, animación, **11 escenas
  Ponder**, sincronización de loot y menú por Forge: **correcta**.

El workflow de cierre vuelve a ejecutarlo desde outputs limpios y exige además
que no se empaqueten los tests ni sus estructuras.

## Alcance y evidencia histórica

La compilación y las pruebas automatizadas no equivalen a una campaña manual
completa ni prueban todas las combinaciones de addons, configuraciones, curvas
de vías o contraptions multibloque. El cliente automatizado usa OpenGL software;
no certifica cada GPU o backend gráfico externo. El índice inverso conserva
el alcance de entradas de loot directas de la implementación suministrada.

Las pruebas antiguas con las instancias locales `test-1`/`siege` y Forge 47.4.10
pertenecen al estado previo suministrado. No se reinstalaron esas instancias
ni se presentan como evidencia de esta revisión en Arena.
