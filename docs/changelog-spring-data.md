# Changelog research: Spring Data (JPA + Commons)

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/spring-projects/spring-data-jpa` git tags — release-date verification via `git log -1 --format=%ci <tag>` for the full `3.5.x` line (`3.5.0` … `3.5.13`), `4.0.0` … `4.0.6`, and `4.1.0` (+ its `-M1`/`-M2`/`-RC1` milestones). Latest GA `spring-data-jpa` is **4.1.0**; latest 3.5.x is **3.5.13**.
> - `github.com/spring-projects/spring-data-commons` git tags — same train numbers as JPA (`3.5.x`, `4.0.x`, `4.1.0`); Commons and JPA are versioned and released in lockstep.
> - `github.com/spring-projects/spring-data-commons` wiki — `Spring-Data-2025.1-Release-Notes.asciidoc` (= the 4.0 train, **the major boundary**) and `Spring-Data-2026.0-Release-Notes.asciidoc` (= the 4.1 train). The wiki uses release-train names; the per-module version is given in each section heading (e.g. "Spring Data Commons - 4.0", "Spring Data JPA - 4.0").
> - `spring-data-commons` / `spring-data-jpa` **source diffs** across `3.5.8` → `4.0.0` → `4.1.0` of the exact types our test code consumes: `org.springframework.data.repository.{Repository,CrudRepository}`, `org.springframework.data.jpa.repository.JpaRepository`, `org.springframework.data.jpa.repository.config.EnableJpaRepositories`.
> - typed-ids source: `testing/testing-typed-ids-spring-boot-3x-data-indexed/`, `testing/testing-typed-ids-spring-boot-4x-data-indexed/`, `gradle/libs.versions.toml`, `settings.gradle.kts`, and a repo-wide grep for `org.springframework.data.*` imports.
> - Cross-reference: the sibling `docs/changelog-spring-boot.md` spine table (Boot 3.5 → Spring Data Bom `2025.0` → JPA 3.5.x; Boot 4.0 → `2025.1` → JPA 4.0.0; Boot 4.1 → `2026.0` → JPA 4.1.0).

## TL;DR for typed-ids

- **Spring Data is purely a test harness, not a production integration surface.** There is **no `org.springframework.data:spring-data-*` coordinate anywhere** in `gradle/libs.versions.toml` or any module's `build.gradle.kts`. It reaches the classpath only because the two `*-data-indexed` test modules pull `org.springframework.boot:spring-boot-starter-data-jpa`. typed-ids extends **no** Spring Data SPI — it implements no `RepositoryFactory`, no custom `QueryEnhancer`, no `Converter`, no `EntityInformation`, nothing.
- **What the tests actually consume is a tiny, rock-stable slice of the Spring Data surface:** `JpaRepository<Entity, Entity.Id>` (typed-id as the **ID type parameter**), `@EnableJpaRepositories(basePackages=…)`, a derived `findByTitle(...)` finder, and the inherited CRUD methods `findById` / `saveAll` / `findAllById`. (The `@Repository` on the repository interfaces is `org.springframework.stereotype.Repository` — **Spring Framework**, not Spring Data.)
- **The "indexed" in the module name is a typed-ids concept, not a Spring Data one.** Both modules apply `annotationProcessor(project(":typed-ids-index-java-classes-processor"))`, which emits a `META-INF/services` index of the typed-id classes. The test proves a typed-id value type works as a JPA `@Id` **accessed through a Spring Data `JpaRepository`**, and asserts the physical DB column type (`uuid` / `bigint`) via `INFORMATION_SCHEMA`. The actual id↔column mapping is **Hibernate's** `UserType`/`JavaType` (covered by `docs/changelog-hibernate.md`) — Spring Data only forwards the id object to the `EntityManager`.
- **The 3.x and 4.x test sources are byte-identical.** Diffing `testing-typed-ids-spring-boot-3x-data-indexed` against `-4x-data-indexed` shows the `SpringDataTestConfiguration`, the `AbstractSpringData*Test` base classes, and every repository interface are **the same file**. The identical Spring Data code compiles and runs unchanged on Spring Data JPA **3.5.x (under Boot 3.5.8)** and **4.0.0 (under Boot 4.0.0)** — the clearest possible evidence that our surface spans the major untouched.
- **Source-verified: the major (3.5 → 4.0) does not change a single symbol we touch.** `JpaRepository` (declaration, `extends` clause, all method names) and `CrudRepository` are **identical** at `3.5.8` and `4.0.0`; `EnableJpaRepositories` gains exactly **one new optional attribute** (`queryEnhancerSelector()`, with a default), which our `basePackages`-only usage ignores. Every "⚠️ Breaking Change" in the 4.0 release notes lands on internal/SPI/integration-author surfaces (`PropertyPath`/`TypeInformation` package move, `DeclaredQuery`/`QueryEnhancer` revision, removed `@PersistenceConstructor`/SpEL/`ListenableFuture` APIs, the Criteria→JPQL derived-query rewrite) — **none of which typed-ids imports**.
- **Net assessment: low / no direct impact.** Like Spring Framework, Spring Data matters to us only as a *version Boot drags in*, and that coupling is already captured by the Boot axis. The honest conclusion is "test-only, no integration SPI, spans 3.5 → 4.0 → 4.1 with zero code change." The Spring Data version should be a **derived column of the Boot row**, not an independent module axis.

## How typed-ids uses Spring Data

### It's test-only — confirmed by the dependency graph

A repo-wide grep for `org.springframework.data.*` returns hits **only** under `testing/testing-typed-ids-spring-boot-{3x,4x}-data-indexed/src/test/`. The catalog (`gradle/libs.versions.toml`) declares **no** Spring Data coordinate; the only Spring-related pins are `springBoot = "3.5.8"` and the springdoc/OpenAPI entries. Both data modules get Spring Data purely transitively:

```kotlin
// testing-typed-ids-spring-boot-3x-data-indexed/build.gradle.kts
implementation(project(":typed-ids-hibernate-63"))
api(platform("org.springframework.boot:spring-boot-dependencies:3.5.8"))
api("org.springframework.boot:spring-boot-starter-data-jpa")          // ← drags in spring-data-jpa 3.5.x + commons 3.5.x
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.boot:spring-boot-testcontainers")

// testing-typed-ids-spring-boot-4x-data-indexed/build.gradle.kts
implementation(project(":typed-ids-hibernate-70"))
api(platform("org.springframework.boot:spring-boot-dependencies:4.0.0")) // ← drags in spring-data-jpa 4.0.0 + commons 4.0.0
api("org.springframework.boot:spring-boot-starter-data-jpa")
testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")   // ← new Boot-4 split test starter
testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")  // ← new Boot-4 split test starter
testImplementation("org.springframework.boot:spring-boot-testcontainers")
```

The 3x module tests `:typed-ids-hibernate-63`; the 4x module tests `:typed-ids-hibernate-70`. The only build-file difference relevant to Spring Data is that Boot 4 split the test slices into `spring-boot-starter-data-jpa-test` / `-data-jdbc-test` (a Boot-4 module-restructuring artifact, see `docs/changelog-spring-boot.md`), not a Spring Data change.

### The exact Spring Data surface we consume (all in test code)

| Symbol | Package | Where / how used |
|---|---|---|
| `JpaRepository<T, ID>` | `org.springframework.data.jpa.repository` | every `*EntityRepository` extends it with the **typed-id type as `ID`**, e.g. `extends JpaRepository<UuidAppGeneratedExplicitMappingEntity, UuidAppGeneratedExplicitMappingEntity.Id>` |
| `@EnableJpaRepositories(basePackages=…)` | `org.springframework.data.jpa.repository.config` | once, in `SpringDataTestConfiguration` |
| derived finder `findByTitle(String)` | (query derivation) | one custom method per repository — exercises Spring Data's query-method derivation with a typed-id-keyed entity |
| inherited `findById(ID)` / `saveAll(…)` / `findAllById(Iterable<ID>)` | `CrudRepository` (via `JpaRepository`) | the test body round-trips typed-id keys through these |
| `@Repository` | `org.springframework.stereotype` (**Spring Framework**, not Data) | stereotype on the repository interfaces |

Supporting Boot test wiring (not Spring Data per se): `@DataJpaTest`, `TestEntityManager`, `@AutoConfigureTestDatabase`, `@AutoConfigureTestEntityManager`, `@ServiceConnection` + Testcontainers (Postgres 16.4 / MySQL 8.4). These exercise typed-ids **through** Spring Data; they are not integration points typed-ids exposes.

### What the test proves (and what "indexed" means)

`ObjectUuidTypeIndexedSpringDataJpaPostgreSQLTest` / `…MySQLTest` (and the BigInt equivalents) do two things:

1. **Schema assertion** — a native `INFORMATION_SCHEMA.COLUMNS` query confirms the `id` column's physical type is `uuid` (UUID ids) / `bigint` (BigInt ids). This is a **Hibernate** mapping outcome, surfaced via JPA — Spring Data is not involved in the column type.
2. **Repository round-trip** — `saveAll(...)`, then `findById(typedId)`, `findByTitle(...)`, `findAllById(collectionOfTypedIds)`, asserting the typed-id keys survive persistence and lookup through the `JpaRepository`.

"**indexed**" refers to typed-ids' own `typed-ids-index-java-classes-processor` annotation processor (applied via `annotationProcessor`/`testAnnotationProcessor`), which builds a compile-time `META-INF/services` index of the typed-id classes — **not** a database index and **not** a Spring Data feature. The Spring Data angle is simply "are these typed-id value types usable as `@Id` via a `JpaRepository`?" — and the answer is yes, identically on 3.5 and 4.0.

### What is NOT there (the load-bearing negative result)

| Candidate Spring Data SPI a typed-id lib *could* couple to | Package | Present in typed-ids? |
|---|---|---|
| `Converter<S,T>` for the id type (custom id↔store conversion) | `org.springframework.data.convert` | **No** |
| `EntityInformation` / `JpaEntityInformation` (custom id introspection) | `org.springframework.data.repository.core` / `…jpa.repository.support` | **No** |
| Custom `RepositoryFactoryBean` / `RepositoryFactorySupport` | `org.springframework.data.repository.core.support` | **No** |
| Custom `QueryEnhancer` / `QueryEnhancerSelector` (the 4.0 SPI revision) | `org.springframework.data.jpa.repository.query` | **No** |
| `PropertyPath` / `TypeInformation` (the 4.0 package-relocated types) | `org.springframework.data.mapping` → `…data.core` | **No** |

This is the point: typed-ids does **not** plug into Spring Data's identifier-handling, conversion, or query-parsing internals. For JPA, Spring Data treats the `@Id` opaquely — it hands the id object to the `EntityManager`, and Hibernate's `UserType`/`JavaType` does the id↔column mapping. So the surfaces the 4.0 major actually churned (mapping/conversion internals, query enhancement) are exactly the ones we don't touch.

## Version timeline (newer-than-ours)

Dates are git **tag commit dates** (`git log -1 --format=%ci`). JPA and Commons share the train number and ship together, so a single version covers both. "Ours" = whatever Boot pins: **JPA/Commons 3.5.x** under the Boot-3.5.8 module, **4.0.0** under the Boot-4.0.0 module.

### 3.5.x — the current 3.x maintenance line (3.5.0 GA 2025-05-16; latest 3.5.13, 2026-06-24)

- The 3.5 train (release-train `2025.0`) is the "classic" Spring Data: Spring Framework 6.2.x, JPA 3.1/Hibernate 6.6.x, Java 17, Jakarta EE 10. Boot **3.5.8** manages Spring Data Bom `2025.0.6` (→ JPA 3.5.x); the latest Boot 3.5.x (3.5.16) manages `2025.0.13` (still JPA 3.5.x). Pure maintenance — bug fixes and dependency bumps, no API removals on the `CrudRepository`/`JpaRepository` surface.
- **Impact on typed-ids: none.** This is the line our 3x data module already runs on. Sliding the Boot-3.5 pin forward across the 3.5.x tail is a no-op for our test code (and there is no shipped code to affect).

### 4.0.0 — 2025-11-14 — **the major boundary** (GA; full deep dive below)

- First GA of the 4.x generation (release-train `2025.1`; milestones `4.0.0-M1` Jan 2025 … `4.0.0-RC2` Oct 2025). **Boot 4.0.0 (2025-11-20) pins `2025.1.0` → JPA 4.0.0**, so our 4x data module already runs on it.
- Headline baseline bumps (from the `2025.1` release notes): **Spring Framework 7**, **Jakarta EE 11** (JPA 3.2 with **Hibernate ORM 7.1/7.2**, Servlet 6.1), **Kotlin 2.2**, **JUnit 6**, **Jackson 3** *(REST module only)*. JDK floor stays **17**.
- Headline features: **AOT repositories** (codegen for query methods, on by default), **AOT-generated property accessors/instantiators**, **Vector Search methods** (`SearchResults`, `@VectorSearch`), and in JPA a **rewrite of derived queries from Criteria API to String-based JPQL** (cited ~25% throughput win in real apps).
- **Impact on typed-ids: effectively none for our code.** Verified by source-diffing the four types we consume across `3.5.8` → `4.0.0`: `JpaRepository` and `CrudRepository` are **unchanged** (copyright lines only); `Repository` is **unchanged**; `EnableJpaRepositories` adds exactly one optional `queryEnhancerSelector()` attribute that defaults and that we never set. The "⚠️ Breaking Change" items all sit on surfaces we don't import (see deep dive). It matters only as the Spring Data version Boot 4 brings, under which our Hibernate-70 module is integration-tested.

### 4.0.1 → 4.0.6 — the 4.0.x maintenance line (latest 4.0.6, 2026-06-09)

- Maintenance on 4.0. Boot 4.0.0 → JPA 4.0.0; Boot 4.0.7 (latest 4.0.x) → Spring Data Bom `2025.1.6` → JPA 4.0.6. So "support Boot 4.0.x" already implies "tested under Spring Data JPA 4.0.0 through 4.0.6."
- **Impact on typed-ids: none.** Nothing in the 4.0.x patch range touches the `JpaRepository`/`CrudRepository` CRUD surface or `@EnableJpaRepositories(basePackages)`.

### 4.1.0 — 2026-06-09 (GA; release-train `2026.0`)

- The 4.1 train. Milestones `4.1.0-M1` (2026-02-13), `-M2` (2026-03-13), `-RC1` (2026-04-17); the GA tag is dated **2026-06-09** (the release-notes "Release Dates" section optimistically lists "GA - 15 May 2026"; the cut tag is June). **Boot 4.1.0 pins `2026.0.0` → JPA 4.1.0.**
- Headline (from the `2026.0` notes): **type-safe property paths** (`PropertyPath.of(Person::getName)`, `Sort.by(Person::getFirstName, …)`), **`@ProjectedPayload` opt-in now required** for web projections (with a deprecation-logging migration window through 4.2), Kotlin 2.3.20 / Vavr 0.11.0 baselines, and in Relational a single-statement upsert. **The "Spring Data JPA - 4.1" section is empty** — no JPA-specific changes this train.
- **Impact on typed-ids: none.** `CrudRepository` only sees Javadoc edits between 4.0.0 and 4.1.0 (verified). The type-safe property paths and `@ProjectedPayload` opt-in are net-new opt-in features on surfaces we don't use. Relevant only as the version Boot 4.1 selects.

## Breaking-change deep dive: Spring Data 3.5 → 4.0 (release-train 2025.0 → 2025.1)

Framed for a consumer that uses only `JpaRepository` CRUD + one derived finder, with a value-object as the `@Id`. Each 4.0 "⚠️ Breaking Change" is triaged **affects us / irrelevant**. Source from `Spring-Data-2025.1-Release-Notes.asciidoc`, confirmed against the type diffs.

### 0. The surface we touch — source-verified stable

```
spring-data-commons  Repository.java        3.5.8 vs 4.0.0 → identical (copyright line only)
spring-data-commons  CrudRepository.java    3.5.8 vs 4.0.0 → identical (copyright line only); 4.0.0 vs 4.1.0 → Javadoc only
spring-data-jpa      JpaRepository.java     3.5.8 vs 4.0.0 → declaration, `extends` clause, and every method name identical
spring-data-jpa      EnableJpaRepositories  3.5.8 vs 4.0.0 → +1 optional attribute `queryEnhancerSelector()` (defaulted) — additive
```

The one additive `@EnableJpaRepositories` attribute is the API-visible tip of the `QueryEnhancer` revision below; because it has a default and we pass only `basePackages`, it is a no-op for us.

### 1. `PropertyPath` / `TypeInformation` package relocation — **irrelevant (we don't import them)**

4.0 moved `PropertyPath`, `PropertyReferenceException` (from `org.springframework.data.mapping`) and `TypeInformation`, `NullableWrapper(Converters)`, `ReactiveWrappers`, `CustomCollections` (from `org.springframework.data.util`) into a new `org.springframework.data.core` package. The notes flag this as transparent for app authors and breaking only for "an author of a Spring Data integration or maintainer of a Spring Data module." typed-ids is neither — it imports none of these. **No impact.**

### 2. `DeclaredQuery` / `QueryEnhancer` revision + `QueryEnhancerSelector` — **irrelevant**

4.0 widely revised `DeclaredQuery`/`QueryEnhancer` and removed the `spring.data.jpa.query.native.parser` option, exposing selection via `@EnableJpaRepositories(queryEnhancerSelector=…)`. We register no custom `QueryEnhancer`, set no parser property, and don't set the new attribute. **No impact.**

### 3. Removal of deprecated Commons API — **irrelevant**

Removed: `@PersistenceConstructor` (→ `@PersistenceCreator`), the SpEL→`ValueExpression` family (`DefaultSpELExpressionEvaluator`, `SpELExpressionEvaluator`, `QueryMethodEvaluationContextProvider`, `SpelQueryContext`, …), `QueryMethod.createParameters(Method, TypeInformation)`, `org.springframework.data.repository.util.ClassUtils`/`CastUtils`, `ClassTypeInformation` made package-private, the `org.springframework.data.type.classreading` package. **typed-ids references none of these** — our entities use plain JPA annotations and standard constructors; we author no query-method infrastructure. No impact.

### 4. `ListenableFuture` removal — **irrelevant**

Following Spring Framework 7, 4.0 dropped `ListenableFuture` for `@Async` query methods (use `CompletableFuture`). We have no async repository methods. No impact.

### 5. JPA 3.2 baseline + derived-query rewrite (Criteria → JPQL) — **realized through Hibernate, behavior to spot-check**

Spring Data JPA 4.0 requires JPA 3.2 (Hibernate 7.1+) and switches single-result query methods to `Query.getSingleResultOrNull()` (avoiding `NoResultException`), and **re-implements derived queries as String-based JPQL** instead of the Criteria API. This is internal to Spring Data's query execution. Our only derived method is `findByTitle(String)` over a `String` column; the typed-id is the `@Id`, reached via `findById`. The JPA-3.2/Hibernate-7.1 floor is the same EE-11 bump tracked in `docs/changelog-hibernate.md` (our 4x module already runs Hibernate 7.1.8). **No compile impact; the only thing worth confirming on CI is that the JPQL-derived `findByTitle` still round-trips a typed-id-keyed entity** — which the existing `-4x-data-indexed` tests already assert green.

### 6. Jackson 3, AOT repositories, Vector Search — **irrelevant**

Jackson 3 in 4.0 applies to **Spring Data REST only** (the `rest` module); we pull `spring-boot-starter-data-jpa`, not REST, so no `ProjectingJacksonHttpMessageConverter`/`SpringDataJackson3Configuration` is on our path (typed-ids' own JSON story is Jackson via `typed-ids-jackson`, see `docs/changelog-jackson.md`). AOT repositories are enabled by default but transparent to repository semantics (and we don't run native-image/AOT in these tests). Vector Search is opt-in via `@VectorSearch`/`SearchResults`, which we don't use. None intersect our surface.

### 7. Identifier / value-object `@Id` handling — **no Spring Data change touches us**

The brief flagged value-object identifiers as the surface that *could* affect how typed-id types behave as entity identifiers. Confirmed: for **JPA**, Spring Data does not convert or introspect the `@Id` value itself — it forwards the id object to the `EntityManager`/Hibernate, which owns the id↔column mapping (typed-ids' `UserType`/`JavaType`). The `CrudRepository`/`JpaRepository` `ID`-typed methods (`findById(ID)`, `findAllById(Iterable<ID>)`) are **generic and unchanged** across 3.5 → 4.0 → 4.1 (source-verified). The "composite ids / entity-as-id" additions in 4.0 are in **Spring Data JDBC/R2DBC** (Relational), a different module we don't use — not Spring Data JPA. So there is **no Spring-Data-side value-object-identifier behavior change** for our typed-id-as-`@Id`-via-`JpaRepository` pattern.

## Compatibility-strategy implications

What this means for module structure and the version matrix (surfacing, not deciding):

- **Spring Data is not an axis we model independently.** typed-ids extends no Spring Data SPI, so — exactly like Spring Framework — the Spring Data version is a pure function of the Boot version (Boot 3.5 → JPA/Commons 3.5.x; Boot 4.0 → 4.0.x; Boot 4.1 → 4.1.0). It is already captured by the Boot axis in `docs/changelog-spring-boot.md`. There is **no `typed-ids-spring-data-3` / `-4` split** to consider, because there is nothing Spring-Data-version-specific to compile.
- **The byte-identical 3x/4x test sources are the proof.** The same `JpaRepository`/`@EnableJpaRepositories`/derived-finder code is the Spring Data integration we ship into CI, and it compiles and passes on both 3.5.x and 4.0.0 without a single edit. Preserving the old upgrade path here is **free** — keep the Boot-3.5 data module for 3.5.x coverage, keep/advance the Boot-4 data module for 4.0.x/4.1.x coverage; both validate the same source through different Spring Data trains.
- **Track Spring Data only as a *reported* version, not a managed one.** Since we never declare it, the compatibility matrix should show Spring Data JPA/Commons as a **derived column of the Boot row** (3.5.x / 4.0.0 / 4.0.6 / 4.1.0) for documentation completeness, without a corresponding typed-ids module.
- **Stale-directory note (matches the Boot doc).** `testing/testing-typed-ids-spring-data-indexed` contains only leftover `build/` output (no `build.gradle.kts`, no `src/`); `settings.gradle.kts` auto-includes every directory under `testing/`, so this dead dir is technically enumerated but builds nothing. The live modules are `testing-typed-ids-spring-boot-{3x,4x}-data-indexed`. The stale dir is housekeeping, unrelated to Spring Data versions.

### Open questions / decisions to make later (not decided here)

1. **Add a Boot-4.1 data module?** The 4x module is pinned at `spring-boot-dependencies:4.0.0` (→ JPA 4.0.0). Bumping a copy to Boot 4.1 (→ JPA 4.1.0, Hibernate 7.4) would extend coverage — but the *driver* for that bump is Hibernate (the `-70` module's range, see `docs/changelog-hibernate.md`), **not** Spring Data, which is inert across 4.0 → 4.1. Decide on the Boot/Hibernate axis; Spring Data follows for free.
2. **Should typed-ids ever offer a real Spring Data integration?** Today it deliberately doesn't (no `Converter`, no `EntityInformation`). If a future need arose — e.g. custom id introspection or a Spring `Converter` so ids bind in non-JPA Spring Data stores — *that* would create a genuine Spring Data SPI surface (and a real 3.5-vs-4.0 question, given the `PropertyPath`/`TypeInformation`/`QueryEnhancer` churn). It would be a net-new feature, not a compatibility fix. Flagged only because the brief asked whether the surface exists: it does not, yet.
3. **CI confidence over the JPQL-derived-query rewrite.** The static diff proves the *API* is stable; the one behavioral 4.0 change that brushes our tests is the Criteria→JPQL derived-query switch. The existing `-4x-data-indexed` tests already exercise `findByTitle` against a typed-id-keyed entity, so green CI there is the confirmation — keep that test, don't drop it when advancing the Boot pin.
