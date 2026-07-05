# Changelog research: Swagger Core (v3 / Jakarta)

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/swagger-api/swagger-core` git tags `v2.2.0`…`v2.2.52` — full tag list enumerated with `git tag`; dates verified with `git log -1 --format=%ci <tag>`. Confirmed there is **no** `v3.x` tag (latest is `v2.2.52`, 2026-06-23).
> - `github.com/swagger-api/swagger-core` `pom.xml` at tags `v2.2.35` and `v2.2.52` (`git show <tag>:pom.xml`) for the Jackson dependency-management properties and the Java baseline.
> - `github.com/swagger-api/swagger-core` source diffed across `v2.2.35`→`v2.2.52` (`git diff`) on the exact converter SPI + model classes we consume: `io/swagger/v3/core/converter/{ModelConverter,ModelConverterContext,ModelConverterContextImpl,ModelConverters,AnnotatedType}.java`, `io/swagger/v3/oas/models/Components.java`, `io/swagger/v3/oas/models/media/{Schema,StringSchema,IntegerSchema}.java`, plus `io/swagger/v3/core/jackson/ModelResolver.java`.
> - typed-ids source: `gradle/libs.versions.toml`, `modules/typed-ids-openapi-swagger-jakarta/{build.gradle.kts, src/main/java/org/framefork/typedIds/swagger/{TypedIdsModelConverter,TypedIdsSchemaUtils}.java}`.

## TL;DR for typed-ids

- **We target `io.swagger.core.v3:swagger-core-jakarta:2.2.35` today** (catalog `swagger-v3-core-jakarta`), released 2025-07-31. The latest is **`2.2.52`** (2026-06-23). The whole gap is a **steady patch cadence on a single `2.2.x` minor — there is NO major bump and no `swagger-core 3.x` artifact**. The "v3" in the coordinate/package name refers to the **OpenAPI 3 spec**, not a library major (verified: the only tags are `v2.2.x`, latest `v2.2.52`).
- **Definitive Jackson finding (resolves the springdoc doc's open question #4): swagger-core 2.2.x is Jackson-2-based, end to end.** Its own `pom.xml` declares `com.fasterxml.jackson.*` only — `2.2.35` manages Jackson `2.19.2` (databind `2.19.2`), `2.2.52` manages `jackson-version=2.22.0` / `jackson-databind-version=2.21.1` / `jackson-annotations-version=2.21`. There is **zero `tools.jackson`** (Jackson 3) anywhere in the source at `v2.2.52`. So our `com.fasterxml.jackson.databind.type.SimpleType` usage in `TypedIdsSchemaUtils` **stays valid**, and the OpenAPI axis is **truly decoupled** from our Jackson-3 work.
- **Every swagger-core API our two classes touch is byte-for-byte stable across `2.2.35`→`2.2.52`.** The `ModelConverter` SPI interface diff is **empty**; `ModelConverterContext`, `ModelConverters`, `Components`, `StringSchema` diffs are all **empty**; `AnnotatedType` and `Schema` changed only **additively** (new fields/methods, zero `public`/`protected` removals); `IntegerSchema` changed only its internal `cast()` body; `ModelConverterContextImpl` changed only one log line. Our integration **compiles and behaves unchanged** — a bump from 2.2.35 to 2.2.52 is a one-line catalog edit with no source impact.
- **Java baseline unchanged: Java 8** (`maven.compiler.release=8`) at both `2.2.35` and `2.2.52`. No JDK floor move on this axis.
- **Catalog drift to flag:** our declared floor `2.2.35` is **older than what every current springdoc resolves** — springdoc `2.8.17`/`3.0.3` both pin swagger-core `2.2.47` (per `docs/changelog-springdoc.md`), and the absolute latest is `2.2.52`. A real app on a current springdoc therefore runs `2.2.47+` against our converter, while our build compiles against `2.2.35`. Given the stable SPI, bumping the floor to `2.2.47`/`2.2.52` is risk-free and closes the gap.
- **Forward look:** no signal of a future swagger-core moving to Jackson 3 / a new major. There is no `jackson3`/`spring-boot-4` branch, and the open dependabot branches still target Jackson **2.22.0**. A Jackson-3 coupling could only arrive if swagger-core itself ships a Jackson-3 major — which has **not** happened and is not staged in the repo.

## How typed-ids uses Swagger Core

All swagger-core usage lives in **`typed-ids-openapi-swagger-jakarta`**, declared as `api(libs.swagger.v3.core.jakarta)` (= `io.swagger.core.v3:swagger-core-jakarta:2.2.35`). The sibling `typed-ids-openapi-springdoc` only *registers* the converter as a Spring bean and imports no swagger symbol itself (see `docs/changelog-springdoc.md`). Two classes do the work:

### `TypedIdsModelConverter` — the SPI implementation

`implements io.swagger.v3.core.converter.ModelConverter`, annotated `@AutoService(ModelConverter.class)` (generates `META-INF/services/io.swagger.v3.core.converter.ModelConverter` for swagger's own `ServiceLoader` discovery). It overrides the single SPI method:

```java
Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain)
```

For any `TypedId` subtype it builds a `Schema` and either calls `context.defineModel(name, schema, rawClass, null)` + returns a `$ref` (when `idsAsRef`), or returns the inline schema; otherwise it delegates down the `chain`. Its javadoc also `{@link}`s `io.swagger.v3.core.jackson.ModelResolver#enumsAsRef` (documentation reference only, not a compile dependency on the field).

### `TypedIdsSchemaUtils` — the schema building + the one Jackson touchpoint

Builds `StringSchema` (`format("uuid")`, `minLength`/`maxLength = 36`) for UUID ids and `IntegerSchema` (`format("int64")`) for bigint ids, and bootstraps a private `ModelConverterContextImpl` (filtered to exclude our own converter) to resolve a default schema and read any `io.swagger.v3.oas.annotations.media.Schema` annotation off the class. It contains the **single Jackson import on this axis** — `com.fasterxml.jackson.databind.type.SimpleType` — used in `rawClassOf(Type)`:

```java
if (type instanceof SimpleType simpleType) { return simpleType.getRawClass(); }
```

This is **Jackson 2**, matching swagger-core 2.2.x's Jackson-2 internals (swagger passes Jackson `JavaType`s — `SimpleType` is one — through the `ModelConverter` chain).

### Exact upstream API surface we consume

| Symbol | Package | How we use it |
|---|---|---|
| `ModelConverter` | `io.swagger.v3.core.converter` | **implemented** SPI; `resolve(...)` override + `@AutoService` |
| `ModelConverterContext` | `io.swagger.v3.core.converter` | `resolve(...)`, `defineModel(...)` |
| `ModelConverterContextImpl` | `io.swagger.v3.core.converter` | instantiated in `getModelConverterContext()` |
| `ModelConverters` | `io.swagger.v3.core.converter` | `getInstance().getConverters()` (to filter out self) |
| `AnnotatedType` | `io.swagger.v3.core.converter` | `.type(...)`, `.resolveAsRef(false)` builder |
| `ModelResolver` | `io.swagger.v3.core.jackson` | javadoc `{@link}` only (`enumsAsRef`) |
| `Components` | `io.swagger.v3.oas.models` | `COMPONENTS_SCHEMAS_REF` constant for `$ref` prefix |
| `Schema` | `io.swagger.v3.oas.models.media` | `$ref()`, `name()`, `getName()`/`setName()`, `getDescription()`/`setDescription()` |
| `StringSchema` | `io.swagger.v3.oas.models.media` | `format()`, `minLength()`, `maxLength()` |
| `IntegerSchema` | `io.swagger.v3.oas.models.media` | `format()` |
| `io.swagger.v3.oas.annotations.media.Schema` | `io.swagger.v3.oas.annotations.media` | read via reflection (`getDeclaredAnnotation`) |
| `SimpleType` | `com.fasterxml.jackson.databind.type` (**Jackson 2**) | `instanceof` + `getRawClass()` |

## Version timeline (newer-than-ours)

There is **one minor line** in scope — `2.2.x` — so this is a flat list of patch releases, not a multi-major timeline. Dates verified against git tags. Every release below is a Jackson-2 build (`maven.compiler.release=8`).

| swagger-core-jakarta | Released | Managed Jackson (databind) | Relevance |
|---|---|---|---|
| **2.2.35** *(our floor)* | 2025-07-31 | 2.19.2 | baseline |
| 2.2.36 | 2025-08-18 | — | patch |
| 2.2.38 | 2025-09-29 | — | what springdoc 2.8.14 / 3.0.0 pin |
| 2.2.41 | 2025-11-24 | — | what springdoc 2.8.15 / 3.0.1 pin |
| 2.2.43 | 2026-02-17 | — | what springdoc 2.8.16 / 3.0.2 pin |
| 2.2.45 | 2026-03-10 | — | patch |
| 2.2.47 | 2026-04-09 | — | **what springdoc 2.8.17 / 3.0.3 pin** (current real-world floor) |
| 2.2.49 | 2026-04-28 | — | patch |
| 2.2.51 | 2026-06-12 | — | patch |
| **2.2.52** *(latest)* | 2026-06-23 | 2.21.1 (jackson-core 2.22.0, annotations 2.21) | latest available |

**122 commits** land between `v2.2.35` and `v2.2.52` — a high-cadence maintenance line. The Jackson management drifts *within* the 2.x family (2.19.2 → 2.22.0/2.21.1), never to Jackson 3.

### What changed on our integration points across 2.2.35 → 2.2.52 (the only triage that matters)

Diffing the exact files we consume:

| File / symbol | Diff result | Touches us? |
|---|---|---|
| `converter/ModelConverter.java` (the SPI we implement) | **empty diff** — interface identical | **No** |
| `converter/ModelConverterContext.java` | **empty diff** | **No** |
| `converter/ModelConverters.java` | **empty diff** | **No** |
| `converter/ModelConverterContextImpl.java` | one changed log line only (`resolve %s` → `resolve %s from %s` with `identityHashCode`); constructor + `resolve(...)` unchanged | **No** |
| `converter/AnnotatedType.java` | **additive only** — new fields `resolveEnumAsRef`, `isSubtype` + their getters/setters, new `Comparator`/`Collectors` imports; the `.type(...)` / `.resolveAsRef(...)` builder methods we call are unchanged | **No** |
| `models/Components.java` (`COMPONENTS_SCHEMAS_REF`) | **empty diff** — constant unchanged (`"#/components/schemas/"`) | **No** |
| `models/media/Schema.java` | 41 insertions / 8 deletions, but **zero `public`/`protected` removals**; `$ref()`, `name()`, `getName()`/`setName()`, `getDescription()`/`setDescription()` all present at `v2.2.52` | **No** |
| `models/media/StringSchema.java` | **empty diff** | **No** |
| `models/media/IntegerSchema.java` | internal `cast()` rewrite (`NumberFormat` → `Long.parseLong` + `withinIntegerBounds`); no API change — `format()` we call lives on `Schema` | **No** |
| `core/jackson/ModelResolver.java` (`enumsAsRef`, javadoc `{@link}` target) | field still present at `v2.2.52` (`public static boolean enumsAsRef`) | **No** (link target intact) |

**Conclusion: `TypedIdsModelConverter` and `TypedIdsSchemaUtils` compile and behave unchanged across the entire `2.2.35`→`2.2.52` range.** No deprecation, rename, or removal hits any symbol we use. (Verified, not assumed.)

A handful of in-range fixes touch schema-resolution *internals* near our code — e.g. `AnnotatedType` equality/caching corrections (`#4975`, `#5005`, `#5114`) and an `enumAsRef` reattachment fix (`#4932`). These change swagger's own caching/resolution behaviour, not the SPI contract; our usage resolves each `TypedId` class through a freshly built `ModelConverterContextImpl`, so the caching fixes are at worst neutral-to-beneficial and at best invisible. Worth a sanity round-trip in tests when bumping, but none is a contract break.

## Breaking-change deep dive

**There is no major boundary to deep-dive.** swagger-core has stayed on `2.2.x` for the entire window of interest (and well before — `2.2.0` long predates our floor). The coordinate, package root (`io.swagger.v3.*`), groupId (`io.swagger.core.v3`), Jakarta flavour (`-jakarta`), and Java 8 baseline are all constant from `2.2.35` through `2.2.52`. The only "axis" of change is the *internally managed Jackson version*, and that stays within Jackson 2.

### The Jackson-2 coupling, stated definitively (springdoc open question #4)

> *Does swagger-core 2.2.x depend on Jackson 2 (`com.fasterxml.jackson`) and NOT Jackson 3?* — **Yes, definitively Jackson 2. Evidence from swagger-core's own POMs:**

| | swagger-core `v2.2.35` | swagger-core `v2.2.52` |
|---|---|---|
| Jackson groupId in `pom.xml` | `com.fasterxml.jackson.*` | `com.fasterxml.jackson.*` |
| `jackson-version` | **2.19.2** | **2.22.0** |
| `jackson-databind-version` | **2.19.2** | **2.21.1** |
| `jackson-annotations-version` | (= `jackson-version`, 2.19.2) | **2.21** |
| Any `tools.jackson` reference in source | **none** | **none** |

swagger-core generates schemas with its own internal Jackson-2 `ObjectMapper` (`io.swagger.v3.core.util.Json/Yaml`) and threads Jackson-2 `JavaType`s (incl. `SimpleType`) through the `ModelConverter` chain. Because that pipeline is Jackson-2 *regardless of the host application's primary mapper*, our `TypedIdsSchemaUtils` `instanceof SimpleType` (importing `com.fasterxml.jackson.databind.type.SimpleType`) is correct as long as swagger-core stays on its `2.2.x` Jackson-2 line — which is every version through `2.2.52`. **This is exactly why a Boot-4 / Jackson-3 application does not break our OpenAPI module:** the schema axis runs on swagger-core's bundled Jackson 2, independent of the app's Jackson 3. The OpenAPI work and the Jackson-3 work are separate decisions.

## Compatibility-strategy implications

What the above means for module structure and the matrix (surfacing, not deciding):

- **No `-2x`/`-3x` split is forced on the swagger axis.** Unlike Jackson (`com.fasterxml`→`tools.jackson`) and Hibernate (the `-NN` per-version modules), swagger-core presents one stable `2.2.x` SPI. The *same compiled* `typed-ids-openapi-swagger-jakarta` bytecode loads against any `2.2.35`…`2.2.52`. A second source set would only become necessary if/when swagger-core ships a Jackson-3 major with a renamed/changed `ModelConverter` SPI — which does not exist today.
- **Raise the floor (low-risk catalog hygiene).** `swagger-v3-core-jakarta = 2.2.35` sits below what real consumers resolve (springdoc pins `2.2.47`; latest is `2.2.52`). Bumping to `2.2.47` (the springdoc-aligned value) or `2.2.52` (latest) is risk-free given the empty SPI diff, and stops our compile target drifting behind the runtime. Keeping a *low* floor only matters if we want to preserve old-swagger upgrade paths — and since the SPI is unchanged back through at least `2.2.0`, our code already works against the whole `2.2.x` line, so the floor choice is about which version we *test/compile* against, not about source compatibility.
- **The Jackson axis lives in this module, but swagger pins it.** The single Jackson-2 touchpoint (`SimpleType`) is bound to whatever Jackson swagger-core drags in (`2.19.2` at our floor, `2.21.1` at latest) — all Jackson 2, all source-compatible for an `instanceof`/`getRawClass()` use. This module is therefore **not** part of the "move typed-ids to Jackson 3" decision; it follows swagger-core, not the core module's `jackson` catalog entry.
- **This module gates nothing on Spring Boot 4.** Because swagger-core stays Jackson-2 and the SPI is stable, a consumer on Boot 4 / springdoc 3.0 still exercises our converter against swagger-core `2.2.x` Jackson 2 (confirmed in `docs/changelog-springdoc.md`: springdoc 3.0.x keeps pinning swagger-core `2.2.x`). The only thing to validate end-to-end is the springdoc/Boot wiring, not anything in this module.

### Open questions / decisions to make later (not decided here)

1. **Bump the swagger-core floor.** Choose `2.2.47` (springdoc-aligned) vs `2.2.52` (latest) vs keep `2.2.35`. All three compile our code identically; the only consideration is staying close to what consumers resolve. *Recommendation surface, not decision:* align to `2.2.47`+ to kill the drift flagged above.
- **Re-check coupling only if swagger-core ships a new major.** The "OpenAPI axis is decoupled from Jackson 3" conclusion holds **for as long as swagger-core stays on its Jackson-2 `2.2.x` line.** If swagger-core ever releases a Jackson-3-based major (none exists or is branched as of 2026-06-28), `TypedIdsSchemaUtils`'s `com.fasterxml.jackson.databind.type.SimpleType` import would need re-evaluation against `tools.jackson` — at which point this module would join the Jackson-3 decision. Until then, treat it as independent.
- **No swagger-specific test-matrix axis is needed.** Since the SPI is invariant across `2.2.x`, testing against a single current `2.2.x` (ideally the springdoc-pinned one) is sufficient; a per-version test fan-out (à la Hibernate) is not warranted for swagger-core.
