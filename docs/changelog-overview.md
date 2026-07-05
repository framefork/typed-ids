# Compatibility research — overview & synthesis

> Index and cross-cutting synthesis of the per-library research compiled 2026-06-28 for the typed-ids newer-versions compatibility effort.
> This phase is research only — it maps what changed upstream and what it means for our integration. The "how do we restructure to solve it" decisions come next; open questions are surfaced here, not decided.

## Since the 2026-07 research (re-checked 2026-11-14)

Each per-library doc has its own re-check section with the evidence (tags, source diffs, compile and runtime probes). This section is the cross-library summary. Statements in the 2026-06-28 text below that the re-check proved wrong are corrected inline and marked "(corrected 2026-11-14)".

### What still holds

- **Jackson: no new module.** The Jackson 3 SPI we implement is unchanged 3.0.2 → 3.3.0, and the Jackson 2 SPI is unchanged 2.21.0 → 2.23.0. `typed-ids-jackson3` round-trips on databind 3.3.0, and `typed-ids-jackson2` on 2.18.1 and 2.23.0 (runtime probes).
- **Hibernate 7.x: no new module.** `-72` compiles unchanged against 7.2.25, 7.3.13 and 7.4.15. There is no 7.5; `main` is 8.1.0-SNAPSHOT. The `getGeneratedType()` fix for the 7.4 generator-type validation still holds: the check is identical 7.4.4 → 7.4.15.
- **Spring Boot: no new Hibernate module and no new boundary.** No new mid-line Hibernate crossing. Every Boot 4.0.1–4.1.2 release and the 4.2 pre-releases map to `-72` + `jackson3` (4.2: Java 17, Hibernate 7.4, Jackson 3.1, Spring Framework 7.1, Spring Data 2026.1). Starters are one per Boot minor (decided 2026-11-14, see below), so Boot 4.1 and 4.2 get their own `typed-ids-spring-boot-41`/`-42`, which pull the same modules with higher floors. Boot 4.2.0 GA is not tagged yet. The `@AutoConfiguration`, `.imports`, `@ConfigurationProperties` and `ApplicationConversionService` surface is unchanged 4.0.0 → 4.2.0-RC2. Boot 3.5.x had no release after 3.5.16 (2026-06-25), so block C ends there.
- **Boot 4.2 still manages Jackson 2** (2.21.8) for the `spring-boot-jackson2` stop-gap, deprecated for removal since 4.0.0. The earliest removal is 4.3.
- **Gson 2.15.0: no impact.** It still has no ServiceLoader. The floor 2.10.1 stays.
- **Boot 3 / springdoc 2.x line: unaffected.** springdoc 2.9.x/2.10.0 stay on `swagger-core-jakarta` 2.2.x, because swagger-core 3.0 needs Jakarta EE 11. The Boot 3 starters (`typed-ids-spring-boot-30..35`) and `testing-typed-ids-springdoc-2x-openapi` keep working.

### What broke

- **swagger-core 3.0.0 (2026-10-28) depends on Jackson 3** (`tools.jackson` 3.2.3) instead of Jackson 2, and requires Java 17 and Jakarta EE 11. It renamed its artifacts inside the same groupId (`swagger-core-jakarta` → `swagger-core`, same for `-models` and `-annotations`) with no relocation, no Gradle capability, and a BOM that does not manage the old IDs. The SPI our `ModelConverter` uses is source-identical 2.2.55 ↔ 3.0.0. Two things still break:
  - `TypedIdsSchemaUtils.rawClassOf` checks for the Jackson 2 `SimpleType`. Under 3.0, `ModelResolver` passes a `tools.jackson.databind.JavaType`, so `rawClassOf` returns null and the converter skips the id. The result is a wrong schema with no error (inferred from source, not run). Without Jackson 2 databind on the classpath the check throws `NoClassDefFoundError`.
  - Our `api(swagger-core-jakarta:2.2.35)` lands next to springdoc's `swagger-core:3.0.0`: the same classes in two jars, built on different Jackson majors. It also pulls Jackson 2 databind into Boot 4 apps.
- **springdoc 3.2.1 switches the Boot 4 line to swagger-core 3.0.0 in a patch release** (on `main` as 3.2.1-SNAPSHOT, commit `7091d82`). The `spring-boot-40` starter pulls our OpenAPI module, so it is hit directly. springdoc 3.0.x–3.2.0 still work with the current jar. No springdoc targets Boot 4.2 yet; the first one will very likely use swagger-core 3.0 (inference).
- **Hibernate 8.0 breaks more than `beforeAssemble`.** Since 8.0.0.Beta2, `SqmExpressible` moved to `org.hibernate.query.sqm.spi` and `SqmDomainType` to `org.hibernate.query.sqm.tree.spi.domain`. `ImmutableType` implements both. The 8.0 migration guide does not mention the move. `-72` fails with 12 errors against 8.0.0.CR1; two import changes plus removing the `beforeAssemble` override make it compile. So 8.0 needs its own small `-hibernate-80` module.
- **The databind#5318 rationale in `typed-ids-jackson3` is wrong.** The bigint `ValueDeserializer` fallback is still required, but for another reason: Jackson 3's `VisibilityChecker.DEFAULT` requires a public constructor for deserialization (single-arg at least package-private), and typed ids have a private single-`long` constructor. The code comment in `ObjectBigIntIdJacksonModule` (jackson3 package) still cites #5318.

### Modules that may be needed

- **`typed-ids-openapi-swagger3`** — decided 2026-11-14: one OpenAPI module per swagger-core major. `typed-ids-openapi-swagger2` replaces `typed-ids-openapi-swagger-jakarta` (swagger-core-jakarta 2.2.x, Jackson 2); `typed-ids-openapi-swagger3` is new (swagger-core 3.0, Jackson 3, `rawClassOf` fixed for the Jackson 3 `JavaType`) and ships in 0.12.0. Both declare swagger-core `compileOnly`, because a Boot starter cannot know the app's swagger-core line: springdoc is not in the Boot BOM and switches swagger-core lines inside its 3.x patch line. Each auto-config activates only when its own swagger-core line is present (the check is still to prototype). Boot 3 starters pull `-swagger2`; Boot 4 starters pull both, and the app's springdoc decides which activates.
- **`typed-ids-spring-6` / `typed-ids-spring-7`** — decided 2026-11-14: the converter follow-up (PR #22) gets one module per Spring Framework major instead of going into the starters. `-spring-6` compiles against spring-core 6.0.2, `-spring-7` against 7.0.2. The SPI is erasure-identical, but the split lets 1.0.0 drop Spring 6 cleanly and lets each auto-config compile against one Boot major.
- **Starters per Boot minor** — decided 2026-11-14: `typed-ids-spring-boot-30, -31, -32, -33, -34, -35, -40, -41, -42`, each pinning the floors its minor's first release manages (see `spring-boot-compat-matrix.md` §5). `-42` stays unpublished until Boot 4.2.0 GA.
- **`typed-ids-hibernate-80`** — decided 2026-11-14: add `typed-ids-hibernate-80` to the repo now, built and tested against the newest 8.0 pre-release (8.0.0.CR1), but not published. It applies `org.framefork.build.library-internal` instead of `library-published` (publishing is chosen per module by that convention plugin; `jreleaser.yml` lists no modules). Once Hibernate 8.0 is GA, switch the catalog to the GA version and the module to `library-published`, and it joins the released set. 8.0.0.CR1 (2026-11-12) depends on the `jakarta.persistence-api` 4.0.0-M8 milestone. Hypersistence Utils has no `hibernate-74` or `hibernate-80` artifact. **Decided 2026-11-14 by Filip: drop the Hypersistence Utils (HU) test dependency completely** and replace it with framefork's own Hibernate testing packages in a separate repository, with primitives for testing against Hibernate ORM 6.1 through 8.0. Today typed-ids uses only two HU classes: `io.hypersistence.utils.test.AbstractHibernateTest` (10 imports) and `io.hypersistence.utils.test.providers.DataSourceProvider` (15 imports), in three files of each `typed-ids-hibernate-{61,62,63,70,72}-testing` module (`AbstractContainerDataSourceProvider`, `AbstractMySQLIntegrationTest`, `AbstractPostgreSQLIntegrationTest`); the other HU references are build-file dependencies and catalog pins (`hypersistence-utils-hibernate{61,62,63,70,71,73}`). This removes the per-Hibernate-line HU artifactId pins, the frozen 3.9.4 pins for 6.1/6.2, the `hibernate-70` artifact that stopped at 3.15.3, and the missing `hibernate-74`/`hibernate-80` artifacts. `io.hypersistence:hypersistence-tsid` is a different artifact (the TSID generator, `compileOnly` in core) and is not affected. Scope (Filip, 2026-11-14): the new library replaces both the typed-ids testing classes and the HU primitives they build on. It is written from scratch for what typed-ids' tests use; HU may inform which problems need solving, but no HU code is copied. The five `typed-ids-hibernate-{61,62,63,70,72}-testing` modules contain the same five classes, byte-identical across all five (`AbstractContainerDataSourceProvider`, `MySQLDataSourceProvider`, `PostgreSQLDataSourceProvider`, `AbstractMySQLIntegrationTest`, `AbstractPostgreSQLIntegrationTest`, 263 lines in total), and none of them references a typed-ids type; only the package name `org.framefork.typedIds.hibernate.tests` ties them to typed-ids. What differs per Hibernate line is only the build file (the `hibernate-core` version and the HU artifact). After the move, a `typed-ids-hibernate-NN-testing` module holds no sources, only wiring (`typed-ids-testing`, `testFixtures(typed-ids-hibernate-NN)` and the new library's artifact for that Hibernate line), so the five testing modules are deleted (step 7 below). Their remaining dependencies (`typed-ids-testing`, the Hibernate version, `hibernate-orm-testing-NN`) move onto each `typed-ids-hibernate-NN` module's test fixtures via `testFixturesApi(...)`, and the 19 consumers depend on `testFixtures(project(":typed-ids-hibernate-NN"))` only. The test-fixtures variant stays unpublished, because its metadata would reference the unpublished `typed-ids-testing`. The new repository is `framefork/hibernate-orm-testing` (groupId `org.framefork.testing`, package `org.framefork.testing.hibernate`, first version 0.1.0), with a shared Hibernate-independent module `hibernate-orm-testing-database` (Testcontainers-backed `DataSource` providers for PostgreSQL and MySQL) and per-line modules `hibernate-orm-testing-61/62/63/70/72/80`; `typed-ids-hibernate-80` comes after it and needs no `-80-testing` module.

### Risks that are open but not breaking

- **jackson-annotations floor.** databind 3.2+ needs `jackson-annotations` ≥ 2.22 and fails with `NoClassDefFoundError: JsonApplyView` otherwise. swagger-core 2.2.52 declares 2.21, so a Maven app without the Boot BOM can resolve too low a version. swagger-core 2.2.53+ and 3.0.0 declare 2.22.
- **Hibernate 8.0, unverified:** `SequenceStyleGenerator.configure` now reads `creationContext.getDatabase().getJdbcEnvironment()`. Our `TypeOverrideGeneratorCreationContext` passes `getDatabase()` through, but only a test run proves it. `GeneratorCreator` is still `@Internal`, and `DynamicParameterizedType` is still deprecated for removal.
- **Spring `Converter` generics for the converter follow-up (`typed-ids-spring-6`/`-7`).** From Spring Framework 7.0.2, `Converter<S, T extends @Nullable Object>`; from 7.0.3, `ConverterFactory.getConverter` returns `Converter<S, ? extends @Nullable T>`. The erasure is unchanged, so one jar stays binary compatible with 6.x and 7.x. To compile against both, an implementation declares the narrow `T convert(S)` return type. PR #22 already does. `typed-ids-spring-7` compiles against 7.0.2, so its NullAway JSpecify check sees the 7.0.2 `Converter` generics.
- **springdoc 3.2.0 opt-in `@Nullable` customizer** marks `$ref` properties nullable too. With `framefork.typed-ids.openapi.as-ref=true` our ids are `$ref`s, so check the output if a user enables it (release notes only).

### Open decisions

1. ~~**OpenAPI under swagger-core 3.0**~~ — decided 2026-11-14: neither option as written. One module per swagger-core major (`typed-ids-openapi-swagger2`, `typed-ids-openapi-swagger3`), both with swagger-core `compileOnly`: the shape of (a) with the dependency scope of (b). The options as they were:
   - (a) A second module, for example `typed-ids-openapi-swagger` on swagger-core 3.0.0 and Java 17, with the same shape as `jackson2`/`jackson3`. It needs its own copy of the folded-in Boot auto-config, and `spring-boot-40` switches to it.
   - (b) One jar with swagger as `compileOnly` and a `rawClassOf` that handles either Jackson major. Every symbol we call has the same signature in both lines (source diff only, not run).
2. ~~**When to ship `-hibernate-80`**~~ — decided 2026-11-14: add `typed-ids-hibernate-80` to the repo now, built and tested against the newest 8.0 pre-release (8.0.0.CR1), but not published. It applies `org.framefork.build.library-internal` instead of `library-published` (publishing is chosen per module by that convention plugin; `jreleaser.yml` lists no modules). Once Hibernate 8.0 is GA, switch the catalog to the GA version and the module to `library-published`, and it joins the released set.
3. ~~**Whether PR #31 raises test pins to the newest patches**~~ — decided 2026-11-14: not in PR #31. The raises are their own PR right after #31 (step 2 below): Hibernate v66 6.6.59, v73 7.3.13, v74 7.4.15 and a Gson test at 2.15.0, without the HU raise (HU is dropped) and without starter changes. The question as it was (Hibernate v66 6.6.38 → 6.6.59, v73 7.3.11 → 7.3.13, v74 7.4.4 → 7.4.15; HU `hibernate-73` 3.15.4 → 3.16.0; gson test target 2.14.0 → 2.15.0). The "no blanket floor bumps" rule covers floors, not test targets.

### Release plan (decided 2026-11-14)

- 0.11.x keeps shipping from master. 0.12.0 contains the restructure (PR #31), the test-version raises, the OpenAPI split, the per-minor starters, the `typed-ids-spring-6`/`-7` converter modules, the switch from Hypersistence Utils to the new `framefork/hibernate-orm-testing` library (which deletes the `typed-ids-hibernate-NN-testing` modules), and the unpublished `typed-ids-hibernate-80`. Every new artifact is added during 0.x.
- Each step is a separate PR into master, merged once it implements its step correctly and CI is green. The steps run strictly in sequence in the order 1, 6, 2–5, 7–10; the only overlap is that steps 2–5 proceed while Filip reviews `hibernate-orm-testing`, and step 7 waits until `hibernate-orm-testing` 0.1.0 is on Maven Central:
  1. PR #31: the restructure (JSON modules out of core, Jackson 3, Hibernate 7.3/7.4 under `-72`, starters `-30/31/32/40`).
  2. Test-version raises: Hibernate 6.6.59 / 7.3.13 / 7.4.15, a Gson test at 2.15.0; no HU, no starter changes.
  3. OpenAPI split into `typed-ids-openapi-swagger2` / `-swagger3`, plus the databind#5318 comment fix in `typed-ids-jackson3`.
  4. Per-minor starters `-33/34/35/41/42`, each starter test on the newest patch of its own minor (`-40` at 4.0.9).
  5. PR #22 reworked into `typed-ids-spring-6` / `-7`, Jan Loufek staying the author.
  6. Right after step 1: the new repository `framefork/hibernate-orm-testing` (`hibernate-orm-testing-61/62/63/70/72/80`), released before step 7.
  7. typed-ids switches to `hibernate-orm-testing`: Hypersistence Utils and the `typed-ids-hibernate-NN-testing` modules go.
  8. `typed-ids-hibernate-80` on 8.0.0.CR1, unpublished.
  9. 0.12.0 prepared (master green, release notes drafted), released by Filip.
  10. 1.0.0 PR (Boot 4 only), green and left open until Filip says go.
- `typed-ids-hibernate-72` covers Hibernate 7.2–7.4; the 73/74 modules are test modules only.
- Outside the 0.12.0 gate, because they wait on upstream releases: the springdoc 3.2.1 end-to-end test for `typed-ids-openapi-swagger3`, publishing `typed-ids-hibernate-80` (Hibernate 8.0 GA) and publishing `typed-ids-spring-boot-42` (Boot 4.2.0 GA).
- 1.0.0 supports Spring Boot 4 only. It drops `typed-ids-spring-boot-30..35`, `typed-ids-spring-6`, `typed-ids-hibernate-61/62/63`, and `typed-ids-hibernate-70` (only Boot 4.0.0 manages Hibernate 7.0/7.1, and no starter covers 4.0.0). It keeps `typed-ids-openapi-swagger2`, because springdoc 3.0–3.2.0 on Boot 4 still use swagger-core 2.2.x.
- 1.0.0 keeps `typed-ids-jackson2`: Boot 4 still has the deprecated `spring-boot-jackson2` stop-gap (earliest removal 4.3), Jackson 2.21 is LTS until 2028-01, and the module has no Boot dependency.
- Boot 3.x users stay on the 0.12.x line, maintained on the `release-0.x` branch (bugfixes only, no new features), which the 1.0.0 release notes name.

### Corrections to the 2026-06-28 text below

- "Only two libraries need source changes": swagger-core 3.0 is a third.
- "Hibernate 7.3 and 7.4 change nothing we implement": the SPI is unchanged, but 7.4.0.CR1 added generator-type validation (HHH-18210) that rejected our bigint sequence generator, which needed the `getGeneratedType()` fix in `-72`.
- "Hibernate 8.0 has exactly one break": it has three (`beforeAssemble` and the two moved interfaces).
- "The OpenAPI stack is decoupled from Jackson 3": true only up to springdoc 3.2.0 / swagger-core 2.2.x.
- Catalog-hygiene numbers: newer swagger-core, springdoc and HU releases, and the HU `hibernate-70` artifact stops at 3.15.3.
- "Jackson 2.22 is the last planned 2.x minor" (in `changelog-jackson.md`): 2.23.0 shipped 2026-11-07 and 2.24 is in progress.

## The per-library docs

| Doc | Library | Is it a real code dependency for us? | Headline verdict |
|---|---|---|---|
| [changelog-jackson.md](changelog-jackson.md) | Jackson | **Yes — production** (`compileOnly` in core module) | **The main breaking axis.** 2.x bumps are free; 3.0 renames our exact base types → needs a parallel Jackson-2/Jackson-3 flavor. |
| [changelog-hibernate.md](changelog-hibernate.md) | Hibernate ORM | **Yes — production** (per-version modules) | 7.3/7.4 leave the SPI unchanged → extend the `-72` range with test modules, no new modules; 7.4 still needed a behavior fix in `-72` (generator-type validation). 8.0 breaks `beforeAssemble` plus two moved interfaces → a future `-hibernate-80` module. *(corrected 2026-11-14)* |
| [changelog-spring-boot.md](changelog-spring-boot.md) | Spring Boot | No — **test-only**, but the matrix *driver* | Its major dictates the transitive Jackson/Hibernate/Data/Framework versions. The BOM is the spine of our test matrix. |
| [changelog-spring-framework.md](changelog-spring-framework.md) | Spring Framework | No — **transitive-only** | One stable `@Bean` usage, no converter SPI. Not an independent axis. |
| [changelog-spring-data.md](changelog-spring-data.md) | Spring Data (JPA + Commons) | No — **test-only** | Byte-identical 3x/4x test sources; every 4.0 break lands on SPI we don't touch. A derived column of the Boot row. |
| [changelog-springdoc.md](changelog-springdoc.md) | SpringDoc OpenAPI | **Yes — production** (auto-config module) | Consumes zero `org.springdoc.*` API. 3.0.x–3.2.0 are a Boot-4 rebaseline on swagger-core 2.2.x; **3.2.1 moves to swagger-core 3.0 (Jackson 3)** in a patch release. *(corrected 2026-11-14)* |
| [changelog-swagger-core.md](changelog-swagger-core.md) | Swagger Core (v3 Jakarta) | **Yes — production** (`ModelConverter`) | 2.2.x stays on Jackson 2 (our integration unchanged 2.2.35 → 2.2.55). **3.0.0 depends on Jackson 3 instead of Jackson 2 and renamed the artifacts**; our SPI calls are source-identical, but `rawClassOf` and the dependency coordinates break. *(corrected 2026-11-14)* |
| [changelog-hypersistence-utils.md](changelog-hypersistence-utils.md) | Hypersistence Utils | No — **test-only** (+ tsid `compileOnly`) | Added a `hibernate-73` module (3.15.0) targeting Hibernate 7.4.0 → unblocks `-73/-74` test coverage. No `hibernate-74`/`-80` artifact as of 3.16.0. |

## The single most important realization

Of the eight libraries, **three require actual source changes to our shipped code** *(corrected 2026-11-14: the 2026-06-28 text said two; swagger-core 3.0 is the third, see point 3)*, and they are on different timelines:

1. **Jackson 2 → 3** — the *real* work. Jackson 3.0 renames exactly the databind/core types our two modules are built on (`Module`→`JacksonModule`, `JsonSerializer`→`ValueSerializer`, `JsonDeserializer`→`ValueDeserializer`, `SerializerProvider`→`SerializationContext`, package `com.fasterxml.jackson.{core,databind}`→`tools.jackson.{core,databind}`, plus changed SPI callback signatures incl. `findSerializer`'s new 4th param). A single `.class` cannot extend both `JsonSerializer` and `ValueSerializer` — so supporting both means **parallel source sets / separate artifacts**, mirroring how the per-Hibernate modules already work.

2. **Hibernate 8.0** — *future, small*. `org.hibernate.type.Type.beforeAssemble(...)` is removed in 8.0.0.Beta1, and our `ImmutableType` `@Override`s it. Since 8.0.0.Beta2, `SqmExpressible` and `SqmDomainType` (both implemented by `ImmutableType`) also moved package. Not actionable until 8.0 is GA. Hibernate 7.3 and 7.4 leave the SPI we implement unchanged (only additive `default` methods), but 7.4's generator-type validation needed a behavior fix in `-72`. *(corrected 2026-11-14)*

3. **swagger-core 3.0** — *now, small*. *(added 2026-11-14)* The `ModelConverter` SPI is source-identical, but `rawClassOf` checks the Jackson 2 `SimpleType` and our dependency uses the retired `swagger-core-jakarta` coordinates. springdoc 3.2.1 brings swagger-core 3.0 to every Boot 4 app.

**Everything else is a free version bump, a test-matrix addition, or genuinely decoupled.** The whole Spring stack (Framework/Data) is the version-management machinery delivered transitively through the Spring Boot BOM. *(corrected 2026-11-14: the 2026-06-28 text also called the OpenAPI stack decoupled from the Jackson-3 decision; that held only until swagger-core 3.0 / springdoc 3.2.1.)*

## The matrix spine: Spring Boot version → transitive versions

This is the backbone of our compatibility/test matrix (all read from the `spring-boot-dependencies` BOM at git tags):

| Spring Boot | Spring Framework | Hibernate ORM | Spring Data JPA | Jackson | Jakarta EE | Java floor |
|---|---|---|---|---|---|---|
| **3.5.8** (our current) | 6.2.14 | 6.6.36 | 3.5.x | **2.19.4** | 10 | 17 |
| **4.0.0** | 7.0.1 | 7.1.8 | 4.0.0 | **3.0.2** (+ Jackson-2 mgmt 2.20.1) | 11 | 17 |
| **4.0.7** | 7.0.8 | 7.2.19 | 4.0.6 | 3.0.x→3.1.x | 11 | 17 |
| **4.0.9** | 7.0.10 | 7.2.25 | 4.0.x (Data BOM 2025.1.8) | 3.1.7 | 11 | 17 |
| **4.1.0** | 7.0.8 | 7.4.1 | 4.1.0 | **3.1.4** | 11 | 17 |
| **4.1.2** | 7.0.10 | 7.4.13 | 4.1.x (Data BOM 2026.0.2) | 3.1.7 | 11 | 17 |
| **4.2.0-RC2** (pre-release) | 7.1.0-RC2 | 7.4.14 | 4.2.x (Data BOM 2026.1.0-RC1) | 3.1.8 | 11 | 17 |

Two subtleties that bite if ignored (both from the Spring Boot doc):
- **Boot 4.0.x slides Hibernate across a *minor* inside its own patch line** — 4.0.0 = 7.1.8 but 4.0.1 = 7.2.0, 4.0.7 = 7.2.19. Our `4x-data-indexed` test module is pinned at *exactly* 4.0.0 precisely to keep it on Hibernate 7.1 to match `:typed-ids-hibernate-70`. That pin is load-bearing, not incidental.
- **Jackson also slides 3.0→3.1 inside Boot 4.0.x**, and Boot 4 keeps Jackson-2 dependency management alive for the deprecated `spring-boot-jackson2` stop-gap.

## Which axes are independent (what actually multiplies the matrix)

- **Hibernate axis** (our existing per-version modules: 6.1 / 6.2 / 6.3 / 7.0 / 7.2 → + 7.3/7.4 coverage → eventually 8.0). Real, additive, and the cheapest to extend.
- **Jackson axis** (Jackson 2 flavor vs Jackson 3 flavor) — **new** axis the work introduces; the only one needing genuinely duplicated source.
- **Spring Boot axis** (3.5 vs 4.0/4.1) — exists only in *test* modules and is really just a convenient way to pin a whole transitive stack at once; it largely *coincides* with the Jackson axis (Boot ≤3.5 ↔ Jackson 2, Boot ≥4.0 ↔ Jackson 3).
- **OpenAPI/Swagger** — follows the Jackson axis since swagger-core 3.0. springdoc 2.x (Boot 3) stays on swagger-core 2.2.x / Jackson 2; springdoc 3.0.x–3.2.0 (Boot 4) also stay on 2.2.x; springdoc 3.2.1+ moves to swagger-core 3.0 / Jackson 3. Our SPI calls are the same source in both lines, but the jar is not (see open decision 1 in the re-check section). *(corrected 2026-11-14)*

So the matrix is essentially **Hibernate-version × Jackson-major**, with Spring Boot as the test harness that fixes the transitive stack. OpenAPI follows the Jackson major once springdoc ≥ 3.2.1 is in play. *(corrected 2026-11-14)*

## Consolidated open decisions for the next ("how to solve it") phase

These are surfaced by the research, not decided here:

1. **Jackson flavor split — the central design question.** One module with two source sets, or two published artifacts (e.g. `-jackson2` / `-jackson3`)? Needs distinct packages / `@AutoService(JacksonModule.class)` so the two service registrations can coexist on a classpath without colliding. Candidate floors/ceilings: Jackson-2 flavor floor stays ≤2.18 (preserve old upgrade paths), tested to 2.21 (LTS)/2.22 (2.23.0 is out since 2026-11-07, and the jackson2 module round-trips on it); Jackson-3 flavor floor 3.0, target 3.1 (LTS) (3.3.0 is out, and the jackson3 module round-trips on it).
2. **Hibernate range extension.** Add `testing-…-73-indexed` and `-74-indexed` against the existing `-72` module (SPI unchanged), backed by `hypersistence-utils-hibernate-73:3.15.3` (targets Hibernate 7.4.0, covers both). Pending a green CI run. Separately, track Hibernate 8.0 (the `beforeAssemble` removal plus the moved `SqmExpressible`/`SqmDomainType`) as a later module.
3. **Add a Boot-4 / springdoc-3 test module.** Today only Boot 3.5 + springdoc 2.8.14 is exercised; there's no Boot-4/springdoc-3.0 counterpart yet.
4. **Version-catalog hygiene.** Production floors drifted below what the ecosystem now resolves: `swagger-core-jakarta` 2.2.35 (latest 2.2.55 on the 2.x line; 3.0.0 exists under the new `swagger-core` ID), `springdoc-…-common` 2.1.0 (vs 2.10.0 on the Boot 3 line), Hypersistence `hibernate63/70/71` at 3.13.3 (`hibernate63`/`hibernate71` movable to 3.16.0; `hibernate70` only to 3.15.3, the last release that ships that artifact). *(corrected 2026-11-14)* **Keep** the `3.9.4` pins for `hibernate61/62` — they are the last HU release shipping those now-dropped modules, required to preserve the old upgrade paths.
5. **JDK floor.** Jackson 3 and Spring Framework 7 both require Java 17 — we are already there, so no floor change is forced; the Jackson-3 flavor simply inherits the 17 baseline.

## Cross-cutting preserve-the-old-paths principle

Every doc was framed around Filip's explicit goal: **newer-version support must be additive, never a replacement.** The older Hibernate modules (6.1+), the Boot-3.5 / Jackson-2 / springdoc-2.8 stack, and the frozen HU `3.9.4` pins all stay. The work adds a Jackson-3 flavor and 7.3/7.4 (later 8.0) Hibernate coverage alongside what exists — letting users take small, safe upgrade steps rather than forcing a jump to the newest stack.
