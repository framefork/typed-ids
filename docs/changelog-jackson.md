# Changelog research: Jackson

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/FasterXML/jackson` wiki — `Jackson-Release-2.19`, `-2.20`, `-2.21`, `-2.22`, `-3.0`, `-3.1`, `-3.2`, `Jackson-Releases`
> - `github.com/FasterXML/jackson-bom` git tags (release-date verification, `git log -1 --format=%ci jackson-bom-<v>`)
> - `github.com/FasterXML/jackson-databind` source — diffed tags `jackson-databind-2.18.1` (our current) vs `jackson-databind-3.0.0` for the exact base-type renames/signatures our two modules extend
> - `github.com/FasterXML/jackson-core` source — tag `jackson-core-3.0.4` for the streaming-method signatures we call
> - `github.com/spring-projects/spring-boot` wiki — `Spring-Boot-4.0-Release-Notes`, `Spring-Boot-4.0-Migration-Guide` (Boot↔Jackson coupling)
> - typed-ids source: `modules/typed-ids/src/main/java/org/framefork/typedIds/{uuid,bigint}/json/jackson/*`, `modules/typed-ids/build.gradle.kts`, `gradle/libs.versions.toml`

## TL;DR for typed-ids

- We target **Jackson 2.18.1** today (`gradle/libs.versions.toml`: `jackson = "2.18.1"`), pulled in as `compileOnly` (`jackson-databind`, `jackson-annotations` declared but only databind is actually wired into the build). Spring Boot is pinned at **3.5.8**, which lives entirely on the Jackson **2.x** line.
- The whole **2.19 → 2.22** range is a **non-event for us**: nothing we import was deprecated, renamed, or removed. The APIs our two modules consume (`Module`, `Serializers.Base`, `Deserializers.Base`, `JsonSerializer`/`JsonDeserializer`, `JsonGenerator`/`JsonParser`, `Version`) are byte-for-byte source-compatible across all of 2.x up to and including **2.22.0** (released 2026-05-31, the last planned 2.x minor). **2.21 is the 2.x LTS** (open until at least Jan 2028) — the natural "newest 2.x" bump target.
- **Jackson 3.0 (2025-10-03) is THE breaking boundary** and it hits us hard, because it renames *exactly the symbols our modules are built on* (all source-verified by diffing `jackson-databind-2.18.1` vs `-3.0.0`): Maven groupId `com.fasterxml.jackson.core` → `tools.jackson.core`, Java package `com.fasterxml.jackson.{core,databind}` → `tools.jackson.{core,databind}`, plus class renames `Module`→`JacksonModule`, `JsonSerializer`→`ValueSerializer`, `JsonDeserializer`→`ValueDeserializer`, `SerializerProvider`→`SerializationContext`. The `Serializers`/`Deserializers` SPI callbacks also changed signature (`BeanDescription` → `BeanDescription.Supplier`, and `findSerializer` gained a 4th `JsonFormat.Value formatOverrides` param). `jackson-annotations` is the one part that does **not** move (stays `com.fasterxml.jackson.annotation`) — but we don't use annotations anyway.
- **Spring coupling:** Boot **3.5 → Jackson 2.x**; Boot **4.0 (Nov 2025) → Jackson 3** as the preferred JSON lib (with a deprecated `spring-boot-jackson2` stop-gap and retained Jackson-2 dependency management). So our Jackson axis and our Spring axis move together: anyone on Boot 4 will expect a Jackson-3-capable typed-ids.
- **Key open question (do not decide here):** a single compiled artifact cannot satisfy both `com.fasterxml.jackson.databind.JsonSerializer` and `tools.jackson.databind.ValueSerializer` — they are different types in different packages with different method signatures. Supporting both Jackson 2 and Jackson 3 almost certainly means **two source sets / two artifacts** (e.g. a `-jackson2` and `-jackson3` flavor), not a single cross-version module. Reasoning in the strategy section below.

## How typed-ids uses Jackson

All meaningful Jackson integration lives in the **`typed-ids` core module**, declared as **`compileOnly(libs.jackson.databind)`** (so Jackson is an optional/provided dependency — consumers bring their own). Two `@AutoService(Module.class)`-registered modules:

**UUID side** — `modules/typed-ids/src/main/java/org/framefork/typedIds/uuid/json/jackson/`
- `ObjectUuidJacksonModule extends com.fasterxml.jackson.databind.Module` — overrides `getModuleName()`, `version()` (returns `Version.unknownVersion()`), `setupModule(SetupContext)`; registers an inner `Serializers.Base` and `Deserializers.Base`.
  - inner `ObjectUuidSerializers extends Serializers.Base` overrides `findSerializer(SerializationConfig, JavaType, BeanDescription)`.
  - inner `ObjectUuidDeserializers extends Deserializers.Base` overrides `findBeanDeserializer(JavaType, DeserializationConfig, BeanDescription)`.
- `ObjectUuidSerializer extends com.fasterxml.jackson.databind.JsonSerializer<ObjectUuid<?>>` — `serialize(value, JsonGenerator, SerializerProvider) throws IOException`, calls `jsonGenerator.writeString(...)`.
- `ObjectUuidDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<ObjectUuid<?>>` — `deserialize(JsonParser, DeserializationContext) throws IOException`, calls `parser.getValueAsString()`.

**BigInt side** — `modules/typed-ids/src/main/java/org/framefork/typedIds/bigint/json/jackson/`
- `ObjectBigIntIdJacksonModule extends Module` — same shape, but serialize-only (registers `Serializers` only; relies on Jackson's built-in `long`→constructor deserialization, per the in-code comment).
- `ObjectBigIntIdSerializer extends JsonSerializer<ObjectBigIntId<?>>` — `serialize(...)` calls `jsonGenerator.writeNumber(id.toLong())`.

**Exact upstream API surface we consume** (from `grep import com.fasterxml.jackson` over non-test main sources):

| Symbol (Jackson 2.x) | Package | How we use it |
|---|---|---|
| `Module` | `com.fasterxml.jackson.databind` | base class of both modules |
| `Module.SetupContext` | `com.fasterxml.jackson.databind` | `addSerializers()`, `addDeserializers()` |
| `JsonSerializer<T>` | `com.fasterxml.jackson.databind` | base of both serializers |
| `JsonDeserializer<T>` | `com.fasterxml.jackson.databind` | base of UUID deserializer |
| `SerializerProvider` | `com.fasterxml.jackson.databind` | `serialize()` param (unused body) |
| `DeserializationContext` | `com.fasterxml.jackson.databind` | `deserialize()` param (unused body) |
| `ser.Serializers.Base` | `com.fasterxml.jackson.databind.ser` | `findSerializer(...)` override |
| `deser.Deserializers.Base` | `com.fasterxml.jackson.databind.deser` | `findBeanDeserializer(...)` override |
| `SerializationConfig` / `DeserializationConfig` | `com.fasterxml.jackson.databind` | SPI callback params |
| `JavaType` | `com.fasterxml.jackson.databind` | SPI callback params + `getRawClass()` |
| `BeanDescription` | `com.fasterxml.jackson.databind` | SPI callback params (unused body) |
| `JsonGenerator` | `com.fasterxml.jackson.core` | `writeString()`, `writeNumber(long)` |
| `JsonParser` | `com.fasterxml.jackson.core` | `getValueAsString()` |
| `Version` | `com.fasterxml.jackson.core` | `Version.unknownVersion()` |

We use **zero `jackson-annotations`** in the core module — so the "annotations don't move in 3.0" carve-out doesn't help us; everything we touch is in the packages that **do** get renamed.

**Secondary, indirect touch-point** (different axis): `modules/typed-ids-openapi-swagger-jakarta` imports `com.fasterxml.jackson.databind.type.SimpleType` in `TypedIdsSchemaUtils.java` (used in an `instanceof SimpleType` check). That module depends on `swagger-core-jakarta` (`api(libs.swagger.v3.core.jakarta)`), not on `jackson-databind` directly — its Jackson version is **whatever swagger-core drags in**, which is Jackson 2 for the foreseeable future. So this is coupled to the swagger axis, not independently bumpable, and is out of scope for a "move typed-ids to Jackson 3" decision until swagger-core itself moves.

## Version timeline (newer-than-ours)

Dates verified against `jackson-bom` git tags.

### 2.19.0 — 2025-04-24

- Headline: new `javax.money` datatype modules; `@JsonUnwrapped` + `@JsonCreator` support; `JsonNode` stream/optional helpers; one behavior tweak to `JsonPointer` orphan-`~` parsing. Platform baseline unchanged (mostly Java 8, Android SDK 26).
- **Impact on typed-ids: none.** Nothing in `Module`/`Serializers`/`Deserializers`/`JsonSerializer`/`JsonGenerator`/`Version` changed. `JsonPointer` and `JsonNode` are not on our path.
- 2.19 is **not LTS** and its branch is already closed (last patch 2.19.4, 2025-10-29; 2.19.3 was BROKEN, skip it).

### 2.20.0 — 2025-08-28

- Headline: mostly prep-for-3.0 deprecations and packaging hygiene. `jackson-annotations` drops its patch number (versioned `2.20`, not `2.20.0`) and raises baseline to Java 8. Several `MapperBuilder`/`ObjectMapper` methods deprecated to align with 3.0 (`serializationInclusion()`, URL-taking `readValue()`), `iPhone`-style property fix (`MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX`, default off in 2.x).
- **Impact on typed-ids: none.** All deprecations are on `ObjectMapper`/`MapperBuilder` config surfaces we never call. Our SPI/serializer base classes are untouched. (Note: Jackson 3.0 ships against `jackson-annotations` 2.20 — relevant only as a dependency-management fact, not a code change for us.)

### 2.21.0 — 2026-01-18 — **2.x LTS**

- Headline: **the 2.x Long-Term-Support line** (open until ≥ Jan 2028). New `@JsonDeserializeAs`/`@JsonSerializeAs` annotations, `@JacksonInject` inject-only mode, `JsonTypeInfo.As.NOTHING`, `@JsonFormat(radix=)` (3.1+ only), SBOM publishing fix. New `jackson-module-spi-subtypes`.
- **Impact on typed-ids: none.** New annotations are opt-in and unused by us; no change to any symbol we import.
- **Strategic note:** if we bump the 2.x floor/ceiling, **2.21.x is the version to standardize on** — it is the supported, long-lived 2.x line, whereas 2.19/2.20 branches are closed.

### 2.22.0 — 2026-05-31 (jackson-core part 2026-06-03)

- Headline: maintenance/bug-fix release, explicitly *not* LTS (2.21 holds that role). A few new annotations (`@JsonApplyView`, `@JsonTypeInfo.writeTypeIdForDefaultImpl`), `MapperFeature.SORT_PROPERTIES_BY_INDEX`, nil-`UUID` inclusion fix, `jackson-core` had *no* changes.
- **Impact on typed-ids: none.** This is the last planned 2.x minor and it still compiles our code unchanged. Confirms the entire 2.x tail is a safe, zero-friction bump for us — the only decision is *which* 2.x to declare, not whether the code needs touching.

> **Bottom line for 2.x:** our integration is forward-compatible across the *whole* remaining 2.x line with **no code changes**. A version bump from 2.18.1 to 2.21.x (LTS) or 2.22.0 is a one-line catalog edit. The interesting work is entirely on the 3.x boundary.

### 3.0.0 — 2025-10-03 — **major, breaking** (full deep dive below)

- Headline: groupId + package rename to `tools.jackson`, removal of all 2.x deprecations, core-entity renames (JSTEP-6), immutable builder-based `ObjectMapper`, unchecked exception hierarchy (`JacksonException`), Java-8 modules folded into databind, native `module-info.java`, **Java 17 minimum**.
- **Impact on typed-ids: extensive — every Jackson symbol our two modules import is renamed and/or moved.** Cannot compile against 3.0 without source changes. See deep dive.

### 3.1.0 — 2026-02-23 — **3.x LTS**

- Headline: the **3.x LTS** line (open ≥ 2 years). Bug-fix-heavy "settle the 3.x defaults" release. Behavior: `AtomicReference` missing-value now defaults like `Optional`; `maxStringLength` default 20M→100M; `JsonNode.asXxx(default)` null-handling tweak. New `@JsonFormat(radix=)` support, `DelegatingSerializer`, multi-error deserialization collection.
- **Impact on typed-ids: none beyond 3.0.** None of our imported symbols changed between 3.0 and 3.1. If/when we ship a Jackson-3 flavor, **3.1 is the target** (LTS), exactly as 2.21 is for the 2.x flavor. The 3.0→3.1 delta does not touch `JacksonModule`/`Value(De)Serializer`/`Serializers`/`Deserializers`/`JsonGenerator`/`JsonParser`.

### 3.2.0 — 2026-06-08

- Headline: maintenance + features. Behavior: external-type-id visibility fix (new `MapperFeature.EXTERNAL_TYPE_ID_ALWAYS_VISIBLE` to restore old behavior); `FAIL_ON_NULL_FOR_PRIMITIVES` now treats absent ≠ explicit-null. Lots of XML/CSV/YAML work, `JsonGenerator.writeComment()`. Not LTS (3.1 is).
- **Impact on typed-ids: none beyond 3.0.** Our symbols remain stable across 3.0→3.1→3.2. (One item to *watch, not act on*: `databind#5960` deprecates `SubTypeValidator` — irrelevant to us.)

## Breaking-change deep dive: Jackson 2.x → 3.0

This is the only migration that requires code changes. The 3.0 changes that intersect our code, mapped concretely to our imports.

### 1. Coordinate + package rename (JSTEP-1)

| Axis | Jackson 2.x | Jackson 3.0 |
|---|---|---|
| Maven groupId (core/databind) | `com.fasterxml.jackson.core` | `tools.jackson.core` |
| `jackson-databind` artifact | `com.fasterxml.jackson.core:jackson-databind` | `tools.jackson.core:jackson-databind` |
| Java package (core) | `com.fasterxml.jackson.core` | `tools.jackson.core` |
| Java package (databind) | `com.fasterxml.jackson.databind` | `tools.jackson.databind` |
| **Annotations (unchanged)** | `com.fasterxml.jackson:jackson-annotations` / pkg `com.fasterxml.jackson.annotation` | **same** (3.0 uses `jackson-annotations` 2.20) |

Because all 13 symbols in our table live under `com.fasterxml.jackson.core` or `…databind`, **every single one moves**. We import zero annotations, so the one stable carve-out gives us nothing.

### 2. Class renames hitting our exact base types (JSTEP-6 / databind#3037, #3043, #3044)

All rows below were source-verified by reading both `jackson-databind-2.18.1` and `jackson-databind-3.0.0` (file paths confirm the package move; declarations confirm the renames).

| Our 2.x type | 3.0 replacement | typed-ids site |
|---|---|---|
| `databind.Module` | **`databind.JacksonModule`** (still `public abstract class`) | base class of `ObjectUuidJacksonModule`, `ObjectBigIntIdJacksonModule` |
| `databind.JsonSerializer<T>` | **`databind.ValueSerializer<T>`** | `ObjectUuidSerializer`, `ObjectBigIntIdSerializer` |
| `databind.JsonDeserializer<T>` | **`databind.ValueDeserializer<T>`** | `ObjectUuidDeserializer` |
| `databind.SerializerProvider` | **`databind.SerializationContext`** | `serialize()` 3rd param |
| `databind.DeserializationContext` | unchanged name, new package `tools.jackson.databind` | `deserialize()` 2nd param |
| `ser.Serializers.Base` / `deser.Deserializers.Base` | same names, `tools.jackson.databind.{ser,deser}`, **changed callback signatures** (see §3) | inner SPI classes |

`Module` was renamed specifically to avoid clashing with `java.lang.Module`. `JacksonModule` keeps the exact same abstract methods we override — `public abstract String getModuleName()`, `public abstract Version version()`, `public abstract void setupModule(SetupContext)` — and its `SetupContext.addSerializers(Serializers)` / `addDeserializers(Deserializers)` now **return `SetupContext`** (chainable) instead of `void`; our void-context calls are unaffected. We extend `Module` **directly**, not `SimpleModule`, and we do **not** use `StdSerializer`/`StdScalarSerializer` (we extend `JsonSerializer`/`JsonDeserializer` directly) — so those classes' own 3.0 changes don't reach us.

Our `@AutoService(Module.class)` registration also has to switch to `@AutoService(JacksonModule.class)` and to the 3.x type — the generated `META-INF/services` file will reference the new FQCN (`tools.jackson.databind.JacksonModule`).

### 3. Method signature changes (source-verified, 2.18.1 → 3.0.0)

Exact declarations quoted from source at each tag. Every method we override changes, even beyond the package/type renames. Our bodies ignore the `BeanDescription` argument entirely, so the changes are mechanical — but each override signature *must* match the 3.x declaration or `@Override` fails to compile (and without `@Override` it silently stops overriding).

**Serializer** (we extend it; we implement `serialize`):
```
2.18.1  public abstract void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException;
3.0.0   public abstract void serialize(T value, JsonGenerator gen, SerializationContext ctxt)        throws JacksonException;
```
→ `SerializerProvider`→`SerializationContext`; checked `IOException`→unchecked `JacksonException`.

**Deserializer** (we extend it; we implement `deserialize`):
```
2.18.1  public abstract T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException;
3.0.0   public abstract T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException;
```
→ `DeserializationContext` keeps its name (new package); the checked `IOException` is dropped.

**`Serializers.Base.findSerializer`** (we override it; this is the biggest delta — note the **new 4th parameter** and `BeanDescription.Supplier`):
```
2.18.1  public JsonSerializer<?>  findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc)
3.0.0   default ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides)
```
→ return `JsonSerializer`→`ValueSerializer`; `BeanDescription`→`BeanDescription.Supplier`; **added** `JsonFormat.Value formatOverrides` (note `JsonFormat.Value` comes from the *un-renamed* `com.fasterxml.jackson.annotation` package). In 3.0 the interface methods are `default` (returning `null`), so `Serializers.Base` exists mainly for convenience; our override must adopt the full 4-arg shape.

**`Deserializers.Base.findBeanDeserializer`** (we override it):
```
2.18.1  public JsonDeserializer<?>   findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc)
3.0.0   default ValueDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef)
```
→ return `JsonDeserializer`→`ValueDeserializer`; `BeanDescription`→`BeanDescription.Supplier` (no extra param here, unlike `findSerializer`). `Deserializers.Base` is still a `public abstract static class` in 3.0.

### 4. Exceptions: checked `IOException` → unchecked `JacksonException` (JSTEP-4)

Both our `serialize()` and `deserialize()` overrides declare `throws IOException`. In 3.0 the streaming/databind methods throw the **unchecked** `tools.jackson.core.JacksonException` (root of the new hierarchy; `JsonProcessingException`→`JacksonException`, `JsonMappingException`→`DatabindException`, `JsonParseException`/`JsonGenerationException`→`StreamReadException`/`StreamWriteException`). Dropping `throws IOException` is harmless (unchecked), but the override signatures must match the new abstract declarations.

### 5. Streaming methods we call — do they survive 3.0? (verified from `jackson-core` 3.0.4 source)

All confirmed present in `tools.jackson.core` at tag `jackson-core-3.0.4` (extracted via `git show`):
- `JsonGenerator.writeString(String value) throws JacksonException` and `writeNumber(long v) throws JacksonException` — **retained**, now return `JsonGenerator` (chainable) and throw unchecked `JacksonException` instead of `IOException`. Our void-context calls are unaffected.
- `JsonParser.getValueAsString()` — **retained** (`public String getValueAsString()`, no checked throws). We're clear. (`getText()` core#1378: still present in 3.0.4 but `@Deprecated since 3.0`, delegating to the new abstract `getString()`; we don't call `getText()`, so no impact.)
- `Version.unknownVersion()` — **retained** (`public static Version unknownVersion()` on `tools.jackson.core.Version`).
- `JacksonException` is confirmed `extends RuntimeException` (unchecked) — relevant to the `throws IOException` removal in §4.

### 6. Behavioral defaults (mostly irrelevant to us, listed for triage)

3.0 flips many defaults (`FAIL_ON_UNKNOWN_PROPERTIES`→false, `WRITE_DATES_AS_TIMESTAMPS`→false, alphabetical property sort on, enums via `toString`, etc.). **None affect us**: our UUID serializer writes a bare string, our BigInt serializer writes a bare number, and BigInt deserialization relies on Jackson's built-in `long`→single-arg-constructor path. The one to *sanity-check with a test* (not a known break) is whether the 3.0 constructor/parameter-name auto-detection changes affect the BigInt "no explicit deserializer, use primary constructor" assumption — the in-code comment ("Jackson is capable of deserializing the long, and then using the primary constructor without any additional help") leans on 2.x creator detection, and 3.0 folded `parameter-names` into databind and tweaked creator auto-detection (databind#5318). Worth a round-trip test, not a presumed failure.

### 7. Other 3.0 facts that matter to packaging (not our code, but our build)

- **Java 17 minimum** for all 3.x components (we'd need our Jackson-3 flavor to compile on 17+).
- Native `module-info.java` (JPMS) — modules are now proper named modules; our `@AutoService` registration still works via `META-INF/services` but a fully-modular consumer may want a `provides … with` directive eventually.
- Java-8 datatype/parameter-names/jsr310 modules are **folded into databind** in 3.0 — no behavioral impact on us, but it changes what "register all modules" means downstream.

## Spring Boot coupling (why the Jackson axis tracks the Spring axis)

From the `github.com/spring-projects/spring-boot` wiki release notes (4.0 Migration Guide, "Upgrading Jackson"):

| Spring Boot | Jackson | Notes |
|---|---|---|
| **3.5.x** (our current `springBoot = 3.5.8`) | **Jackson 2.x** | only "well-known" modules auto-registered |
| **4.0** (GA 2025-11, "Upgrading Jackson" section) | **Jackson 3** preferred (`tools.jackson`) | retains Jackson-2 dependency management; ships deprecated **`spring-boot-jackson2`** stop-gap; `spring.jackson.use-jackson2-defaults` flag; auto-registers **all** classpath modules (`spring.jackson.find-and-add-modules`, default on) — our `@AutoService`-registered module benefits |
| **4.1** | Jackson 3 | continues 4.0 direction |

Implication: a typed-ids consumer on **Boot ≤ 3.5 needs the Jackson-2 build**; a consumer on **Boot ≥ 4.0 will, by default, be on Jackson 3** and needs the Jackson-3 build. Boot 4.0's `find-and-add-modules=true` default is actually favorable to us — our auto-service module gets picked up without manual `mapper.registerModule(...)`. Boot 4 also keeps Jackson-2 dependency management alive, so a Jackson-2 typed-ids artifact still *works* under Boot 4 for users who opt into `spring-boot-jackson2`, but that's a deprecated migration path, not the steady state.

## Compatibility-strategy implications

What the above means for module structure and the version matrix (surfacing, not deciding):

- **2.x is free.** Bumping the declared Jackson from 2.18.1 to **2.21.x (LTS)** or **2.22.0** requires no source changes and preserves every older upgrade path (our `compileOnly` floor could stay low while testing against newer). The only call is which 2.x to standardize the catalog on.
- **3.x cannot share compiled bytecode with 2.x.** `com.fasterxml.jackson.databind.JsonSerializer` and `tools.jackson.databind.ValueSerializer` are distinct classes in distinct packages with distinct method signatures (different param types, `JacksonException` vs `IOException`). A single `.class` cannot extend both, and there is no shared interface bridging them. So "support Jackson 2 and 3 from one module" is **not** achievable by reflection-free polymorphism the way some of our other integrations might be.
- This points toward **parallel source sets / separate artifacts** — e.g. keep the current Jackson-2 integration as one flavor and add a `tools.jackson` (Jackson-3) flavor as a second source set/artifact, sharing only the typed-id core types (`ObjectUuid`, `ObjectBigIntId`, the `ReflectionHacks`/`*TypeUtils` helpers, which have no Jackson dependency). The two flavors would have near-identical logic but different imports/base classes.
- **The annotations carve-out doesn't rescue us.** Some libraries can straddle 2/3 by only touching `jackson-annotations` (unchanged in 3.0). We touch databind + core exclusively, so that escape hatch is closed.

### Open questions / decisions to make later (not decided here)

1. **One module, two source sets, or two published artifacts?** (e.g. `typed-ids-jackson` staying 2.x + new `typed-ids-jackson3`, vs. a multi-variant single module with Gradle feature variants / capabilities.) Trade-off: artifact proliferation vs. classpath-collision safety. Note both flavors can coexist on a classpath only if their `@AutoService` service files and class names don't collide — separate packages would be needed.
2. **Which versions become the supported floor/ceiling per flavor?** Candidate: Jackson-2 flavor floor stays ≤ 2.18 (preserve upgrade paths), tested up to 2.21/2.22; Jackson-3 flavor targets **3.1 (LTS)** with 3.0 as floor.
3. **Do we couple the decision to Spring Boot majors?** Boot 3.5 ↔ Jackson 2, Boot 4.0+ ↔ Jackson 3. A test-matrix axis (Boot 3.5 + Jackson 2, Boot 4.x + Jackson 3) likely mirrors the Jackson flavor split exactly.
4. **JDK floor for the Jackson-3 flavor.** Jackson 3 requires **Java 17**; if typed-ids currently supports an earlier JDK, the Jackson-3 flavor raises the floor for that flavor only.
5. **BigInt deserialization round-trip under 3.0** — verify the "no explicit deserializer, rely on constructor" assumption still holds given 3.0's creator/parameter-name auto-detection changes (databind#5318); add a 3.x round-trip test before trusting it.
6. **`@AutoService(Module.class)` → `@AutoService(JacksonModule.class)`** in the 3.x flavor, and confirm `MapperBuilder.findAndAddModules()` (Boot 4 default) discovers it.
