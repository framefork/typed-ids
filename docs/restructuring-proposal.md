# Module restructuring proposal — typed-ids new module layout

> Design proposal building on the research in [changelog-overview.md](changelog-overview.md) and a full integration-surface audit of the repo. Status: **proposal, nothing implemented.**
> Ground rules agreed: BC breaks are acceptable (classes may move between gradle modules), but runtime compatibility breadth is preserved — Hibernate 6.1+, Spring Boot 3.5, Jackson 2 all stay supported. Newer-version support is additive.
> **Version strategy (decided):** the restructure ships as one more **0.x cycle** (semver permits BC breaks in 0.x) — e.g. `0.12.0` — to dogfood the final layout in downstream projects; **1.0.0** then freezes the proven layout with minimal further delta. Mentions of "v1.0" below describe the target layout, which lands in the 0.x release.

## Goals

1. Extract the JSON integrations out of the core `typed-ids` artifact into dedicated modules, so the Jackson-2/Jackson-3 flavor split has a natural home and the core artifact stops silently bundling five optional integrations.
2. Minimize duplication — the audit shows the big win is the test-support layer (100% duplicated), not the production Hibernate modules (small, concentrated divergence).
3. Keep the per-version compatibility model: users on old stacks upgrade in small steps; every existing runtime combination keeps working.
4. Make the v1.0 migration mechanical: coordinate swaps in the build file, zero source changes for the common cases.

## Current state (what the audit established)

- The `typed-ids` core artifact bundles **five** optional integrations, all `compileOnly` (which `framefork.java-public` publishes as `<optional>true</optional>` POM deps): **Jackson 2** (`@AutoService(Module.class)`), **Gson** (`@AutoService(TypeAdapterFactory.class)`), **kotlinx-serialization** (explicit opt-in, no AutoService), **hypersistence-tsid** and **java-uuid-generator** (id-generation backends).
- Auto-registration keys on `META-INF/services/...` files, not artifact or package identity — moving classes to another jar preserves `ObjectMapper.findAndRegisterModules()` behavior as long as the new jar is on the classpath. Keeping packages unchanged avoids source breakage for explicit references.
- Jackson 2 and Jackson 3 base types cannot be extended by one class (`Module` vs `JacksonModule`) → the flavor split forces separate artifacts/source trees regardless of packaging; the two service files can coexist on one classpath.
- The five `typed-ids-hibernate-NN-testing` modules are **byte-identical** across 61/62/63/70/72; testFixtures differ by exactly one file across the whole range. Neither is published.
- Production Hibernate modules diverge in a concentrated set of files (generators, `ImmutableType`, `*JavaType`/`*Contributor`); 62↔63 are 12/13 identical; the 6→7 SPI boundary is the real fork.
- The OpenAPI stack is `api`-linked, consumes only swagger's `ModelConverter` SPI, stays Jackson-2-based — decoupled from the Jackson-3 work.
- Two empty leftover dirs (`testing-typed-ids-spring-data-indexed`, `testing-typed-ids-springdoc-openapi`) are still registered as subprojects via the settings glob; README still links one of them.

## Proposed module layout

### Published artifacts

| Artifact (v1.0) | Contents | Third-party dep (scope) | Status |
|---|---|---|---|
| `typed-ids` | Core: `TypedId`, `ObjectUuid`, `ObjectBigIntId`, registry, common utils, id-generation factories (tsid, uuid-generator) | tsid + uuid-generator stay `compileOnly` | **slimmed** — JSON integrations removed |
| `typed-ids-jackson2` | Current Jackson classes, packages unchanged (`…json.jackson`), `@AutoService(Module.class)` | `compileOnly(jackson-databind 2.x)` | **new** (extracted) |
| `typed-ids-jackson3` | New `tools.jackson` flavor, package `…json.jackson3`, `@AutoService(JacksonModule.class)`, targets Jackson 3.1 LTS | `compileOnly(tools.jackson:jackson-databind 3.x)` | **new** |
| `typed-ids-gson` | Current Gson classes, packages unchanged. Note: Gson has **no ServiceLoader auto-discovery** (verified — zero `ServiceLoader` usage in Gson source), so the `@AutoService(TypeAdapterFactory.class)` file is inert and registration is always manual via `GsonBuilder.registerTypeAdapterFactory(...)`; the README's "registered automatically" claim must be corrected during extraction (see [changelog-gson.md](changelog-gson.md)) | `compileOnly(gson)`; floor stays 2.10.1, test target 2.14.0 | **new** (extracted) |
| `typed-ids-kotlinx-serialization` | Current kotlinx classes, packages unchanged | `compileOnly(kotlinx-serialization-json)` | **new** (extracted) |
| `typed-ids-spring-convert` | Revives PR #22: Spring `ConverterFactory` implementations for typed ids (`String→ObjectUuid`, `String→ObjectBigIntId` — new, required for MVC binding — and `Number→ObjectBigIntId` for props/programmatic conversion) plus a bundled Boot auto-config registering them as beans. One jar serves Spring 6.2/7.0 and Boot 3.5/4.0 | `api(:typed-ids)`; `compileOnly(spring-core)`; `compileOnly(spring-boot, spring-boot-autoconfigure)` for the auto-config | **new** (revives PR #22) |
| `typed-ids-hibernate-{61,62,63,70,72}` | unchanged coordinates & contents | unchanged | kept as-is |
| `typed-ids-index-java-classes-processor` | unchanged | unchanged | kept as-is |
| `typed-ids-openapi-swagger-jakarta` | unchanged contents; swagger-core floor bumped 2.2.35 → 2.2.47 (aligned with what current springdoc resolves) | `api(swagger-core-jakarta)` kept — standalone SPI consumers (no Spring) need it transitive | kept, floor bump |
| `typed-ids-openapi-spring-boot` | **renamed** from `typed-ids-openapi-springdoc`; same classes, same packages (incl. the legacy `…springdoc.config` package — a naming wart not worth the source break) | `compileOnly(spring-boot-autoconfigure)` + `compileOnly(spring-boot)` (the honest compile deps); springdoc demoted `api` → `compileOnly` optional | **renamed + re-scoped** |

Naming rationale: explicit `-jackson2` / `-jackson3` (rather than `-jackson` + `-jackson3`) because the two lines will coexist for years and the repo already uses version-suffixed naming for Hibernate. The Jackson-3 classes get a distinct package (`json.jackson3`) so both flavors can sit on one classpath without simple-name collisions confusing imports; Jackson-2/Gson/kotlinx classes keep their exact current packages so existing explicit references compile unchanged after the dependency swap.

Extraction keeps the `compileOnly` (→ POM-optional) pattern inside each new module. That preserves today's behavior where typed-ids never forces a Jackson/Gson/kotlinx version onto consumers — Boot BOM stays in charge.

### OpenAPI stack: rename + re-scope, no split (resolved)

Source-verified findings that settle the springdoc/swagger structure question:

- **One compiled jar serves Spring Boot 3.5 AND 4.0.** Boot 4's modularization moved build directories, not Java packages: every FQCN the auto-config compiles against (`@AutoConfiguration`, `@EnableConfigurationProperties`, `@ConfigurationProperties`) and the `META-INF/spring/….AutoConfiguration.imports` discovery mechanism are byte-identical between v3.5.8 and v4.0.0. No per-Boot-major split is justified.
- **The springdoc module consumes zero `org.springdoc.*` API** — it's a Spring Boot auto-config registering the swagger `ModelConverter` as a bean. The one springdoc behavior we rely on (`ModelConverterRegistrar` collecting context `ModelConverter` beans) is byte-identical between springdoc 2.8.17 and 3.0.3. Hence the rename to `typed-ids-openapi-spring-boot`: the name matches what the module actually is, and one artifact covers springdoc 2.8.x + 3.0.x.
- **Today's dependencies are dishonest:** `@AutoConfiguration` is free-ridden transitively through springdoc's POM, `jakarta.validation` through swagger-core's, and `api(springdoc 2.1.0)` forces a 2023-era floor on consumers. The re-scope declares the real compile deps explicitly and makes springdoc a runtime expectation (compileOnly/optional), which it factually is.
- **Dual registration is clean:** swagger's `ModelConverters` singleton loads our `@AutoService` SPI file (works with zero Spring), and springdoc's registrar remove-then-adds the Spring bean of the same class — exactly one converter instance survives, and it's the bean carrying the `@ConfigurationProperties` config.
- **The swagger module keeps `api(swagger-core)`** — a standalone (non-Spring) swagger-core consumer of that module needs it transitive; only the floor moves (2.2.35 → 2.2.47). Swagger-core is Jackson-2 through the latest 2.2.52 with no Jackson-3 signal, so the OpenAPI stack never joins the Jackson flavor split.
- Hardening while we're at it: add `@ConditionalOnClass(ModelConverter.class)` (today the auto-config activates unconditionally) and drop the decorative `@NotNull` on the properties class (not `@Validated`, so it enforces nothing — removing it severs the latent jakarta.validation coupling).
- The one thing static analysis can't prove — that the same bytecode is discovered, binds properties, and is picked up by springdoc 3 under Boot 4 end-to-end — is exactly what the new `testing-typed-ids-springdoc-3x-openapi` module (Boot 4.0.x + springdoc 3.0.3) exists to prove.

### Spring conversion: `typed-ids-spring-convert` (revives PR #22)

PR #22 (author `chapcz` / Jan Loufek) contributed Spring `ConverterFactory` implementations for typed ids; it went stale but the design fits the new layout. Source-verified findings and the revival plan:

- **SPI stability:** `Converter`/`ConverterFactory` between Spring `v6.2.14` and `v7.0.8` differ only in the JSR-305 → JSpecify nullness migration — erasure-identical signatures; `ConverterRegistry`/`FormatterRegistry` are byte-identical. **One compiled jar serves both Spring majors.** The interfaces live in `spring-core` → `compileOnly` per the repo's POM-optional pattern (floor can stay at the PR's 6.0; the SPI dates to Spring 3.0).
- **Registration story (verified at Boot v3.5.8 and v4.0.0):** plain `ConverterFactory` beans are auto-registered into the MVC conversion service — `WebMvcAutoConfiguration.mvcConversionService()` → `ApplicationConversionService.addBeans(...)` explicitly handles `ConverterFactory`. Adding the `@ConfigurationPropertiesBinding` qualifier to the same beans also covers `@ConfigurationProperties` binding (the unqualified MVC lookup still finds qualified beans). So the bundled auto-config is just: one `@Bean` per factory, each `@ConfigurationPropertiesBinding` + `@ConditionalOnMissingBean`.
- **The one landmine:** `WebMvcAutoConfiguration` changed *package* between Boot majors, but `ApplicationConversionService` and `ConfigurationPropertiesBinding` did not — the auto-config must not reference `WebMvcAutoConfiguration` (no ordering annotations against it; none are needed). Respecting that, the one-jar-both-Boot-majors claim holds, mirroring the OpenAPI finding.
- **Why the auto-config is bundled (not a separate module):** the OpenAPI stack is two modules because swagger-core is a standalone third-party SPI with non-Spring consumers; here there is no second SPI — one-integration-one-module says one module. Without Boot on the classpath the `AutoConfiguration.imports` file is never read and the jar degrades gracefully to "plain factories, register manually via `FormatterRegistry`/`ConverterRegistry`".
- **Fixes required on revival** (the PR is fundamentally sound — it correctly reuses `ReflectionHacks.getConstructor` + `*TypeUtils.wrap*ToIdentifier` rather than reinventing them): add **`String→ObjectBigIntId`** (MVC sources are always Strings and `GenericConversionService` does not chain converters — without it, bigint ids don't bind from URLs at all; keep `Number→` for props binding); **cache the resolved `MethodHandle`** in the inner converter's constructor instead of re-resolving per conversion (Spring caches converters per type pair, so this also fails fast on misconfigured types); separate the malformed-input failure (→ honest `ConversionFailedException`/HTTP 400) from the missing-constructor developer error; align empty/trim handling with Spring's own `StringToUUIDConverter`; conventions — JSpecify instead of jetbrains `@NotNull`, `.editorconfig` next-line braces, version-catalog entry instead of the hardcoded `spring-core:6.0.0` coordinate, drop the unused AutoService deps.
- **Crediting:** cherry-pick the PR commit preserving authorship (it touches only new files and applies cleanly against master), then land fixes as follow-up commits; if rewritten wholesale instead, `Co-authored-by: Jan Loufek` + PR credit in the message. Close PR #22 with a pointer to the landed work.

### Versioned `typed-ids-spring*` starters (design agreed, boundaries pending the compat matrix)

Decided direction: alongside the unversioned *code* modules (the converters/auto-configs are proven single-jar across Spring 6.2–7.0 / Boot 3.5–4.0), publish **versioned starter artifacts** keyed to the points where upgrading Spring Boot forces a consumer to switch their `typed-ids-hibernate-NN` module or Jackson flavor. Rationale: legacy consumers (Boot 3.0.x onwards) get a single supported entry point that assembles the correct variant set for their stack, instead of hand-picking 3–4 artifacts.

Agreed starter contract:

- **Each `-springN` starter declares the LOWEST supported Spring Boot version of its range, and pins the matching `typed-ids-hibernate-NN` module + Jackson flavor as `api` dependencies** — consumers get the correct stack transitively, floor pinned low so old patch lines resolve cleanly.
- **CI must test each starter at least against the NEWEST Hibernate within its declared range** (and sensibly the newest Boot patch of the range) — the same "compile against the range floor, range-test high" convention the Hibernate modules already use via their `testing-*-indexed` siblings.
- Starters additionally pull the unversioned code modules (`typed-ids-spring`, `typed-ids-openapi-spring-boot`) — the only per-range pick besides hibernate/jackson is which springdoc line the *user* runs, which doesn't affect our artifacts (single jar covers both).
- Open sub-question: whether converter/auto-config *code* stays in one unversioned `typed-ids-spring` module that starters depend on (working assumption — one compiled jar is valid everywhere), or gets absorbed per-starter (rejected unless the matrix reveals an actual SPI fork; it would duplicate identical bytecode).

The concrete boundary set, derived from [spring-boot-compat-matrix.md](spring-boot-compat-matrix.md) (all 89 Boot GA tags v3.0.0→v4.1.0, versions read from the BOM at each tag): the whole 3.0→4.1 history collapses into **five blocks**, and only one mid-patch-line module switch exists (Boot 4.0.0→4.0.1, Hibernate 7.1.8→7.2.0 = `-70`→`-72`). The Jackson 2→3 flip and the Hibernate `-63`→`-70` jump both land exactly at Boot 4.0.0, so the Boot-major boundary flips both axes at once.

| Starter | Declared Boot range | `api` hibernate module | `api` Jackson flavor | Range-test target (newest in range) |
|---|---|---|---|---|
| `typed-ids-spring-30` | 3.0.x | `-hibernate-61` | `-jackson2` | Hibernate 6.1.latest, Boot 3.0.13 |
| `typed-ids-spring-31` | 3.1.x | `-hibernate-62` | `-jackson2` | Hibernate 6.2.latest, Boot 3.1.12 |
| `typed-ids-spring-32` | 3.2.0 – 3.5.x | `-hibernate-63` | `-jackson2` | Hibernate 6.6.latest, Boot 3.5.latest (existing `testing-…-6{4,5,6}-indexed` modules already cover the Hibernate side) |
| `typed-ids-spring-40` | **4.0.1** – 4.1.x | `-hibernate-72` | `-jackson3` | Hibernate 7.4.latest, Boot 4.1.latest (leans on the planned `-72` range extension to 7.3/7.4) |

- **The Boot 4.0.0 edge case:** exactly one patch (4.0.0, Hibernate 7.1.8) sits between the `-32` and `-40` starters. Proposed handling: no starter for it — `typed-ids-spring-40` declares floor 4.0.1, and a user on exactly 4.0.0 either bumps Boot one patch (trivial, same minor) or hand-assembles `-hibernate-70` + `-jackson3` as documented today. A dedicated single-patch starter would be maintenance noise for a stack nobody should stay on.
- Starter naming (`-spring-30` vs `-spring30` vs keying on the covered range) is cosmetic — table above uses the hibernate-module convention (`typed-ids-hibernate-61` → `typed-ids-spring-30`); final call is Filip's.

### Internal (unpublished) consolidation

- **One `typed-ids-hibernate-testing` module replaces the five byte-identical `typed-ids-hibernate-NN-testing` copies.** It has no Hibernate dependency of its own beyond what the consuming test module supplies, so a single copy serves all versions. The single-file testFixtures delta (`BigIntDbIdentityGeneratedUniqueTitleEntity`, present ≤6.3 only) stays with the module pair that needs it.
- This consolidated module is also the natural seed for the future extraction into a separate framefork testing-support project — deliberately **out of scope now**, but the consolidation means that later move touches one module instead of five. Since none of this is published today, that future extraction is a greenfield publish with no deprecation path to design.

### Production Hibernate modules: recommend NOT sharing source (yet)

Two options were considered for the 13-files-per-module duplication:

- **(a) Shared source dir** compiled into each module (`srcDir` pointing at a common folder; per-version files live only in the module — partition, not shadowing). Saves ~8–11 files per module but couples every shared edit to all five Hibernate APIs at once and makes the modules harder to read in isolation.
- **(b) Status quo copies.** ~1.4k LOC per module, divergence is the *point* of the per-version model, and the copies are what let each module be reviewed/patched against its own Hibernate line without risk to the others.

**Recommendation: (b) for v1.0.** The duplication that actually hurts (test support, 100% identical) is eliminated by the consolidation above; the production copies are small, load-bearing, and the honest record of SPI drift. Revisit (a) only if the module count grows (e.g. a future `-80`).

## v1.0 user migration guide (sketch)

| If you use… | v0.x | v1.0 |
|---|---|---|
| Jackson serialization | `org.framefork:typed-ids` alone | add `org.framefork:typed-ids-jackson2` |
| Gson serialization | `org.framefork:typed-ids` alone | add `org.framefork:typed-ids-gson` |
| kotlinx-serialization | `org.framefork:typed-ids` alone | add `org.framefork:typed-ids-kotlinx-serialization` |
| Jackson 3 / Spring Boot 4 | *(not supported)* | add `org.framefork:typed-ids-jackson3` |
| OpenAPI via springdoc | `org.framefork:typed-ids-openapi-springdoc` | `org.framefork:typed-ids-openapi-spring-boot` (coordinate rename only; classes and packages unchanged) |
| Spring MVC / `@ConfigurationProperties` binding of typed ids | *(not supported)* | add `org.framefork:typed-ids-spring-convert` |
| Hibernate / swagger / processor artifacts | — | unchanged coordinates |

No package changes for existing integrations → the migration is a build-file edit only. The silent-loss risk (a user upgrades `typed-ids` and auto-registration quietly disappears) is mitigated by the major version signal + a prominent README/release-notes migration table; if we want a hard stop instead, an optional follow-up is a `typed-ids` 1.0 that logs a warning when it detects Jackson on the classpath without the new module — surfaced as an open question below.

## Accompanying work in the same major (from the changelog research)

- **New test coverage:** `testing-typed-ids-hibernate-{73,74}-indexed` against the existing `-72` module (SPI unchanged; backed by `hypersistence-utils-hibernate-73:3.15.3`); `testing-typed-ids-springdoc-3x-openapi` (Boot 4.0.x + springdoc 3.0.3, mirroring the 2x module's assertions incl. TS client generation — proves the one-jar-both-Boot-majors claim end-to-end); a Jackson-3 test module exercising `typed-ids-jackson3` under Boot 4. Bump the 2x module's springdoc pin 2.8.14 → 2.8.17.
- **Spring-convert test coverage:** Boot 3.5.x and Boot 4.0.x web test modules exercising the *same compiled* `typed-ids-spring-convert` jar — MockMvc `@PathVariable`/`@RequestParam` binding for uuid and bigint ids (incl. the HTTP-400 path for malformed input), `@ConfigurationProperties` binding, and a Spring Data repository-web/`DomainClassConverter` case (the one registration claim not yet source-verified). Whether these are dedicated modules or assertions folded into the planned Boot web test apps is an open question below.
- **Catalog hygiene:** bump swagger-core floor (2.2.35 → current), springdoc common floor, HU `hibernate63/70/71` pins 3.13.3 → 3.15.3; **keep** HU `3.9.4` for h61/h62 (last release shipping those modules) and the load-bearing Boot `4.0.0` pin in the 4x test module.
- **Cleanup:** delete the two empty leftover `testing/` dirs (they're registered subprojects via the settings glob) and fix the stale README link to `testing-typed-ids-springdoc-openapi`.
- **Build/CI split (decided):** stop wiring the `test-jdk21`/`test-jdk25` tasks into `check` in `framefork.java.gradle.kts` (keep them registered), so local `./gradlew build` runs the minimal supported JDK (17) only; change the GitHub workflow from one three-JDK job to parallel matrix jobs — JDK 17 runs the full `build` (compile + errorprone + javadoc + tests), JDK 21/25 jobs run only their `test-jdkNN` task (tests compile with the 17 toolchain regardless, so newer-JDK jobs skip the static-analysis cost). Motivated by build time — the restructure adds test modules, and tests run serially within a job (root `mustRunAfter` chain) with `outputs.upToDateWhen { false }` forcing full re-runs.
- **README rewrite** for the new artifact layout + migration table.

## Open questions

1. ~~**Version number**~~ **Decided:** one more 0.x cycle (`0.12.0`) carrying the full restructure, dogfooded downstream; `1.0.0` follows once the layout is proven.
2. **Classpath-detection warning** in core when Jackson is present but no typed-ids Jackson module is — worth the complexity, or is the major-version signal enough?
3. **Should tsid/uuid-generator also move out?** Proposal keeps them in core (they back core id-generation, not serialization), but a stricter reading of "core = plain base classes" would extract them too.
4. ~~**Gson upstream**~~ **Resolved:** researched in [changelog-gson.md](changelog-gson.md) — no BC risk 2.10.1 → 2.14.0, floor stays, test target 2.14.0; the real finding is the inert `@AutoService` file / README correction noted in the layout table.
5. **`typed-ids-jackson3` floor/target:** floor 3.0 vs floor 3.1 (LTS). Research suggests floor 3.0, tested against 3.1/3.2 — mirrors the "low floor, test high" pattern used elsewhere.

Spring-convert (PR #22 revival) specifics:

6. **Factory set:** `String→ObjectUuid` + `String→ObjectBigIntId` + `Number→ObjectBigIntId` is the proposed trio; add `UUID→ObjectUuid` for completeness?
7. **Auto-config opt-out:** jar-presence = consent (default-on, matching the OpenAPI module), or add a `framefork.typed-ids.spring-convert.enabled` property toggle?
8. **spring-core floor:** keep the PR's 6.0 ("low floor, test high" — the SPI dates to Spring 3.0), or align with the Boot-3.5 line at 6.2.x?
9. **Test-module placement:** dedicated `testing-typed-ids-spring-convert-{3x,4x}` modules, or fold the binding assertions into the planned Boot web test apps to contain module count (CI test execution is serial across modules)?
10. **Sequencing:** ship spring-convert in the same `0.12.0` cycle as the restructure, or as a fast-follow (it's purely additive, no BC surface)?

## Suggested next steps

1. Filip reviews/adjusts this proposal (especially the open questions).
2. Write the implementation plan (branch/commit structure, work order: cleanup → JSON extraction → jackson3 → hibernate-testing consolidation → new test coverage → catalog bumps → README/migration guide → 1.0.0 release).
3. Implement in that order — each step independently green in CI, so the branch is bisectable and reviewable.
