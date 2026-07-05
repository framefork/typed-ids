# Compatibility research — overview & synthesis

> Index and cross-cutting synthesis of the per-library research compiled 2026-06-28 for the typed-ids newer-versions compatibility effort.
> This phase is research only — it maps what changed upstream and what it means for our integration. The "how do we restructure to solve it" decisions come next; open questions are surfaced here, not decided.

## The per-library docs

| Doc | Library | Is it a real code dependency for us? | Headline verdict |
|---|---|---|---|
| [changelog-jackson.md](changelog-jackson.md) | Jackson | **Yes — production** (`compileOnly` in core module) | **The one hard breaking axis.** 2.x bumps are free; 3.0 renames our exact base types → needs a parallel Jackson-2/Jackson-3 flavor. |
| [changelog-hibernate.md](changelog-hibernate.md) | Hibernate ORM | **Yes — production** (per-version modules) | 7.3/7.4 are SPI non-events → extend the `-72` range with test modules, no new modules. 8.0 has exactly one break, and it's future. |
| [changelog-spring-boot.md](changelog-spring-boot.md) | Spring Boot | No — **test-only**, but the matrix *driver* | Its major dictates the transitive Jackson/Hibernate/Data/Framework versions. The BOM is the spine of our test matrix. |
| [changelog-spring-framework.md](changelog-spring-framework.md) | Spring Framework | No — **transitive-only** | One stable `@Bean` usage, no converter SPI. Not an independent axis. |
| [changelog-spring-data.md](changelog-spring-data.md) | Spring Data (JPA + Commons) | No — **test-only** | Byte-identical 3x/4x test sources; every 4.0 break lands on SPI we don't touch. A derived column of the Boot row. |
| [changelog-springdoc.md](changelog-springdoc.md) | SpringDoc OpenAPI | **Yes — production** (auto-config module) | Consumes zero `org.springdoc.*` API; 3.0 is a Boot-4 rebaseline only and does **not** pull Jackson 3. No parallel module forced. |
| [changelog-swagger-core.md](changelog-swagger-core.md) | Swagger Core (v3 Jakarta) | **Yes — production** (`ModelConverter`) | Stays on **Jackson 2**; our integration unchanged 2.2.35→2.2.52; no major bump exists. |
| [changelog-hypersistence-utils.md](changelog-hypersistence-utils.md) | Hypersistence Utils | No — **test-only** (+ tsid `compileOnly`) | Added a `hibernate-73` module (3.15.0) targeting Hibernate 7.4.0 → unblocks `-73/-74` test coverage. |

## The single most important realization

Of the eight libraries, **only two require actual source changes to our shipped code**, and they are on completely different timelines:

1. **Jackson 2 → 3** — the *real* work. Jackson 3.0 renames exactly the databind/core types our two modules are built on (`Module`→`JacksonModule`, `JsonSerializer`→`ValueSerializer`, `JsonDeserializer`→`ValueDeserializer`, `SerializerProvider`→`SerializationContext`, package `com.fasterxml.jackson.{core,databind}`→`tools.jackson.{core,databind}`, plus changed SPI callback signatures incl. `findSerializer`'s new 4th param). A single `.class` cannot extend both `JsonSerializer` and `ValueSerializer` — so supporting both means **parallel source sets / separate artifacts**, mirroring how the per-Hibernate modules already work.

2. **Hibernate 8.0** — *future, tiny*. One concrete break: `org.hibernate.type.Type.beforeAssemble(...)` is removed in 8.0.0.Beta1, and our `ImmutableType` `@Override`s it. Not actionable until 8.0 stabilizes. Hibernate 7.3 and 7.4 (already released) change nothing we implement — only additive `default` methods.

**Everything else is a free version bump, a test-matrix addition, or genuinely decoupled.** In particular, the entire OpenAPI/Swagger stack is **decoupled from the Jackson-3 decision** (swagger-core stays Jackson-2-based even under springdoc 3.0 / Boot 4), and the whole Spring stack (Framework/Data) is just the version-management machinery delivered transitively through the Spring Boot BOM.

## The matrix spine: Spring Boot version → transitive versions

This is the backbone of our compatibility/test matrix (all read from the `spring-boot-dependencies` BOM at git tags):

| Spring Boot | Spring Framework | Hibernate ORM | Spring Data JPA | Jackson | Jakarta EE | Java floor |
|---|---|---|---|---|---|---|
| **3.5.8** (our current) | 6.2.14 | 6.6.36 | 3.5.x | **2.19.4** | 10 | 17 |
| **4.0.0** | 7.0.1 | 7.1.8 | 4.0.0 | **3.0.2** (+ Jackson-2 mgmt 2.20.1) | 11 | 17 |
| **4.0.7** | 7.0.8 | 7.2.19 | 4.0.6 | 3.0.x→3.1.x | 11 | 17 |
| **4.1.0** | 7.0.x | 7.4.1 | 4.1.0 | **3.1.4** | 11 | 17 |

Two subtleties that bite if ignored (both from the Spring Boot doc):
- **Boot 4.0.x slides Hibernate across a *minor* inside its own patch line** — 4.0.0 = 7.1.8 but 4.0.1 = 7.2.0, 4.0.7 = 7.2.19. Our `4x-data-indexed` test module is pinned at *exactly* 4.0.0 precisely to keep it on Hibernate 7.1 to match `:typed-ids-hibernate-70`. That pin is load-bearing, not incidental.
- **Jackson also slides 3.0→3.1 inside Boot 4.0.x**, and Boot 4 keeps Jackson-2 dependency management alive for the deprecated `spring-boot-jackson2` stop-gap.

## Which axes are independent (what actually multiplies the matrix)

- **Hibernate axis** (our existing per-version modules: 6.1 / 6.2 / 6.3 / 7.0 / 7.2 → + 7.3/7.4 coverage → eventually 8.0). Real, additive, and the cheapest to extend.
- **Jackson axis** (Jackson 2 flavor vs Jackson 3 flavor) — **new** axis the work introduces; the only one needing genuinely duplicated source.
- **Spring Boot axis** (3.5 vs 4.0/4.1) — exists only in *test* modules and is really just a convenient way to pin a whole transitive stack at once; it largely *coincides* with the Jackson axis (Boot ≤3.5 ↔ Jackson 2, Boot ≥4.0 ↔ Jackson 3).
- **OpenAPI/Swagger** — **not** an independent axis: stays Jackson-2, unchanged SPI; springdoc 2.8.x↔Boot 3, 3.0.x↔Boot 4 but our code is the same bytecode either way.

So the matrix is essentially **Hibernate-version × Jackson-major**, with Spring Boot as the test harness that fixes the transitive stack, and OpenAPI riding along unaffected.

## Consolidated open decisions for the next ("how to solve it") phase

These are surfaced by the research, not decided here:

1. **Jackson flavor split — the central design question.** One module with two source sets, or two published artifacts (e.g. `-jackson2` / `-jackson3`)? Needs distinct packages / `@AutoService(JacksonModule.class)` so the two service registrations can coexist on a classpath without colliding. Candidate floors/ceilings: Jackson-2 flavor floor stays ≤2.18 (preserve old upgrade paths), tested to 2.21 (LTS)/2.22; Jackson-3 flavor floor 3.0, target 3.1 (LTS).
2. **Hibernate range extension.** Add `testing-…-73-indexed` and `-74-indexed` against the existing `-72` module (SPI unchanged), backed by `hypersistence-utils-hibernate-73:3.15.3` (targets Hibernate 7.4.0, covers both). Pending a green CI run. Separately, track Hibernate 8.0 (the `beforeAssemble` removal) as a later module.
3. **Add a Boot-4 / springdoc-3 test module.** Today only Boot 3.5 + springdoc 2.8.14 is exercised; there's no Boot-4/springdoc-3.0 counterpart yet.
4. **Version-catalog hygiene.** Production floors drifted below what the ecosystem now resolves: `swagger-core-jakarta` 2.2.35 (latest 2.2.52), `springdoc-…-common` 2.1.0 (vs 2.8.x), Hypersistence `hibernate63/70/71` at 3.13.3 (movable to 3.15.3). **Keep** the `3.9.4` pins for `hibernate61/62` — they are the last HU release shipping those now-dropped modules, required to preserve the old upgrade paths.
5. **JDK floor.** Jackson 3 and Spring Framework 7 both require Java 17 — we are already there, so no floor change is forced; the Jackson-3 flavor simply inherits the 17 baseline.

## Cross-cutting preserve-the-old-paths principle

Every doc was framed around Filip's explicit goal: **newer-version support must be additive, never a replacement.** The older Hibernate modules (6.1+), the Boot-3.5 / Jackson-2 / springdoc-2.8 stack, and the frozen HU `3.9.4` pins all stay. The work adds a Jackson-3 flavor and 7.3/7.4 (later 8.0) Hibernate coverage alongside what exists — letting users take small, safe upgrade steps rather than forcing a jump to the newest stack.
