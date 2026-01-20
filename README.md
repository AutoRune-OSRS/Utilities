# AutoRune Utilities

A comprehensive shared library providing utilities and helpers used across multiple AutoRune modules. Contains ASM bytecode extensions, file system utilities, scene/coordinate mathematics, deobfuscation transformers, and client-specific resources.

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 1.3.61 / Java 11 |
| Build System | Maven |
| Bytecode | ASM 7.1 (full suite) |
| Serialization | Jackson 2.11.3 (JSON, Properties) |
| Collections | Google Guava 28.1-jre |
| HTML Parsing | JSoup 1.12.1 |
| Logging | SLF4J 1.7.26 + Logback 1.2.3 |
| UI Extensions | kxtra-swing 1.0.3 |
| Text Processing | pfsw-text 8.2.0 |

## Architecture

```
Utilities/
├── io.autorune.utilities.asm/       # ASM bytecode extensions
├── io.autorune.utilities.jar/       # JAR loading and downloading
├── io.autorune.utilities.preferences/  # Directory management
├── io.autorune.utilities.resource/  # Jagex resource fetching
├── io.autorune.utilities.scene/     # 3D scene and coordinate math
└── org.runestar.client.updater.deob/  # Deobfuscation transformers
```

## Modules

### 1. ASM Extensions

Powerful bytecode pattern matching and instruction navigation utilities.

**Assembly.java** - Instruction navigation:
```java
// Find previous instruction matching mask within distance
prev(AbstractInsnNode node, int distance, Mask mask)

// Find next instruction matching mask
next(AbstractInsnNode node, int distance, Mask mask)

// Chain-find sequence of instructions
next(AbstractInsnNode node, Mask[] masks)

// Search across classes/methods
find(ClassNode classNode, Mask[] masks)
findAll(MethodNode[] methods, Mask[] masks)
```

**Mask.java** - Bytecode pattern matching:
- Extends `BigInteger` for efficient opcode bitmask operations
- **170+ predefined opcode masks**: NOP, ACONST_NULL, ICONST_*, LDC, LOAD/STORE, arithmetic, branching
- **Matching attributes**:
  - `distance` - Search distance threshold
  - `owner` - Class owner filter
  - `desc` - Method/field descriptor filter
  - `operand` - Specific operand value
  - `descContains` - Substring matching
- **Chainable builders**: `.distance()`, `.own()`, `.describe()`, `.operand()`

**Wildcard.java** - Method signature pattern matching:
```java
// Match "(II)V" against "(I?)?*"
Wildcard.matches("(II)V", "(I?)?")
```

### 2. JAR Utilities

**JarDownloader.kt** - Downloads JAR files from remote locations:
- `fetchJar(uri: String)` - Download jar, compute hash, return path
- Custom HTTP headers mimicking Firefox browser
- Buffered streaming (1024 byte chunks)
- Stores in vanilla gamepack directory

**JarLoader.kt** - Loads and parses JAR files:
- `loadJar(Path)` - Extract all `.class` entries into ClassNode objects
- Extracts client revision from bytecode patterns:
  - Searches for SIPUSH opcodes (765, 503 - applet dimensions)
  - Identifies `initializeApplet` invocations
  - Retrieves version from next IntInsnNode operand

### 3. System Preferences

**SystemPreferences.kt** - Centralized directory management:

| Method | Path |
|--------|------|
| `getAutoruneDirectory()` | `~/.autorune` |
| `getGamepackDirectory()` | `.autorune/gamepacks` |
| `getVanillaGamepackDirectory()` | `.autorune/gamepacks/vanilla` |
| `getScriptsDirectory()` | `.autorune/scripts` |
| `getCharacteristicDirectory()` | `.autorune/characteristics` |
| `getResourcesDirectory()` | `.autorune/resources` |
| `getInjectedJarLocation()` | `.autorune/gamepacks/injected.jar` |

Auto-creates directories on first access.

### 4. Client Resources

**Data Classes:**
- `GamePack(hash: Long, url: String)` - Downloadable game client
- `World(id, members, address, activityType, countryFlag, playerCount)` - RuneScape world metadata
- `JagexResource(address, html, gamePack, parameters)` - Complete client resource bundle

**JagexResourceFetcher.kt** - Fetches Jagex client information:
- `getJagexResource(members: Boolean)` - Main entry point
- Selects best world (lowest latency)
- Parses HTML for applet parameters
- Extracts gamepack hash from comment: `archive=gamepack_[hash].jar`

**WorldFetcher.kt** - Retrieves RuneScape world list:
- `getWorld(members: Boolean)` - Get best-fit world
- `getWorldList()` - Full world list from `runescape.com/g=oldscape/slr.ws`
- `getBestWorld(members)` - Lowest latency selection
- Binary protocol parsing via ByteBuffer

**RSByteBufferUtils.kt** - Binary data parsing:
- `readRS2String(ByteBuffer)` - Read null-terminated ASCII strings

### 5. Scene & Coordinate Utilities

3D rendering mathematics and coordinate transformations.

**LocalPoint.kt** - 2D client-local coordinates:
```kotlin
// Coordinates in 1/128th tile units
val point = LocalPoint(x, y)
point.distanceTo(other)      // Euclidean distance
point.sceneX                 // Convert to tile coordinates
LocalPoint.fromWorld(plane, baseX, baseY, WorldPoint)
LocalPoint.fromScene(sceneX, sceneY)
```

**WorldPoint.kt** - 3D world coordinates:
```kotlin
val point = WorldPoint(x, y, plane)
point.dx(offset)             // Offset builders
point.distanceTo(other)      // 3D distance (INT.MAX if different planes)
point.distanceTo2D(other)    // Chebyshev distance ignoring plane
point.regionID               // 16-bit region identifier
WorldPoint.isInScene(...)    // Check if within viewport
WorldPoint.fromLocal(...)    // Convert from local
WorldPoint.rotate(...)       // Chunk rotation
```

**Perspective.kt** - 3D-to-2D projection:
- Precomputed sine/cosine tables (2048 entries, fixed-point)
- `modelToCanvas()` - Full 3D rendering pipeline:
  - Camera pitch/yaw/position handling
  - Viewport dimensions and offset
  - Model vertex transformation
  - Perspective division
- `fetchTileHeightFromLocal()` - Terrain height interpolation:
  - Bilinear interpolation on tile height grids
  - Bridge detection (TILE_FLAG_BRIDGE)

**Constants:**
- `LOCAL_COORD_BITS = 7`
- `LOCAL_TILE_SIZE = 128`
- `SCENE_SIZE = 104`
- `MAX_Z = 4`

**Jarvis.kt** - Convex hull computation:
- Jarvis march (gift wrapping) algorithm O(n*h)
- `convexHull(xs[], ys[])` - Compute hull from point arrays
- Handles invalid/sentinel points

**SimplePolygon.kt** - Mutable polygon container:
- Dynamic vertex arrays with left/right expansion
- `intersectWithConvex()` - Sutherland-Hodgman clipping
- `contains()` - Point-in-polygon via ray casting
- Java Shape interface implementation

### 6. Deobfuscation Transformers

An 18-stage pipeline for transforming obfuscated bytecode.

**Transformer Pipeline (DEFAULT):**

| Stage | Transformer | Purpose |
|-------|-------------|---------|
| 1 | `FrameRemover` | Remove stack frame information |
| 2 | `FieldResolver` | Narrow field instruction owners |
| 3 | `UnusedTryCatchRemover` | Remove dead try-catch blocks |
| 4 | `ControlFlowFixer` | Reorder instruction blocks |
| 5 | `OpaquePredicateCheckRemover` | Remove obfuscation predicates |
| 6 | `UnusedFieldRemover` | Remove unused fields |
| 7 | `RemoveErrorConstructors` | Remove error handling noise |
| 8 | `UnusedMethodFinder` | Identify unused methods |
| 9 | `UnusedMethodRemover` | Remove unused methods |
| 10 | `OpaquePredicateArgumentRemover` | Remove predicate arguments |
| 11 | `DuplicateMethodFixer` | Resolve duplicate methods |
| 12 | `UnnecessaryGotoRemover` | Remove redundant jumps |
| 13 | `SortFieldsByModifiers` | Organize fields |
| 14 | `SortMethodsByLineNumber` | Organize methods |
| 15 | `DebugRemover` | Strip debug information |
| 16 | `RemoveEnclosingMethodAttributes` | Clean attributes |
| 17 | `MultiplierFinder` | Identify field encoding multipliers |
| 18 | `MultiplierFixer` | Apply multiplier corrections |

**Key Transformers:**

**FieldResolver.kt** - Narrows field instruction owners per JVMS 5.4.3.2:
- Walks inheritance hierarchy to find actual field declarations
- Distinguishes static vs instance fields

**ControlFlowFixer.kt** - Analyzes and reorders control flow:
- Tracks block predecessors/successors
- Uses BlockAnalyzer for instruction segmentation
- Eliminates unnecessary jumps

**MultiplierFinder.kt** - Detects field encoding multipliers:
- Analyzes patterns: `DUP/DUP2` + `PUT*` + `LDC` + `IMUL/LMUL`
- Identifies decoder multipliers (invertible odd integers)
- Unfolds dependent multipliers through field chains
- Outputs Map<String, Number> of field multipliers

**Utility Classes:**
- `AsmTreeJar.kt` - `readJar()`, `writeJar()` for JAR I/O
- `Inversion.kt` - `invert()`, `isInvertible()` for modular arithmetic
- `Block.kt`, `BlockAnalyzer.kt` - Control flow analysis

### 7. Random Extensions

**RandomExt.kt** - Kotlin SecureRandom extensions:
```kotlin
secureRandom.nextDouble(low, high)  // Random in range with weighting
```

## Usage

Published as Maven artifact:
```xml
<dependency>
    <groupId>io.autorune</groupId>
    <artifactId>utilities</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Primary Consumers

| Module | Utilities Used |
|--------|----------------|
| Updater/Deobfuscator | ASM extensions, transformers |
| Game Client | Scene coordinates, resources |
| Bot Framework | Preferences, random utilities |
| Injection System | JAR loading/manipulation |

## Building

```bash
mvn clean install
```

## Key Design Principles

- **Shared Foundation** - Single source of truth for common utilities
- **Type-Safe Bytecode** - ASM extensions with strong patterns
- **Efficient Math** - Fixed-point arithmetic and precomputed tables
- **Cross-Platform** - Platform-agnostic file system handling
