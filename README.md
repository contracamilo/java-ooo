# PLAY · Java OOP learning project

Simulador de plataforma de streaming en consola, hecho en **Java 21 puro** (sin Maven/Gradle ni dependencias) para
aprender programación orientada a objetos y las herramientas modernas del lenguaje. Cada concepto está aplicado
sobre un problema real del proyecto (catálogo de películas y documentales) y comentado en el código.

## Cómo ejecutarlo

```bash
javac -d out $(find src -name "*.java")   # compilar
java -cp out kelea.play.Main              # ejecutar desde la raíz del proyecto
```

El catálogo se lee de `data/movies.csv` al arrancar y se actualiza al agregar o borrar contenido. La ruta es relativa
a la carpeta desde la que se ejecuta, por eso hay que lanzarlo desde la raíz.

## Estructura

```
src/kelea/play/
├── Main.java, MenuOption.java       menú de consola
├── content/                         dominio: Content, Movie, Documentary, enums, Promotable
├── platform/                        Platform (catálogo) y records CatalogStats, DurationRange
├── repository/                      ContentRepository (interfaz) y FileContentRepository
├── util/                            ScannerUtils, ConsoleUtils, FileUtils
├── exceptions/                      ExistingContentException
├── User/                            clase User
└── codeTesting/                     demos pequeñas, un concepto por archivo
```

## Conceptos de Java, de lo general a lo concreto

### 1. Programación orientada a objetos

| Concepto | Dónde verlo | Idea |
|---|---|---|
| **Clases y encapsulación** | `User`, `Content` | Campos `private`, acceso por métodos; el objeto protege su estado. |
| **Herencia** | `Content` → `Movie`, `Documentary` | Lo común vive en la clase base; cada hija añade lo suyo (`director`, `topic`, `narrator`). |
| **Clases abstractas** | `Content`, `codeTesting/AbstractDemo` | No se puede hacer `new Content(...)`; los métodos `abstract` obligan a cada hija a implementarlos. |
| **Clases `sealed`** | `Content permits Movie, Documentary` | El compilador conoce todas las subclases; los `switch` no necesitan `default`. |
| **Polimorfismo** | `content.play()`, `details()`, `type()` | Misma llamada sobre un `Content`, comportamiento distinto según el objeto real. `Platform` y la tabla nunca preguntan de qué tipo es. |
| **Interfaces** | `Promotable`, `ContentRepository`, `codeTesting/InterfaceDemo` | Definen capacidades o contratos. `extends` dice qué *es*; `implements`, qué *puede hacer*. |
| **Métodos `default`** | `Promotable.banner()` | Una interfaz puede traer código ya escrito. |
| **Tipo de retorno covariante** | `Content.withAvailable` | Cada hija devuelve su propio tipo (`Movie withAvailable(...)`). |
| **Inmutabilidad** | `Content`, records | Campos `final`, sin setters; para "cambiar" algo se crea una copia. |
| **Inversión de dependencias** | `ContentRepository` ← `FileContentRepository` ← `Main` | `Main` solo conoce la interfaz; cambiar el almacenamiento no toca el resto. |

### 2. Tipos de datos propios

| Concepto | Dónde verlo | Idea |
|---|---|---|
| **Enums** | `Genre`, `Language`, `Quality`, `ContentType`, `MenuOption` | Conjunto cerrado de valores con campos y constructor. `MenuOption` genera el menú y los números a partir del orden de declaración. |
| **Records** | `CatalogStats`, `DurationRange`, `codeTesting/RecordDemo` | Datos inmutables; el compilador genera constructor, accesores, `equals`, `hashCode` y `toString`. |
| **Constructor compacto** | `DurationRange`, `CatalogStats` | Valida antes de asignar los campos. |
| **`equals` y `hashCode`** | `Content`, `codeTesting/SlotDemo` | Deben usar los mismos campos; sin ellos un objeto no funciona como clave de un `Map`. |
| **Excepciones propias (checked)** | `ExistingContentException` | El compilador obliga a manejar el caso "contenido duplicado". |

### 3. Colecciones y programación funcional

| Concepto | Dónde verlo | Idea |
|---|---|---|
| **`Map`** | `Platform`, `codeTesting/MapDemo` | Catálogo indexado por título (`LinkedHashMap`), contador de vistas (`HashMap`). |
| **`putIfAbsent`, `merge`, `getOrDefault`** | `Platform.addContent`, `play`, `viewsOf` | Operaciones clave-valor sin `if` manuales. |
| **Streams** | `Platform` | `filter`, `sorted`, `limit`, `min`/`max`, `mapToLong`, `average`, `collect(toMap)`. |
| **Lambdas y referencias a métodos** | `Content::title`, `Promotable.class::isInstance` | Atajo cuando la lambda solo llama a un método. |
| **`Comparator`** | `Platform.topRated`, `moviesByNote` | `comparing(...).reversed().thenComparing(...)`. |
| **`Optional`** | `shortestMovie`, `durationRange`, `play` | Obliga a manejar el caso "no hay resultado" en lugar de devolver `null`. `map`, `flatMap`, `ifPresentOrElse`, `orElse`. |
| **Interfaces funcionales** | `FileUtils.replaceLines` | `Predicate<String>` y `UnaryOperator<String>` como parámetros. |

### 4. Entrada/salida

| Concepto | Dónde verlo | Idea |
|---|---|---|
| **NIO.2 (`Path`, `Files`)** | `FileUtils`, `FileContentRepository` | `readAllLines`, `write`, `move`, `createDirectories`, `deleteIfExists`. |
| **Escritura atómica** | `FileUtils.writeLines` | Se escribe en un `.tmp` y se mueve con `ATOMIC_MOVE`: un fallo no deja el archivo a medias. |
| **`SeekableByteChannel`** | `FileUtils.endsWithLineBreak` | Leer solo el último byte sin cargar el archivo. |
| **`try-with-resources`** | `FileUtils` | Cierra el recurso automáticamente. |
| **`IOException`** | `ContentRepository` | Excepción checked: el disco puede fallar por causas externas. |
| **Entrada validada** | `ScannerUtils` | Reintenta hasta recibir un número o un valor de enum válido. |
| **Salida con formato** | `ConsoleUtils` | `printf`/`String.format` para tablas alineadas (`%-28s`, `%8s`, `%.1f`). |

### 5. Detalles modernos del lenguaje

| Concepto | Dónde verlo |
|---|---|
| **`switch` como expresión y `yield`** | `Main` (crear `Movie` o `Documentary` según el tipo) |
| **Pattern matching en `switch`** | `FileContentRepository.format` (`case Movie m`, `case Documentary d`) |
| **Pattern matching en `instanceof`** | `Content.equals` |
| **Genéricos** | `ScannerUtils.catchEnum`: `<E extends Enum<E>> E catchEnum(String, Class<E>)` |
| **Clases utilitarias** | `ConsoleUtils`, `FileUtils`, `ScannerUtils`: solo miembros estáticos y constructor privado |
| **Anidados** | `ContentRepository.LoadResult` (record dentro de una interfaz) |

## Formato de `data/movies.csv`

```
title;genre;language;quality;duration;note[;type[;director | topic[;narrator]]]

Thor;SCI_FI;ENGLISH;FULL_HD;120;4.2                                             (6 campos: película antigua)
Oppenheimer;DRAMA;ENGLISH;UHD_4K;180;4.6;MOVIE;Christopher Nolan
Planet Earth;ACTION;ENGLISH;UHD_4K;50;4.9;DOCUMENTARY;Nature;David Attenborough
```

Las líneas vacías y las que empiezan con `#` se ignoran; una línea inválida se salta con un aviso, sin detener la carga.

## Historial

Los commits están organizados **por concepto, de lo general a lo concreto** (no en el orden cronológico real en que
se desarrolló el proyecto): estructura, tipos, OOP, excepciones, records, colecciones y streams, E/S con NIO,
genéricos, formato y, al final, la aplicación que lo une todo. Cada commit compila por sí solo.
