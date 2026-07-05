# Changelog research: SpringDoc OpenAPI

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/springdoc/springdoc-openapi` git tags `v2.8.0`…`v2.8.17`, `v3.0.0-M1`/`-RC1`, `v3.0.0`…`v3.0.3` — dates verified with `git log -1 --format=%ci <tag>`.
> - `github.com/springdoc/springdoc-openapi` `CHANGELOG.md` at tags `v3.0.3` and `v2.8.17` (the 2.8 and 3.0 lines carry independent changelogs — the 3.0 branch does not back-port the 2.8.15–17 entries).
> - `github.com/springdoc/springdoc-openapi` source diffed across the boundary — `git diff v2.8.17 v3.0.3 --` on `springdoc-openapi-starter-common/src/main/java/org/springdoc/core/configuration/SpringDocConfiguration.java`, `…/converters/ModelConverterRegistrar.java`, plus `git show <tag>:pom.xml` for the parent-Boot / swagger-core / swagger-ui managed versions.
> - typed-ids source: `gradle/libs.versions.toml`, `modules/typed-ids-openapi-springdoc/{build.gradle.kts, src/main/java/org/framefork/typedIds/springdoc/config/*, src/main/resources/META-INF/spring/…AutoConfiguration.imports}`, the sibling `modules/typed-ids-openapi-swagger-jakarta/src/main/java/org/framefork/typedIds/swagger/{TypedIdsModelConverter,TypedIdsSchemaUtils}.java`, and the test module `testing/testing-typed-ids-springdoc-2x-openapi/build.gradle.kts`.

## TL;DR for typed-ids

- **We consume ZERO `org.springdoc.*` API.** The production module `typed-ids-openapi-springdoc` is a thin Spring Boot `@AutoConfiguration` (`TypedIdsOpenApiAutoConfiguration`) that registers one `@Bean` — a swagger-core `ModelConverter` (`TypedIdsModelConverter`, which physically lives in the sibling `typed-ids-openapi-swagger-jakarta` module) — and binds a `@ConfigurationProperties` (`framefork.typed-ids.openapi.as-ref`). It imports no springdoc class, no customizer SPI, no `SpringDocUtils`, no provider. SpringDoc's only role is to be the runtime that *picks up that bean*.
- **The one springdoc behaviour we depend on is byte-for-byte stable across the 2.8→3.0 boundary.** SpringDoc auto-registers every `ModelConverter` bean in the Spring context into swagger's global `ModelConverters` via `SpringDocConfiguration.modelConverterRegistrar(Optional<List<ModelConverter>>, …)` → `ModelConverterRegistrar`. Both the bean method signature and the `ModelConverterRegistrar` class are **identical** between `v2.8.17` and `v3.0.3` (the `git diff` on the registrar is empty). So the contract we lean on does not change.
- **SpringDoc 3.0 is NOT a coordinate/package rename — it is purely a Spring Boot 4 rebaseline.** GroupId stays `org.springdoc`, the artifact stays `springdoc-openapi-starter-common`, the package stays `org.springdoc.core.*`. The CHANGELOG headline for `3.0.0` is literally "Upgrade to Spring Boot 4.0.0". This is fundamentally different from the Jackson `com.fasterxml`→`tools.jackson` break.
- **Parallel-line model confirmed from the POMs:** springdoc **2.8.x → Spring Boot 3.5.x** parent, springdoc **3.0.x → Spring Boot 4.0.x** parent (2.8.14→Boot 3.5.7, 2.8.17→3.5.13, 3.0.0→4.0.0, 3.0.3→4.0.5). The two lines are otherwise near-identical (same features, same fixes, same swagger-core), differing essentially only in which Boot major they compile against — the same coupling shape as swagger/Jackson.
- **Crucially, springdoc 3.0 does NOT drag Jackson 3 into the schema pipeline.** Both lines pin **swagger-core 2.2.x** (2.8.17 and 3.0.3 both ship `swagger-api.version = 2.2.47`), and swagger-core 2.2.x is Jackson-**2**-based with its own internal Jackson-2 `ObjectMapper`. So our Jackson-2 `SimpleType` usage in `typed-ids-openapi-swagger-jakarta` stays valid even under springdoc 3.0 / Boot 4 — the OpenAPI axis does **not** couple to the Jackson-3 axis the way the Boot BOM otherwise would.
- **Net impact:** unlike the Jackson and Hibernate boundaries, springdoc 2.8→3.0 imposes **no source change on our code**. The real (and only) question is whether `TypedIdsOpenApiAutoConfiguration` runs under **Spring Boot 4** — and since it uses only Boot annotations + `jakarta.validation`, that reduces entirely to the Boot-4 axis (see `docs/changelog-spring-boot.md`). The concrete gap is that there is **no Boot-4 / springdoc-3.x test module yet** — the only springdoc test module pins Boot 3.5.8 + springdoc 2.8.14.

## How typed-ids uses SpringDoc

Two cooperating modules, with a clean split of responsibilities:

### `typed-ids-openapi-springdoc` — the SpringDoc/Boot wiring (this doc's subject)

`build.gradle.kts` dependencies:
```
api(project(":typed-ids"))
api(project(":typed-ids-openapi-swagger-jakarta"))
api(libs.springdoc.openapi.starter.common)            // org.springdoc:springdoc-openapi-starter-common, catalog-pinned 2.1.0
annotationProcessor(libs.springdoc.openapi.spring.configuration.processor)  // = spring-boot-configuration-processor, version.ref springBoot (3.5.8)
```

Source — only two classes, **neither imports anything from `org.springdoc`**:

- `org.framefork.typedIds.springdoc.config.TypedIdsOpenApiAutoConfiguration` — annotated `@AutoConfiguration` + `@EnableConfigurationProperties(TypedIdsOpenApiProperties.class)`; exposes one `@Bean TypedIdsModelConverter typedIdsModelConverter()` that pushes the `as-ref` flag onto `TypedIdsModelConverter.idsAsRef` (a static field) and returns a new converter. Its imports are exclusively `org.springframework.boot.autoconfigure.AutoConfiguration`, `org.springframework.boot.context.properties.EnableConfigurationProperties`, `org.springframework.context.annotation.Bean`, plus `org.framefork.typedIds.swagger.TypedIdsModelConverter`.
- `org.framefork.typedIds.springdoc.config.TypedIdsOpenApiProperties` — `@ConfigurationProperties(prefix = "framefork.typed-ids.openapi")` with a single `@NotNull Boolean asRef = true` (import `jakarta.validation.constraints.NotNull` + `org.springframework.boot.context.properties.ConfigurationProperties`).
- `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — one line registering the auto-config (the Boot 2.7+/3.x discovery file).

**So the entire "SpringDoc integration" is: register a swagger `ModelConverter` as a Spring bean and let SpringDoc find it.** The upstream API surface we actually *touch* is Spring Boot's, not SpringDoc's. SpringDoc appears only as a classpath dependency whose runtime behaviour (auto-registering `ModelConverter` beans) we rely on.

### `typed-ids-openapi-swagger-jakarta` — the actual schema logic (separate axis, see swagger doc / Jackson doc)

This is where the OpenAPI work lives, and it depends on **swagger-core** (`api(libs.swagger.v3.core.jakarta)`, catalog `io.swagger.core.v3:swagger-core-jakarta:2.2.35`), not on springdoc:

- `TypedIdsModelConverter implements io.swagger.v3.core.converter.ModelConverter`, `@AutoService(ModelConverter.class)` — overrides `resolve(AnnotatedType, ModelConverterContext, Iterator<ModelConverter>)`; for any `TypedId` subtype builds a `Schema` and either `context.defineModel(...)` + returns a `$ref` (when `idsAsRef`) or returns an inline schema.
- `TypedIdsSchemaUtils` — builds `IntegerSchema`/`StringSchema`, and contains the one Jackson touchpoint on this axis: `import com.fasterxml.jackson.databind.type.SimpleType` (an `instanceof SimpleType` check). This is **Jackson 2**, matching swagger-core 2.2.x's Jackson-2 internals.

The boundary to keep straight: **swagger-core stability is a different question from springdoc stability** (covered in the swagger/Jackson docs). This doc covers the springdoc-starter wiring; it only notes swagger-core where springdoc *pins* it (because springdoc's pin is what a real app resolves).

### The bridge — how our bean reaches swagger (the load-bearing contract)

SpringDoc's `SpringDocConfiguration` declares:
```
@Bean
ModelConverterRegistrar modelConverterRegistrar(Optional<List<ModelConverter>> modelConverters, SpringDocConfigProperties springDocConfigProperties) {
    return new ModelConverterRegistrar(modelConverters.orElse(Collections.emptyList()), springDocConfigProperties);
}
```
`ModelConverterRegistrar` registers each context `ModelConverter` bean into swagger's `io.swagger.v3.core.converter.ModelConverters.getInstance()`. Our `@Bean TypedIdsModelConverter` is exactly one such bean. **This is the entire mechanical link between typed-ids and springdoc, and it is unchanged across 2.8→3.0** (verified: the `modelConverterRegistrar` signature is identical at `v3.0.3`, and `git diff v2.8.17 v3.0.3 -- …/ModelConverterRegistrar.java` is empty).

> Note: our `@AutoService(ModelConverter.class)` on `TypedIdsModelConverter` also generates a `META-INF/services/io.swagger.v3.core.converter.ModelConverter` file (swagger-core's own ServiceLoader path), so the converter is discoverable even outside Spring — but inside a SpringDoc app the Spring-bean → `ModelConverterRegistrar` path is what's exercised, and that's what the test module proves.

## Version timeline (newer-than-ours)

We currently **test** against springdoc **2.8.14** (test module: `springdoc-openapi-starter-webmvc-ui:2.8.14`) and **compile** the production module against `springdoc-openapi-starter-common:2.1.0` (a low floor — that old jar transitively pulls spring-boot 3.0.5, but our own catalog overrides swagger-core-jakarta to 2.2.35). Dates below verified against git tags; managed versions from each tag's `pom.xml`.

| springdoc | Released | Spring Boot parent | swagger-core | swagger-ui |
|---|---|---|---|---|
| **2.8.14** *(our tested)* | 2025-11-02 | 3.5.7 | 2.2.38 | 5.30.1 |
| 2.8.15 | 2026-01-01 | 3.5.9 | 2.2.41 | 5.31.0 |
| 2.8.16 | 2026-02-27 | 3.5.11 | 2.2.43 | 5.32.0 |
| 2.8.17 *(latest 2.8.x)* | 2026-04-12 | 3.5.13 | 2.2.47 | 5.32.2 |
| **3.0.0** *(the boundary)* | 2025-11-21 | **4.0.0** | 2.2.38 | 5.30.1 |
| 3.0.1 | 2026-01-01 | 4.0.1 | 2.2.41 | 5.31.0 |
| 3.0.2 | 2026-02-28 | 4.0.3 | 2.2.43 | 5.32.0 |
| 3.0.3 *(latest 3.0.x)* | 2026-04-12 | **4.0.5** | 2.2.47 | 5.32.2 |

The table makes the parallel-line model obvious: **for each date, the 2.8.x and 3.0.x patches ship the same swagger-core and swagger-ui** and differ only in the Spring Boot major they target. springdoc maintains the two lines in lockstep, exactly mirroring the Boot 3.5 ↔ Boot 4 split in `docs/changelog-spring-boot.md`.

### 2.8.15 → 2.8.17 — the 2.8.x maintenance tail (Boot 3.5 line)

- Pure maintenance on the classic stack. Feature adds that *could* touch schema output: `@Range` constraint support and "auto-set `nullable: true` for Kotlin nullable types" (2.8.17); `springdoc.swagger-ui.document-title` (2.8.16); Scalar UI support (2.8.13–15). Several swagger-core bumps (2.2.41→2.2.47) fixing schema-resolution issues. GraalVM-25 native fixes.
- **Impact on typed-ids: none.** None of these touch the `ModelConverter` SPI or the `ModelConverterRegistrar` contract. The swagger-core 2.2.x bumps stay on the Jackson-2 line our swagger-jakarta module already targets. The new `nullable`/`@Range`/Kotlin behaviours operate on other property types, not on our `TypedId` schema (a bare string/integer). Bumping the test pin from 2.8.14 to 2.8.17 is a no-op for our integration code.

### 3.0.0 — 2025-11-21 — **major, but it's a Boot-4 rebaseline, not an API break** (deep dive below)

- CHANGELOG headline: **"Upgrade to Spring Boot 4.0.0"** (+ Scalar 0.4.3, initial Spring Framework 7 API-versioning support). No groupId change, no `org.springdoc.*` package move, no artifact rename. swagger-core stays **2.2.38** (same as the contemporaneous 2.8.14) — i.e. **no Jackson-3 move**.
- **Impact on typed-ids: no source change required.** Our two classes import zero springdoc symbols, and the one springdoc behaviour we rely on (ModelConverter-bean auto-registration) is unchanged. The only thing that changes is the *runtime platform*: springdoc 3.0 requires Spring Boot 4 / Spring Framework 7 / Jakarta EE 11. So the question "does typed-ids work with springdoc 3.0" is identical to "does `TypedIdsOpenApiAutoConfiguration` work under Spring Boot 4" — a Boot-axis question, answered in the Boot doc (yes: `@AutoConfiguration` + the `AutoConfiguration.imports` file + `jakarta.validation` all survive Boot 4).

### 3.0.1 → 3.0.3 — the 3.0.x maintenance line (Boot 4 line)

- Mirrors 2.8.15–17 feature-for-feature (Scalar WebMVC/WebFlux, `@Range`, Kotlin-`nullable`, `document-title`, swagger-core 2.2.41→2.2.47), plus Boot-4-specific items: **#3186 "Decouple Web Server APIs following Spring Boot modularization"**, **#3228 "springdoc-openapi-starter 3.x doesn't depend on spring-boot-starter"**, **#3155 native-image regression with SpringDoc 3.0 + Spring Boot 4.0** (fixed in 3.0.1), GraalVM-25 reachability metadata (#3220), and **#3246 MCP (Model Context Protocol) support** added in 3.0.3.
- **Impact on typed-ids: none beyond 3.0.0.** These are springdoc-internal restructurings to fit Boot 4's module split; none reach the `ModelConverter` contract. If/when we add a springdoc-3 test module, **3.0.3 (Boot 4.0.5) is the target** — the latest, and the one carrying the native-image and GraalVM-25 fixes.

## Breaking-change deep dive: SpringDoc 2.8 → 3.0

The headline is that, *for us*, this is the gentlest of the three majors (springdoc vs Jackson vs Hibernate), because we don't compile against springdoc's API.

### 1. Coordinates and packages — unchanged

| Axis | springdoc 2.8.x | springdoc 3.0.x |
|---|---|---|
| Maven groupId | `org.springdoc` | **`org.springdoc`** (same) |
| Starter artifact we depend on | `springdoc-openapi-starter-common` | **`springdoc-openapi-starter-common`** (same) |
| Core package | `org.springdoc.core.*` | **`org.springdoc.core.*`** (same) |
| Spring Boot parent | **3.5.x** | **4.0.x** |
| Spring Framework (transitive) | 6.2.x | 7.0.x |
| Jakarta EE / Servlet | EE 10 / Servlet 6.0 | EE 11 / Servlet 6.1 |
| swagger-core (pinned) | 2.2.x (Jackson 2) | **2.2.x (Jackson 2)** — same line |

There is no `com.fasterxml`→`tools.jackson`-style rename anywhere in springdoc 3.0. The break is entirely "you must now be on Spring Boot 4 / Spring 7 / Jakarta 11".

### 2. The one contract we depend on — verified unchanged

Diffing `v2.8.17` vs `v3.0.3`:
- `ModelConverterRegistrar.java` — **empty diff** (identical).
- `SpringDocConfiguration.modelConverterRegistrar(Optional<List<ModelConverter>>, SpringDocConfigProperties)` — identical signature and body at both tags.

The only changes in `SpringDocConfiguration` across the boundary are internal Spring-Boot-4 package adaptations, e.g.:
```
- import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
+ import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
```
(the Boot 4 modularization moving `spring-data` autoconfigure classes into a `spring-boot-data` module) plus javadoc reflow. **None of this is on our integration path.**

### 3. swagger-core / Jackson coupling — the important non-event

The natural worry (mirroring the Jackson and Boot docs) is: *Boot 4 prefers Jackson 3 (`tools.jackson`); does springdoc 3.0 therefore push our swagger-jakarta schema code onto Jackson 3?* **No.** springdoc 3.0.x continues to pin **swagger-core 2.2.x** (2.2.38 in 3.0.0, 2.2.47 in 3.0.3 — the very same versions as the parallel 2.8.x patches). swagger-core 2.2.x is Jackson-2-based and ships its own internal Jackson-2 `ObjectMapper` (`io.swagger.v3.core.util.Json/Yaml`) for schema generation, independent of the application's primary `ObjectMapper`. So even on a Boot-4 app whose main mapper is Jackson 3, the OpenAPI schema pipeline — and our `TypedIdsModelConverter` / `SimpleType` check inside it — keeps running on Jackson 2. **The OpenAPI axis does not pull our Jackson-3 decision forward.** (A future move would only come if swagger-core itself ships a Jackson-3 major and springdoc adopts it — neither has happened in the 3.0.x line.)

### 4. Behavioural / output changes (triage)

- 3.0.x schema-affecting features (`nullable:true` for Kotlin nullable types, `@Range`, `JsonView` propagation for `Page<T>`) operate on property types we don't emit; our `TypedId` resolves to a bare `string`/`integer` schema (optionally a `$ref`). **No impact.**
- Native-image: #3155 (springdoc 3.0 + Boot 4 native regression) and #3220 (GraalVM 25) are fixed by 3.0.3 — relevant only if a consumer compiles native; our reflection-free converter doesn't add metadata needs beyond swagger's own.

## Compatibility-strategy implications

What the above means for module structure and the matrix (surfacing, not deciding):

- **No parallel `typed-ids-openapi-springdoc-3x` integration module is forced.** This is the key contrast with the Jackson `-jackson2`/`-jackson3` and Hibernate `-NN` splits. Because (a) our code imports no springdoc symbol, (b) the `ModelConverterRegistrar` contract is unchanged, (c) groupId/artifact/package are unchanged, and (d) swagger-core stays Jackson-2 — the *same compiled* `typed-ids-openapi-springdoc` bytecode is, in principle, loadable under both springdoc 2.8.x/Boot 3.5 and springdoc 3.0.x/Boot 4. The only divergence is the Spring Boot runtime it lands on.
- **The real gating axis is Spring Boot 4, not springdoc.** Whether the single artifact truly runs on both Boot majors depends on Boot-level concerns (the `@AutoConfiguration.imports` discovery file, `@ConfigurationProperties` binding, `jakarta.validation` availability) covered in `docs/changelog-spring-boot.md` — not on anything springdoc changed. If a split is ever needed, it would be driven by Boot 3-vs-4, not by springdoc 2.8-vs-3.0.
- **swagger-jakarta is the module to watch for the Jackson axis, not springdoc.** Our only Jackson-2 OpenAPI dependency (`SimpleType`) sits in `typed-ids-openapi-swagger-jakarta` and is pinned by swagger-core's version, which springdoc keeps on 2.2.x. That module's Jackson story is the swagger doc's concern; springdoc doesn't move it.
- **Catalog hygiene flag.** The production module compiles against `springdoc-openapi-starter-common = 2.1.0` (a 2023-era floor) while the test module exercises `2.8.14`, and our `swagger-v3-core-jakarta = 2.2.35` is older than what every current springdoc pins (2.2.47). These three drift independently. Bumping the floor to a current 2.8.x and the swagger pin to 2.2.47 would align us with what real apps resolve — and is risk-free given the stable `ModelConverter` contract.
- **Direction is additive.** Keep springdoc 2.8.x / Boot 3.5 coverage (the only thing pinning the classic-stack upgrade path Filip wants preserved); add springdoc 3.0.x / Boot 4 as a *new* axis, not a replacement.

### Open questions / decisions to make later (not decided here)

1. **Add a Boot-4 / springdoc-3.x test module?** The current `testing-typed-ids-springdoc-2x-openapi` pins Boot 3.5.8 + springdoc 2.8.14 only — there is **no end-to-end proof** that `TypedIdsOpenApiAutoConfiguration` + the `ModelConverterRegistrar` pickup still work under springdoc 3.0.3 / Boot 4.0.5. A mirror module (e.g. `testing-typed-ids-springdoc-3x-openapi`, springdoc `3.0.3`, Boot `4.0.5`) would validate the "same bytecode, two Boot majors" hypothesis end-to-end (CI green is the only thing the static diff can't prove). This parallels the missing Boot-4 JSON test module flagged in the Boot/Jackson docs.
2. **One artifact or two for springdoc?** The static analysis says one artifact *should* serve both lines. But if Boot 4's auto-config/validation runtime turns out to need a different `AutoConfiguration.imports` or a Boot-4-only annotation, that would force a split on the **Boot** axis. Decide after the Boot-4 test module exists.
3. **Bump the production floor + swagger pin.** Whether to raise `springdoc-openapi-starter-common` from 2.1.0 to a current 2.8.x (and `swagger-core-jakarta` to 2.2.47) for the compile, to stop the floor drifting far below what consumers resolve.
4. **Confirm swagger-core stays Jackson-2 in the springdoc 3.x line going forward** — the whole "OpenAPI axis is decoupled from our Jackson-3 decision" conclusion holds only while springdoc keeps pinning swagger-core 2.2.x. Re-check when/if springdoc adopts a Jackson-3 swagger-core major.
