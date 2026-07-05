# Changelog research: Spring Boot

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/spring-projects/spring-boot` git tags (release-date + managed-version verification via `git log -1 --format=%ci <tag>` and `git show <tag>:…`) — every `v3.5.x` and `v4.0.x`/`v4.1.0` tag.
> - `github.com/spring-projects/spring-boot` wiki — `Spring-Boot-4.0-Migration-Guide`, `Spring-Boot-4.0-Release-Notes`, `Spring-Boot-4.1-Release-Notes`, `Spring-Boot-3.5-Release-Notes`, `Spring-Boot-4.0-Configuration-Changelog`.
> - The platform BOM `spring-boot-dependencies` and root `gradle.properties` at tags `v3.5.8`, `v3.5.16`, `v4.0.0`…`v4.0.7`, `v4.1.0` — to read the exact managed Jackson / Hibernate / Spring Data / Spring Framework versions Boot imposes. (BOM path moved from `spring-boot-project/spring-boot-dependencies/build.gradle` in 3.5 to `platform/spring-boot-dependencies/build.gradle` in 4.0 as part of the modularization.)
> - `github.com/spring-projects/spring-data-jpa` git tags — to map the Spring Data Bom release-train number to the concrete `spring-data-jpa` module version.
> - typed-ids source: `gradle/libs.versions.toml`, and the three Boot/springdoc test modules `testing/testing-typed-ids-spring-boot-3x-data-indexed`, `testing/testing-typed-ids-spring-boot-4x-data-indexed`, `testing/testing-typed-ids-springdoc-2x-openapi`.

## TL;DR for typed-ids

- **Spring Boot is not a production dependency of typed-ids** — it appears only in `testing/` modules. Its job in this matrix is to act as the **version-management spine**: a Boot version's BOM dictates the *transitive* Jackson / Hibernate / Spring Data / Spring Framework versions, which are the axes we actually integrate with. So "support Boot 4" really means "support the Jackson-3 + Hibernate-7.x + Spring-Data-4 + Spring-Framework-7 stack that Boot 4's BOM pins."
- We pin **`springBoot = 3.5.8`** in `gradle/libs.versions.toml`, and the Boot test modules hard-code BOM versions: **`spring-boot-dependencies:3.5.8`** (3x module) and **`spring-boot-dependencies:4.0.0`** (4x module). The 3x module tests `:typed-ids-hibernate-63`; the 4x module tests `:typed-ids-hibernate-70`. Both run identical Spring Data JPA + Hibernate persistence tests (UUID + BigInt, Postgres + MySQL via Testcontainers) — they are **Hibernate/Spring-Data integration tests**, not JSON tests.
- **Boot 3.5.x is a pure maintenance line on the "classic" stack**: Jackson **2.x**, Spring Framework **6.2.x**, Hibernate **6.6.x**, Spring Data JPA **3.5.x**, Java 17, Jakarta EE 10 / Servlet 6.0. Nothing in the 3.5.x tail breaks us — the only moving part relevant to us is that Boot **bumped its managed Jackson from 2.19.4 (3.5.8) to 2.21.4 (3.5.16)**, which our Jackson-2 integration already tolerates (see the Jackson doc).
- **Boot 4.0.0 (GA 2025-11-20) is THE boundary.** It simultaneously flips *every* transitive axis to its next major: **Jackson 2 → 3** (`tools.jackson`, preferred JSON lib), **Spring Framework 6.2 → 7.0**, **Hibernate 6.6 → 7.1+**, **Spring Data 3.5 → 4.0** (release train `2025.0` → `2025.1`), plus **Jakarta EE 10 → 11 / Servlet 6.1**. It also restructures the whole module/starter layout (`spring-boot-<technology>` split). Java floor stays **17**.
- **Watch out: the Boot 4.0.x patch line slides Hibernate across a *minor* boundary.** Boot 4.0.0 pins Hibernate **7.1.8**, but 4.0.1 already moves to **7.2.0** and 4.0.7 to **7.2.19**; Boot 4.1.0 jumps to **7.4.1**. So "compatible with Boot 4.0.x" actually means "compatible with Hibernate 7.1 *and* 7.2", and "Boot 4.1" means Hibernate 7.4 — which maps onto our existing `-70`/`-72` modules (and the 7.3/7.4 range question in the Hibernate doc), not a single fixed version.
- **Direction: additive.** Boot 4 coverage is a *new* test axis, not a replacement for the Boot 3.5 axis. Filip wants to keep the 3.5 (Jackson-2 / Hibernate-6.6 / Spring-Data-3.5) path supported, so the realistic shape is *both* a Boot-3.5 test module (current) and a Boot-4.x test module (current, pinned at 4.0.0) — and the open question is whether to add a Boot-4.1 module and bump the 4.x pin as Hibernate slides under it.

## How typed-ids uses Spring Boot

There is **no `org.springframework.boot` dependency in any shipped typed-ids module** — `grep` over the non-test modules finds Boot only as the `spring-boot-configuration-processor` annotation processor coordinate in the catalog (`springdoc-openapi-spring-configuration-processor`, used by `typed-ids-openapi-springdoc`, version-pinned to `springBoot`). Everything else is test-only. The version is declared once as `springBoot = "3.5.8"` in `gradle/libs.versions.toml`, but the Boot **BOM** versions are hard-coded inside each test module's `build.gradle.kts` (not taken from the catalog alias).

Three active test modules wire Boot in, each via **`api(platform("org.springframework.boot:spring-boot-dependencies:<v>"))`** (BOM import — *not* the Boot Gradle plugin, except springdoc which applies the plugin `apply false` only to get its dependency management):

| Test module | Boot BOM | typed-ids module under test | Starters pulled | What it proves |
|---|---|---|---|---|
| `testing-typed-ids-spring-boot-3x-data-indexed` | `spring-boot-dependencies:3.5.8` | `:typed-ids-hibernate-63` | `spring-boot-starter-data-jpa`, `-starter-test`, `-testcontainers` | typed-ids' Hibernate-63 integration works under Boot 3.5's managed stack (Hibernate 6.6.x, Spring Data JPA 3.5.x, Spring Framework 6.2.x) through Spring Data JPA repositories + Testcontainers (Postgres + MySQL), for both UUID and BigInt ids and all generation strategies. |
| `testing-typed-ids-spring-boot-4x-data-indexed` | `spring-boot-dependencies:4.0.0` | `:typed-ids-hibernate-70` | `spring-boot-starter-data-jpa`, `-starter-data-jpa-test`, `-starter-data-jdbc-test`, `-starter-test`, `-testcontainers` | the **same test suite** under Boot 4.0's managed stack (Hibernate 7.1.8, Spring Data JPA 4.0.0, Spring Framework 7.0.1, Jackson 3 on the classpath). Confirms typed-ids' Hibernate-70 integration survives the Boot-4 / Jakarta-EE-11 / Spring-7 transition. Note the new split test starters (`-data-jpa-test`, `-data-jdbc-test`) — a direct consequence of the 4.0 module restructuring (see deep dive). |
| `testing-typed-ids-springdoc-2x-openapi` | `spring-boot-dependencies:3.5.8` (plugin `apply false`) | `:typed-ids`, `:typed-ids-openapi-springdoc` | `spring-boot-starter-web`, `springdoc-openapi-starter-webmvc-ui:2.8.14` | the springdoc/OpenAPI schema integration under Boot 3.5 + springdoc-openapi **2.x** (a Jackson-2 / Spring-6.2 world). There is **no Boot-4 / springdoc-3.x counterpart yet**. |

The 3x and 4x data modules contain **byte-for-byte identical test sources** (`org/framefork/typedIds/{uuid,bigint}/springData/*`, `SpringDataTestConfiguration`, the abstract Postgres/MySQL bases). The *only* difference is the Boot BOM version and the Hibernate module wired in — which is exactly the point: the same integration code is re-validated against the two managed stacks Boot selects.

> Two leftover directories — `testing/testing-typed-ids-spring-data-indexed` and `testing/testing-typed-ids-springdoc-openapi` — contain only stale `build/` output and no `build.gradle.kts`/sources; they are **not** active modules (not in `settings.gradle.kts`'s active set) and can be ignored.

### What "Boot 4 test module" implies transitively (the whole point)

Because the test modules import the Boot BOM, the typed-ids module under test is exercised against **whatever Boot pins**, not against the version typed-ids compiled the module against:

- **3x module → `:typed-ids-hibernate-63`** (compiled vs Hibernate 6.3.2) **run against Boot 3.5.8's managed Hibernate 6.6.36** — i.e. this is *already* a Hibernate-6.3-module-vs-6.6-runtime range test, layered on top of Spring Data JPA 3.5.x and Spring Framework 6.2.14.
- **4x module → `:typed-ids-hibernate-70`** (compiled vs Hibernate 7.0.10) **run against Boot 4.0.0's managed Hibernate 7.1.8**, Spring Data JPA 4.0.0, Spring Framework 7.0.1, with **Jackson 3.0.2** (and Jackson-2 *management* 2.20.1) on the classpath. So the Boot-4 axis predominantly exercises the **Hibernate-7.x flavor** of typed-ids; the Jackson axis is incidental here (these tests don't assert JSON), but Jackson 3 *is* what's resolved.

## Spring Boot → managed transitive versions (the compatibility spine)

All values read directly from the `spring-boot-dependencies` BOM and root `gradle.properties` at each git tag. Dates are the tag commit dates (`git log -1 --format=%ci`).

| Spring Boot | Released | Java min | Jakarta / Servlet | Spring Framework | Hibernate ORM | Spring Data Bom → `spring-data-jpa` | Jackson (preferred) | Jackson 2 mgmt |
|---|---|---|---|---|---|---|---|---|
| **3.5.8** *(our pin)* | 2025-11-20 | 17 | EE 10 / Servlet 6.0 | **6.2.14** | **6.6.36.Final** | `2025.0.6` → **JPA 3.5.x** | **2.19.4** | — (single Jackson 2) |
| 3.5.16 *(latest 3.5.x)* | 2026-06-25 | 17 | EE 10 / Servlet 6.0 | 6.2.19 | 6.6.53.Final | `2025.0.13` → JPA 3.5.x | **2.21.4** | — |
| **4.0.0** *(GA, the boundary)* | 2025-11-20 | 17 | **EE 11 / Servlet 6.1** | **7.0.1** | **7.1.8.Final** | `2025.1.0` → **JPA 4.0.0** | **3.0.2** | **2.20.1** |
| 4.0.1 | 2025-12-18 | 17 | EE 11 / Servlet 6.1 | 7.0.x | **7.2.0.Final** | `2025.1.1` → JPA 4.0.x | 3.0.x | 2.20.x |
| 4.0.7 *(latest 4.0.x)* | 2026-06-10 | 17 | EE 11 / Servlet 6.1 | 7.0.8 | **7.2.19.Final** | `2025.1.6` → JPA 4.0.6 | **3.1.4** | **2.21.4** |
| **4.1.0** *(GA)* | 2026-06-10 | 17 (jOOQ feature → 21) | EE 11 / Servlet 6.1 | 7.0.8 | **7.4.1.Final** | `2026.0.0` → **JPA 4.1.0** | 3.1.4 | 2.21.4 |

Cross-references to the sibling docs:
- The **Jackson 2→3** column is the subject of `docs/changelog-jackson.md` (the `com.fasterxml` → `tools.jackson` rename that forces a second source set/artifact for our Jackson modules).
- The **Hibernate** column is the subject of `docs/changelog-hibernate.md` (6.6 → 7.1 → 7.2 → 7.4 SPI drift; note Boot 4 slides us straight onto 7.1+, and 4.1 onto 7.4, which is the 7.3/7.4 "extend `-72` range" question in that doc).

**Two facts worth pulling out of the table:**

1. **Boot 4.0.x is not a fixed Hibernate.** Within the *same* 4.0 patch line, Hibernate moves across a **minor** boundary: 7.1.8 (4.0.0) → 7.2.0 (4.0.1) → 7.2.19 (4.0.7). Our `4x` test module pins Boot **4.0.0** precisely so it lands on Hibernate **7.1** (matching the `-70` module). If we bumped that pin to 4.0.7 we'd be testing `-70` against **7.2** — which is really the `-72` module's territory. This is the same "module-vs-runtime range" mechanic the Hibernate doc describes, now driven by the Boot BOM.
2. **Jackson also slides inside 4.0.x**: 3.0.2 (4.0.0) → 3.1.4 (4.0.7), and the *Jackson-2 management* floor rises 2.20.1 → 2.21.4. So Boot 4 keeps Jackson-2 dependency management alive (for the `spring-boot-jackson2` stop-gap) the whole time — meaning a Jackson-2 typed-ids artifact still *resolves* under Boot 4 if the user opts into it, even though Jackson 3 is the default.

## Version timeline (newer-than-ours)

### 3.5.9 → 3.5.16 — the 3.5.x maintenance tail (latest 3.5.16, 2026-06-25)

- Pure maintenance on the classic stack. No Jakarta/Servlet/Spring-major movement. Spring Framework creeps 6.2.14 → 6.2.19; Hibernate 6.6.36 → 6.6.53; Spring Data Bom `2025.0.6` → `2025.0.13` (still JPA 3.5.x).
- The one change touching our axes: **managed Jackson rises 2.19.4 → 2.21.4** (2.21 is the Jackson 2.x LTS). Per the Jackson doc this is a zero-friction bump for our Jackson-2 modules.
- **Impact on typed-ids: none.** This is the stack we already support. Bumping the test pin / catalog `springBoot` to the latest 3.5.x is a no-op for integration code; it would just exercise our `-63` module against Hibernate 6.6.53 instead of 6.6.36 (both inside the `-63` module's proven 6.3–6.6 range) and Jackson 2.21 instead of 2.19.

### 4.0.0 — 2025-11-20 — **major boundary** (full deep dive below)

- Flips every transitive axis to its next major (Jackson 3, Spring Framework 7, Hibernate 7.1+, Spring Data 4, Jakarta EE 11 / Servlet 6.1) and restructures Boot's own modules/starters. Java floor stays 17; Kotlin floor 2.2; GraalVM 25.
- **Impact on typed-ids: this is the entire reason the `4x` test module exists.** It doesn't break our *code* directly (we don't compile against Boot), but it changes *what our modules are tested against*, and it forces the downstream decisions tracked in the Jackson and Hibernate docs (Jackson-3 source set; Hibernate-7.x modules). The Boot-specific items that touch how we *test* (split test starters, `find-and-add-modules`, `@EntityScan` move) are in the deep dive.

### 4.0.1 → 4.0.7 — the 4.0.x maintenance line (latest 4.0.7, 2026-06-10)

- Maintenance, but with the **Hibernate 7.1 → 7.2 slide** and **Jackson 3.0 → 3.1 slide** noted above. Spring Framework 7.0.1 → 7.0.8; Spring Data Bom `2025.1.0` → `2025.1.6` (JPA 4.0.0 → 4.0.6).
- **Impact on typed-ids:** the moving Hibernate floor means a `4x` module that wants to track *latest* 4.0.x would need its Hibernate module under test to be `-72`, not `-70`. As pinned (4.0.0) it stays on `-70`/7.1. Decision deferred — see open questions.

### 4.1.0 — 2026-06-10 — **GA, continues the 4.x direction**

- New: Spring gRPC support, Jackson factory/customizer config (`spring.jackson.factory.*`, common `spring.jackson.read.*`/`write.*` across CBOR/JSON/XML), refined `spring.data.jpa.repositories.bootstrap-mode` semantics (`lazy`/`deferred`), Spock support restored. Minimum-requirement bump: **jOOQ 3.20 now needs Java 21** (a *feature-specific* floor, not a Boot-wide one — Boot 4.1 itself still baselines Java 17).
- Transitive majors advance again: **Hibernate 7.4.1**, **Spring Data Bom `2026.0.0` → JPA 4.1.0**, Spring Framework 7.0.8, Jackson 3.1.4 (unchanged from 4.0.7).
- **Impact on typed-ids:** Boot 4.1 lands us on **Hibernate 7.4** — exactly the version the Hibernate doc flags as a source-compatible range-extension of the `-72` module. A future `spring-boot-4.1` test module would be the cleanest way to validate that "extend `-72` to 7.4" claim end-to-end (CI green is the only thing the static diff there couldn't prove). The Spring Data JPA bootstrap-mode change is config-level and doesn't touch our `UserType`/id-generator surface.

## Breaking-change deep dive: Spring Boot 3.5 → 4.0

These are the 4.0 changes that matter to a **library tested under Boot** (we don't ship Boot code, so "breaking" here means "changes our test wiring or the resolved transitive stack"). Drawn from the 4.0 Migration Guide.

### 1. The Jackson 2 → 3 default switch

- Boot 4 makes **Jackson 3 the preferred JSON library** (`tools.jackson`, new group IDs/packages; `jackson-annotations` is the lone module that keeps the `com.fasterxml.jackson.annotation` package). Boot renames its own helper classes (`JsonObjectSerializer` → `ObjectValueSerializer`, `Jackson2ObjectMapperBuilderCustomizer` → `JsonMapperBuilderCustomizer`, `@JsonComponent` → `@JacksonComponent`, `@JsonMixin` → `@JacksonMixin`) and moves `spring.jackson.read.*`/`write.*` under `spring.jackson.json.*`. **We use none of Boot's Jackson helpers**, so these renames don't hit us directly — but they confirm the platform-wide commitment to Jackson 3.
- **`find-and-add-modules` (default `true` in Boot 4):** Boot now auto-registers **all** Jackson modules found on the classpath (Boot 3 registered only "well-known" ones). This is **favorable** to our `@AutoService`-registered modules — under Boot 4 a typed-ids Jackson-3 module would be picked up without a manual `mapper.registerModule(...)`. (Disable via `spring.jackson.find-and-add-modules=false`.)
- **`spring-boot-jackson2` stop-gap:** a deprecated module + `spring.jackson.use-jackson2-defaults` flag let users keep Jackson 2 under Boot 4, with Jackson-2 dependency management retained (the `Jackson 2 Bom` / `jackson2Version` we saw in the BOM). So a **Jackson-2 typed-ids artifact still works under Boot 4** for users on the stop-gap — but that's a deprecated migration path, not the steady state. The steady state on Boot 4 needs the Jackson-3 flavor (see `docs/changelog-jackson.md`).

### 2. Module + starter restructuring (the modularization)

- Boot 4 splits the few large jars into many focused `spring-boot-<technology>` modules (root package `org.springframework.boot.<technology>`), each with its own `spring-boot-starter-<technology>` and, for test infra, `spring-boot-<technology>-test` / `spring-boot-starter-<technology>-test`. This is **already visible in our `4x` test module**, which had to add `spring-boot-starter-data-jpa-test` and `spring-boot-starter-data-jdbc-test` alongside `spring-boot-starter-test` — split starters that didn't exist as separate POMs in Boot 3.5.
- A migration aide exists: `spring-boot-starter-classic` (+ `-test-classic`) restores the old fat-classpath as an intermediate step.
- **`@EntityScan` moved**: `org.springframework.boot.autoconfigure.domain.EntityScan` → `org.springframework.boot.persistence.autoconfigure.EntityScan` (new `spring-boot-persistence` module). Our tests don't use `@EntityScan` (our entities live in the test source root), so no change — but worth knowing if a future test does.
- **The Migration Guide explicitly warns**: "supporting both Spring Boot 3 and Spring Boot 4 within the same artifact is strongly discouraged." This is about *Boot-extension* artifacts (auto-config jars), which we don't ship — but it rhymes with the Jackson conclusion that 2-and-3 can't share one artifact: the whole Spring 4 ecosystem assumes a major-version split rather than a straddling build.

### 3. Baseline bumps that gate the resolved stack

- **Jakarta EE 10 → 11, Servlet 6.0 → 6.1** (Boot 4 is Jakarta EE 11). This is why Hibernate jumps to 7.x (Jakarta Persistence 3.2) and Spring Framework to 7.0 under Boot 4. Our Hibernate modules already track Jakarta Persistence via Hibernate; the EE 11 bump is realized *through* the Hibernate-7 / Spring-7 versions in the table.
- **Java 17 minimum** (unchanged from 3.5 — Boot 4 did *not* raise the Java floor; Jackson 3 and Hibernate 7 also baseline 17, so the whole Boot-4 stack is Java-17-clean). Kotlin 2.2, GraalVM 25 for native.
- **Spring Framework 7.x is mandatory** under Boot 4 (the guide: "you must use Spring Framework 7.x"). Spring Data moves to the `2025.1` release train (JPA 4.0).

### 4. Hibernate dependency-management harmonization (Boot 4)

- Boot 4 relocates `hibernate-jpamodelgen` → `hibernate-processor` and drops `hibernate-proxool`/`hibernate-vibur` from management. **We use none of these** (our annotation processing is our own `typed-ids-index-java-classes-processor` + Google AutoService), so no impact — noted only because it's in the "Upgrading Data Features" section a Hibernate-integrating library would scan.

### 5. Other resolved-stack movers (FYI, not ours)

- **Testcontainers 2.0** is managed by Boot 4 (we pin `testcontainers = 1.20.3` in our catalog, but the `4x` test module takes Testcontainers from the Boot BOM via `org.testcontainers:junit-jupiter` with no version → resolves to 2.0). Worth knowing the Boot-4 test module runs Testcontainers 2.x while the standalone Hibernate test modules run 1.20.3.
- JUnit Jupiter 6.0 (Boot 4.0) / Mockito 5.20 — Boot test starters pull these; our catalog's `junit-jupiter = 5.10.3` is only used where Boot's BOM isn't imported.

## Compatibility-strategy implications

What the above means for the test matrix and module structure (surfacing, not deciding):

- **Boot is the matrix selector, so the Boot axis should be expressed as a small set of pinned BOM versions, each standing in for a whole transitive stack.** Today that's `3.5.8` (classic: Jackson 2 / Hib 6.6 / SD 3.5 / SF 6.2) and `4.0.0` (modern: Jackson 3 / Hib 7.1 / SD 4.0 / SF 7.0). A `4.1.0` pin would add the Hib-7.4 / SD-4.1 stack.
- **Keep the 3.5 axis — it is the only thing pinning the "old stack" upgrade paths Filip wants preserved.** Dropping the Boot-3.5 test module would silently drop test coverage of the Jackson-2 + Hibernate-6.6 + Spring-Data-3.5 + Spring-Framework-6.2 combination as an *integrated* whole. Boot-4 coverage is **additive**.
- **The Boot-4.0.x Hibernate slide (7.1 → 7.2) means the `4x` pin is load-bearing.** Pinning 4.0.0 keeps the test on Hibernate 7.1 (matching `-70`); choosing latest 4.0.x would shift it to 7.2 (matching `-72`). The matrix decision (which Boot patch ↔ which Hibernate module) should be explicit, not implicit in "use latest."
- **Boot-4 favors our auto-registration.** `find-and-add-modules=true` is a tailwind for a Jackson-3 typed-ids module under Boot 4 — but it's also a reason to make sure a *Jackson-2* typed-ids artifact doesn't accidentally land on a Boot-4 classpath and get half-registered against a Jackson-3 mapper. (Our Jackson dep is `compileOnly`, so the Jackson-2 classes simply won't load if only `tools.jackson` is present — but the `@AutoService` service file's referenced FQCN matters; see the Jackson doc.)

### Open questions / decisions to make later (not decided here)

1. **Do we add a `spring-boot-4.1` test module** (BOM `4.1.0` → Hibernate 7.4 / Spring Data JPA 4.1 / Spring Framework 7.0.8)? It would be the end-to-end validation of the Hibernate doc's "extend `-72` to 7.4" claim, and would exercise the `-72` module under a Boot-managed stack.
2. **Which Boot 4.0.x patch does the `4x` module pin** — stay at 4.0.0 (Hibernate 7.1, tests `-70`) or move to latest 4.0.x (Hibernate 7.2, which really wants `-72`)? Possibly *both* a `-70`-on-4.0.0 and a `-72`-on-4.0.latest module, mirroring the Hibernate `-indexed` range-test pattern.
3. **A Jackson-3 / Boot-4 JSON test module.** The current `4x` module is Hibernate-only. If/when a Jackson-3 typed-ids flavor exists, a Boot-4 module that actually serializes (relying on `find-and-add-modules`) would prove auto-registration end-to-end. The springdoc axis has no Boot-4 / springdoc-3.x counterpart yet either.
4. **Catalog hygiene.** `springBoot = 3.5.8` in the catalog and the hard-coded `3.5.8`/`4.0.0` BOM strings in the test modules are drifting independently; whether to source all Boot BOM versions from the catalog (and add a `springBoot4` alias) is a housekeeping call.
5. **Testcontainers split.** The `4x` module silently runs Testcontainers 2.0 (from the Boot 4 BOM) while standalone modules run 1.20.3 — decide whether that divergence is intentional matrix coverage or an accident to align.
