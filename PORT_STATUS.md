# Estado del porte

Objetivo: portar Create Diesel Generators 1.3.15 a Minecraft 1.20.1 Forge,
usando Create 0.5.1.j y el Flywheel incluido en esa versión.

## Estado actual

- La base del contenido 1.3.12 para Forge 1.20.1 está integrada.
- Los registros, bloques, entidades, recetas, fluidos, menús, herramientas,
  proyectiles, configuraciones y recursos principales usan los paquetes de la
  versión moderna del mod.
- Se adaptaron los renderizadores avanzados al API de Create/Flywheel de la
  instalación objetivo: motores diésel, motor grande, bomba, tanque de
  destilación, fermentador, quemador, canister, torreta, tapa del basin,
  filtros y herramientas.
- Se añadió el renderizado clásico de respaldo del motor diésel grande para
  cuando Flywheel no puede usar instancing.
- Se corrigieron las dependencias para Create 0.5.1.j y la metadata Forge.
- `compileJava` y `build` terminan correctamente con Java 17.

## Compatibilidad comprobada

- Minecraft: 1.20.1 Forge
- Create: 0.5.1.j
- Flywheel: 0.6.11-13, incluido por Create
- Build: `compileJava` correcto

## Verificación final

- Reincorporado y adaptado el plugin/categorías JEI, incluyendo fermentación,
  destilación, casting, moldes, hammering y wire cutting.
- Reincorporadas las escenas y registros Ponder compatibles con el Ponder de
  Create 0.5.1.j.
- Recuperadas las integraciones de barril montado mediante mixin y los display
  sources de bomba y motores.
- Revisar si los valores de estrés necesitan un proveedor explícito adicional
  tras la prueba dentro del juego.
- Build completo ejecutado correctamente (`./gradlew build` con Java 17).
- JAR final auditado: metadata Forge/Create correcta y clases de JEI, Ponder,
  display source y mounted storage presentes.
- JAR instalado en `/home/Santipdr/.sklauncher/instances/test-1/mods/`; el hash
  coincide con `build/libs/`.
- JAR instalado también en `/home/Santipdr/.sklauncher/instances/siege/mods/`;
  la copia coincide con el build y conserva Create `0.5.1.j` y Flywheel
  `0.6.11-13` de esa instancia.
- Se verificó un arranque real de Forge 47.4.10 usando la instancia `test-1`:
  el mod llegó a la recarga de recursos y no produjo errores de carga de
  clases ni crash del mod. Se corrigieron los blockstates que todavía
  referenciaban las propiedades `silenced`, `turbocharged` y `north` de la
  versión antigua. Permanecen avisos visuales menores de texturas de fluidos
  y avisos de otros mods de la instancia, sin fallo de carga de Diesel
  Generators.
- Se verificó además el arranque real con el conjunto completo de mods de
  `siege`: Forge llegó al cliente y a la recarga de recursos, y Diesel
  Generators registró sus configuraciones sin errores de dependencias. Los
  errores restantes del log corresponden a mixins/modelos opcionales de otros
  mods de `siege` y a servicios externos sin conexión.

No se ha cambiado el JAR de Create ni las dependencias de las instancias.
