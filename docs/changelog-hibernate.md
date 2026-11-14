# Changelog research: Hibernate ORM

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/hibernate/hibernate-orm` git tags (release-date verification via `git log -1 --format=%ci <tag>`) — `7.2.0`/`7.2.21`, `7.3.0`/`7.3.10`, `7.4.0`/`7.4.3`, `8.0.0.Alpha1`, `8.0.0.Beta1`, `6.6.38`/`6.6.54`.
> - `github.com/hibernate/hibernate-orm/migration-guide.adoc` and `whats-new.adoc` at tags `7.3.10`, `7.4.3`, `8.0.0.Beta1`.
> - `github.com/hibernate/hibernate-orm` source — diffed the exact SPI interfaces/classes our modules implement across tags `7.2.0` → `7.4.3` → `8.0.0.Beta1` (and `6.6.38` → `6.6.54` for the 6.x range): `org.hibernate.usertype.UserType`/`EnhancedUserType`, `org.hibernate.type.Type`, `org.hibernate.type.descriptor.java.{JavaType,BasicJavaType}`, `org.hibernate.type.descriptor.jdbc.JdbcType`, `org.hibernate.query.sqm.tree.domain.SqmDomainType`, `org.hibernate.generator.{Generator,GeneratorCreationContext}`, `org.hibernate.mapping.GeneratorCreator`, `org.hibernate.id.enhanced.SequenceStyleGenerator`, `org.hibernate.boot.spi.AdditionalMappingContributor`.
> - typed-ids source: all five `modules/typed-ids-hibernate-{61,62,63,70,72}/`, the `testing/testing-typed-ids-hibernate-{64,65,66,71}-indexed/` range-coverage test modules, `gradle/libs.versions.toml`.

## Since the 2026-07 research (re-checked 2026-11-14)

Re-checked against `https://github.com/hibernate/hibernate-orm` tags fetched 2026-11-14, and against Maven Central metadata for `org.hibernate.orm:hibernate-core`.
The load-bearing claims below come from a compile probe: the `typed-ids` core sources plus the branch's `modules/typed-ids-hibernate-72/src/main/java` (and `-63` for 6.6), compiled with javac against each listed `hibernate-core` jar and the matching `hibernate-models`.

### New releases since 2026-06-28

| Line | Newest tag (date) | Status |
|---|---|---|
| 6.6 | `6.6.59` (2026-10-18) | still patched |
| 7.2 | `7.2.25` (2026-09-17) | patched until September |
| 7.3 | `7.3.13` (2026-08-02) | no patch since August; the line looks finished |
| 7.4 | `7.4.15` (2026-11-06) | the active 7.x line |
| 7.5 | none | no tag and no `7.5` branch upstream; the next minor after 7.4 is 8.0 |
| 8.0 | `8.0.0.CR1` (2026-11-12) | `Beta2` 09-23, `Beta3` 09-25, `Beta4` 10-28, `CR1` 11-12; on Central. `main` is already `8.1.0-SNAPSHOT` |

### Impact on typed-ids

- **`-72` stretches to the newest 7.x.** The `-72` sources compile unchanged against `7.2.25`, `7.3.13` and `7.4.15` (compile probe). No 7.5 exists, so no new 7.x module is needed. The interface diff `7.4.4` → `7.4.15` over every Hibernate type `-72` imports shows only additive changes (`Dialect`, `MySQLDialect`, `MariaDBDialect`, `SharedSessionContractImplementor`, `TypeConfiguration`, a one-line `JdbcType` change). `7.3.11` → `7.3.13` changes none of them.
- **The 7.4 generator-type validation is unchanged through 7.4.15 and in 8.0.** `GeneratorTypeHelper.checkGeneratorGeneratedType` (HHH-18210, commit `21d6758921`, first tag `7.4.0.CR1`) is byte-identical `7.4.4` → `7.4.15`. In `8.0.0.CR1` only its private `wrap()` helper became `PrimitiveHelper.boxedType()`, with the same result. The branch's un-annotated `getGeneratedType()` in `ObjectBigIntIdSequenceStyleGenerator` therefore still works on every 7.4 patch.
- **Correction: the 8.0 break is larger than `beforeAssemble`.** Since `8.0.0.Beta2`, commit `1f51d2eef0` ("move SQM-related SPIs into SPI packages", 2026-06-19) moves two interfaces that `ImmutableType` implements:
  - `org.hibernate.query.sqm.SqmExpressible` → `org.hibernate.query.sqm.spi.SqmExpressible`
  - `org.hibernate.query.sqm.tree.domain.SqmDomainType` → `org.hibernate.query.sqm.tree.spi.domain.SqmDomainType`

  The 8.0 migration guide does not mention this move. Against `8.0.0.CR1` the unmodified `-72` sources fail with 12 errors, all in `ImmutableType`. After three edits (the two import changes and deleting the `beforeAssemble` override) the whole module compiles against `8.0.0.CR1` with `hibernate-models` 1.3.4. No other SPI `-72` touches breaks at compile time: `UserType`, `EnhancedUserType`, `BasicJavaType`, `JdbcType`, `TypeContributor`, `AdditionalMappingContributor`, `Generator`, `GeneratorCreationContext`, `GeneratorCreator`, `SequenceStyleGenerator`, `CustomType` and `DdlTypeImpl` all still fit. Because one source file cannot import both package locations, `-72` cannot cover 8.0; a `-80` module is required. It is still a small copy of `-72`.
- **Every 8.0 build from Beta2 on has the package move**, including `8.0.0.Beta3`, which Spring Data JPA 4.2 uses in its `hibernate-8` build profile. Only Alpha1 and Beta1 still have the 7.x locations.
- **8.0 adds `@SPI` role annotations** (`org.hibernate.SPI`, added 2026-09-05). `UserType`, `Type`, `BasicJavaType` and `TypeContributor` are marked `IMPLEMENT`. `SqmDomainType`, `GeneratorCreationContext` and `AdditionalMappingContributor` stay `@Incubating`. `GeneratorCreator` stays `@Internal`, as it was in 7.x, and `GeneratorAnnotationHelper` lives in an `internal` package. These two are the least stable things `-72` and a future `-80` depend on.
- **`DynamicParameterizedType` survives 8.0** but is still `@Deprecated(since = "7.0", forRemoval = true)`. It is the next likely removal after 8.0.
- **8.0 runtime requirements.** Java 17 baseline (`org.gradle.jvm.version: 17` in the `8.0.0.CR1` Gradle module metadata). `hibernate-core` 8.0.0.CR1 depends on `jakarta.persistence-api` **4.0.0-M8**, a milestone, and on `hibernate-models` 1.3.4 (7.4.15 uses JPA 3.2.0 and models 1.1.2). Any 8.0 test module has to accept a JPA 4.0 milestone on the test classpath until 8.0 goes GA.
- **8.0 behavior changes worth a test run, not a code change.** `SequenceStyleGenerator.configure` now reads the `JdbcEnvironment` from `creationContext.getDatabase()` and normalizes the sequence name. `TypeOverrideGeneratorCreationContext` delegates `getDatabase()`, so this should work, but only a run of the `-80` tests can prove it. `Generator.requiresIdentityColumn()` is a new `default` method returning `false`.
- **Catalog ceilings can move without code changes:** `hibernate-orm-v66` 6.6.38 → 6.6.59 (`-63` compiles against 6.6.59), `-v73` 7.3.11 → 7.3.13, `-v74` 7.4.4 → 7.4.15.

### What this changes in the text below

- The 7.4.0 section said "Impact on typed-ids: none". That was wrong on behavior: the HHH-18210 validation rejected the bigint generator, and the branch fixes it in `-72` (commit `ffc0cc9`). The section is corrected.
- The TL;DR, the 8.0 deep dive and the compatibility implications named `beforeAssemble` as the only 8.0 break. They now also name the SQM SPI package move.
- The 8.0 status was "Beta1". It is now "CR1".

## TL;DR for typed-ids

- We ship **separate per-Hibernate-version modules** because Hibernate's type-system SPI drifts across versions. Current modules and their stated ranges (compiled against the floor, range-tested against higher patches via `testing-*-indexed` modules): **`-61`** (6.1.7), **`-62`** (6.2.46), **`-63`** (6.3.2, range-tested to 6.4.10 / 6.5.3 / 6.6.38), **`-70`** (7.0.10, range-tested to 7.1.11), **`-72`** (7.2.0). Top module caps at **7.2**.
- **7.3 and 7.4 are released and entirely uncovered**, but they are a **non-event for the SPI we implement.** Diffing every interface/class our `-72` module extends from tag `7.2.0` to `7.4.3` shows **zero breaking changes**: the only additions are new **`default` methods** (`GeneratorCreationContext.getMemberDetails()` since 7.3, `Generator.getGeneratedType()` since 7.4) which existing implementors inherit for free, plus Javadoc/cosmetic edits. The `7.3` and `7.4` migration guides have **empty type/SPI-change sections** for our surface. **Strong signal: 7.3/7.4 need no new module — extend the `-72` range with `testing-*-indexed` modules instead.** (Compile + run still to be confirmed by CI, but the API surface is clean.) *Corrected 2026-11-14:* the API surface is clean, but 7.4.0.CR1+ validates the generator's generated type (HHH-18210) and rejected our bigint sequence generator; the branch fixes this inside `-72` with an un-annotated `getGeneratedType()`.
- **Hibernate 8.0 (Alpha1 2026-02-02, Beta1 2026-06-16, CR1 2026-11-12) is the next breaking boundary** and it **does** touch us: since Beta2 `SqmExpressible` and `SqmDomainType` moved to `spi` packages (see the 2026-11-14 section), and `org.hibernate.type.Type.beforeAssemble(...)` is **removed** in 8.0, and our `ImmutableType` carries an `@Override public void beforeAssemble(...)` — that `@Override` will **fail to compile** under 8.0. 8.0 also removes the long-deprecated `UserType` `SharedSessionContractImplementor`-based `nullSafeGet`/`nullSafeSet` overloads (we don't override those specific ones, so no break there) and assorted deprecated `JavaType`/`JdbcType` methods (unused by us). **Upcoming:** 8.0 is at CR1 (2026-11-12), not GA yet.
- **6.6.x is safe to the latest patch.** Between `6.6.38` (what `-63` is currently range-tested against) and `6.6.54` (latest, 2026-06-28) there are **zero** changes to `UserType`/`Type`/`JavaType`/`SequenceStyleGenerator`. Nothing in 6.4/6.5/6.6 threatens the `-63` module's assumptions.
- **Direction:** newer-version support is **additive**. 7.3/7.4 fold into the existing `-72` module's range; 8.0 will eventually want its own `-80` module (small delta — mostly the `beforeAssemble` removal). None of this threatens the older `-61`…`-72` modules, so the 6.1+ upgrade paths Filip wants to preserve stay intact.
- **hypersistence-utils is a test-only dependency** (only in the `*-testing` modules) and **lags Hibernate**: there is no HU `hibernate-72`/`-73`/`-74` artifact in our catalog — `-72-testing` already reuses **`hypersistence-utils-hibernate-71` 3.13.3** to test against 7.2. A 7.3/7.4 range would likely keep reusing that HU build until HU ships newer modules (HU versions tracked in its own doc).

## How typed-ids uses Hibernate

There is **no shared Hibernate base module** — each `typed-ids-hibernate-NN` module carries a **full copy** of the integration source under `org/framefork/typedIds/{common,uuid,bigint}/hibernate/`, and the per-version differences in those copies *are* the record of Hibernate's SPI drift. Each module declares `api(project(":typed-ids"))` + `api(libs.hibernate.orm.vNN)`; `-70`/`-72` additionally declare `compileOnly(libs.hibernate.models.v70)` (hibernate-models 1.0.0, "really a runtime dependency, but in runtime the version from hibernate is provided"). Auto-registration of contributors is via `@AutoService` (Google AutoService → `META-INF/services`).

The Hibernate SPI surface we implement/extend (from the `-72` module, the current top):

| typed-ids class | Hibernate type(s) it extends/implements | Package |
|---|---|---|
| `common/hibernate/ImmutableType<T, JavaTypeType>` | `UserType<T>`, `EnhancedUserType<T>`, `Type`, `SqmExpressible<T>`, `SqmDomainType<T>`, `MutabilityPlanExposer<T>`, `DynamicParameterizedType`, `TypeConfigurationAware` | `org.hibernate.usertype` / `org.hibernate.type` / `org.hibernate.query.sqm.*` / `org.hibernate.type.descriptor.java` |
| `uuid/hibernate/ObjectUuidJavaType`, `bigint/hibernate/ObjectBigIntIdJavaType` | `BasicJavaType<T>`, `DynamicParameterizedType` | `org.hibernate.type.descriptor.java`, `org.hibernate.usertype` |
| `uuid/hibernate/ObjectUuidType`, `bigint/hibernate/ObjectBigIntIdType` | `ImmutableType<…>` (concrete, declare `getName()`/`getSqlType()`) | — |
| `uuid/hibernate/jdbc/NativeUuidJdbcType` | extends `UUIDJdbcType` | `org.hibernate.type.descriptor.jdbc` |
| `uuid/hibernate/jdbc/BinaryUuidJdbcType` | extends `VarbinaryJdbcType` | `org.hibernate.type.descriptor.jdbc` |
| `uuid/hibernate/ObjectUuidTypesContributor`, `bigint/hibernate/ObjectBigIntIdTypesContributor` | `TypeContributor` (`@AutoService`) | `org.hibernate.boot.model` |
| `bigint/hibernate/ObjectBigIntIdTypeGenerationMetadataContributor` | `AdditionalMappingContributor` (`@AutoService`) | `org.hibernate.boot.spi` |
| `bigint/hibernate/id/ObjectBigIntIdSequenceStyleGenerator` | extends `SequenceStyleGenerator` | `org.hibernate.id.enhanced` |
| `bigint/hibernate/id/ObjectBigIntIdGeneratorCreator` (`-70`/`-72`) | `GeneratorCreator` | `org.hibernate.mapping` |
| `common/hibernate/TypeOverrideGeneratorCreationContext` (`-70`/`-72`) | `GeneratorCreationContext` | `org.hibernate.generator` |

Key SPI dependencies inside method bodies: `JdbcTypeIndicators.getJdbcType(resolveJdbcTypeCode(SqlTypes.UUID/BIGINT))`, `TypeConfiguration.getJdbcTypeRegistry()`/`getDdlTypeRegistry()`, `DdlTypeImpl` (5-arg constructor since 6.2), `TypeContributions.contributeJdbcType/contributeType`, `WrapperOptions` (binder/extractor since 7.0), `MappingContext` (since 7.0).

### How the modules differ (the SPI-drift fingerprint)

The diffs between adjacent modules are exactly the Hibernate SPI changes a new module has to absorb. Summarized so the 7.3/7.4/8.0 analysis has a baseline:

- **6.1 → 6.2:** `IdentifierGenerator.getInsertGeneratedIdentifierDelegate(persister, dialect, boolean)` → `getGeneratedIdentifierDelegate(persister)`; `MetadataContributor` (jandex `IndexView`) → `AdditionalMappingContributor` (`AdditionalMappingContributions` + `MetadataBuildingContext`); `JdbcTypeRegistry.getDescriptor(code)` → `indicators.getJdbcType(indicators.resolveJdbcTypeCode(code))`; `DdlTypeImpl` 3-arg → 5-arg constructor.
- **6.2 → 6.3:** the hand-written `InsertGeneratedIdentifierDelegate` wrapper was replaced by a **reflective `java.lang.reflect.Proxy`** over the delegate (`ReflectionHacks.getAllInterfaces(...)`), specifically to stop chasing the churn in that delegate's method set across patch releases.
- **6.3 → 7.0 — the big one.** This is where the largest share of our per-version code lives:
  - **Id generation rewrite** (`IdentifierGenerator` → `Generator`/`GeneratorCreator`): `SequenceStyleGenerator.configure(Type, Properties, ServiceRegistry)` → `configure(GeneratorCreationContext, Properties)` + a new `initialize(SqlStringGenerationContext)`; identifier strategy strings (`identifier.setIdentifierGeneratorStrategy("…SequenceStyleGenerator")`) → `identifier.getCustomIdGeneratorCreator()`/`setCustomIdGeneratorCreator(new ObjectBigIntIdGeneratorCreator(...))`. New helper class `TypeOverrideGeneratorCreationContext implements GeneratorCreationContext`. The `-61`…`-63` `ObjectBigIntIdIdentityGenerator` is **dropped** in `-70`/`-72`.
  - **Type SPI:** `BindableType<T>` dropped from `ImmutableType`; `DomainType<T>` → `SqmDomainType<T>` (and new `getTypeName()`/`getSqmType()`/`getPersistenceType()`); `getBindableJavaType()` → `getJavaType()`; binder/extractor `get`/`set`/`nullSafeGet`/`nullSafeSet` switched their session param `SharedSessionContractImplementor` → **`WrapperOptions`**; `getColumnSpan(Mapping)`/`getSqlTypeCodes(Mapping)`/`toColumnNullness(Mapping)` → `MappingContext`; widespread `@Nullable` (jspecify) annotations.
  - hibernate-models 1.0.0 becomes a (provided) dependency.
- **7.0/7.1 → 7.2:** `BasicType.getJdbcType(TypeConfiguration)` → no-arg **`getJdbcType()`** (we dropped the `getJdbcType(null)` call sites); `GeneratorCreationContext` gained a new `getValue()` method that `TypeOverrideGeneratorCreationContext` now implements.

The `-70` module is range-tested against 7.1 (`testing-typed-ids-hibernate-71-indexed` → `:typed-ids-hibernate-70` + Hibernate 7.1.11); the `-63` module against 6.4/6.5/6.6 (`testing-typed-ids-hibernate-{64,65,66}-indexed` → `:typed-ids-hibernate-63`). **This is the template for 7.3/7.4 coverage** — a `testing-typed-ids-hibernate-{73,74}-indexed` pointing at `:typed-ids-hibernate-72`.

## Version timeline (newer-than-ours)

Dates verified against `hibernate-orm` git tags (`git log -1 --format=%ci`). Note: Maven artifacts append `.Final` (e.g. catalog pins `7.2.0.Final`); git tags drop it (`7.2.0`).

### 7.2.x — our current top module's line (7.2.0 GA 2025-12-12; latest patch 7.2.21, 2026-06-28)

- Baseline for the comparison below. The `-72` module is built against `7.2.0.Final`. 7.2 is an actively-patched line (21 patches by 2026-06-28).
- **Impact on typed-ids:** this is what we already support. The `-72` module is **not yet** range-tested against any 7.2 patch beyond `.0` — adding a `testing-…-72-indexed` against the latest 7.2 patch would mirror the existing pattern and is low-risk.

### 7.3.0 — 2026-03-16 (latest patch 7.3.10, 2026-06-28)

- Headline (from `migration-guide.adoc`@7.3.10 + `whats-new.adoc`): removed obsolete `@Columns` annotation (API); **Classmate dependency removed** (`ClassmateContext` gone — SPI, not ours); raw `Map` types eliminated from `org.hibernate.jpa.boot.spi.Bootstrap` signatures; behavior: read-only entities now propagate read-only to their collections, `getSingleResult()` deduplication semantics tightened, timeout→`PersistenceException` on rollback, schema actions run even with no `@Entity`, `@Version` columns now `not null` by default. Dependencies: **Byte Buddy 1.18**, **Hibernate Models 1.1**.
- **Impact on typed-ids: none to compilation.** Verified by interface diff `7.2.0` → `7.3`/`7.4`: the only change to anything we implement is **`GeneratorCreationContext` gaining a `default MemberDetails getMemberDetails()` (since 7.3)** — a `default` method returning `null`, so our `TypeOverrideGeneratorCreationContext` inherits it and needs no edit. We never reference `MemberDetails`, so the hibernate-models 1.0.0→1.1 bump is irrelevant to our `compileOnly` surface (Hibernate supplies its own at runtime). None of the 7.3 behavior changes touch UUID-string / BigInt-number round-tripping.

### 7.4.0 — 2026-05-26 (latest patch 7.4.3, 2026-06-28)

- Headline (from `migration-guide.adoc`@7.4.3): **`Changes to API` and `Changes to SPI` sections are empty.** Behavior: Oracle `current date`/`local date` now `trunc()`-ed; `MySQLDialect` drops its `max_fetch_depth=2` default; eager `@Any` now join-fetched; pagination/limit with collection fetch pushed into SQL (`org.hibernate.limitInMemory` hint to revert); `SpannerPostgreSQLDialect` moved from community to core. DDL: `@ElementCollection` set unique constraints, `@CreationTimestamp`/`@UpdateTimestamp` inferred `NOT NULL`, Oracle value-based LOBs. Big new feature: **core `@Audited`** (Envers functionality folded into core, opt-in).
- **Impact on typed-ids: no source change, but one behavior break (corrected 2026-11-14).** Since `7.4.0.CR1`, `GeneratorTypeHelper.checkGeneratorGeneratedType` (HHH-18210) checks up front that a generator's `getGeneratedType()` is assignable to the id attribute. `SequenceStyleGenerator` reports `Long`, so it rejected `ObjectBigIntIdSequenceStyleGenerator`. The branch fixes it in `-72` with a `getGeneratedType()` method without `@Override` that returns the concrete id class (commit `ffc0cc9`). Interface diff `7.2.0` → `7.4.3` confirms: `Generator` gained a `default @Nullable Class<?> getGeneratedType()` (since 7.4) and `SequenceStyleGenerator` provides it — `default`, so our `ObjectBigIntIdSequenceStyleGenerator` inherits it, no edit. `UserType` got new Javadoc + an optional `AnnotationBasedUserType` construction path (purely additive — existing default-constructor user types are unaffected). `JdbcType` changes `7.2→7.4` are body-only (`StringBuilder`→`var`) with no signature change. `BasicJavaType`, `JavaType`, `SqmDomainType`, `EnhancedUserType`, `GeneratorCreator`, `AdditionalMappingContributor` all **unchanged**.

> **Bottom line for 7.3/7.4:** the SPI we implement is **source-compatible from 7.2 through 7.4**. The realistic plan is to **extend the `-72` module's range** (add `testing-…-{73,74}-indexed` against `:typed-ids-hibernate-72`) rather than create new modules — pending a green CI run, which is the only thing the static diff can't prove.

### 8.0.0.Alpha1 — 2026-02-02 / 8.0.0.Beta1 — 2026-06-16 / 8.0.0.CR1 — 2026-11-12 — next major

- Headline: next major; `migration-guide.adoc`@8.0.0.Beta1 is large (740 lines). Removes a long tail of `@Deprecated(forRemoval=true)` members across the type system; internal `@Nullable` annotation switched from Checker Framework (`org.checkerframework.checker.nullness.qual.Nullable`) to `jakarta.annotation.Nullable`.
- **Impact on typed-ids: a small compile break (see deep dive).** `ImmutableType` overrides the removed `Type.beforeAssemble(...)` and implements `SqmExpressible`/`SqmDomainType`, which moved to `spi` packages in Beta2. 8.0 is at CR1, so the SPI is close to final.

### 6.6.x latest patch — 6.6.54 (2026-06-28), inside the `-63` module's 6.3–6.6 range

- **Impact on typed-ids: none.** `UserType`, `Type`, `JavaType`, `SequenceStyleGenerator` are **byte-for-byte unchanged** between `6.6.38` (current `-63` range-test ceiling) and `6.6.54`. The `-63` module's 6.6 coverage holds to the latest patch; bumping `hibernate-orm-v66` in the catalog (and the `-66-indexed` test) to 6.6.54 is a no-op for the integration code.

## Breaking-change deep dive: Hibernate 7.x → 8.0 (the only newer boundary that touches us)

Source-verified by diffing the interfaces/classes our modules implement at tag `7.4.3` vs `8.0.0.Beta1`.

### 1. `Type.beforeAssemble(...)` removed — one of our two concrete breaks (see also §6)

Hibernate removed the method (it was `@Deprecated(forRemoval = true, since = "6.6")` with Javadoc "Is not called anymore"):

```
7.4.3       void beforeAssemble(Serializable cached, SharedSessionContractImplementor session);   // present on org.hibernate.type.Type
8.0.0.Beta1 (removed)
```

Our `ImmutableType` implements it explicitly:

```java
@SuppressWarnings("removal")
@Override
public void beforeAssemble(final Serializable cached, final SharedSessionContractImplementor session) { }
```

With the method gone from `Type` (and absent from `UserType`), the `@Override` becomes a compile error under 8.0. The fix is trivial (delete the method), but it means a `-72` artifact **cannot** be reused as-is for 8.0 — an `-80` module (or a source tweak) is required. This is the kind of small, mechanical delta that justifies a dedicated module rather than range-extension.

### 2. `UserType` deprecated session-based overloads removed — *not* a break for us

8.0 removes the two `@Deprecated(since = "7", forRemoval = true)` defaults:

```
7.4.3   default J nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
7.4.3   default void nullSafeSet(PreparedStatement st, J value, int position, SharedSessionContractImplementor session)
8.0.0.Beta1 (both removed)
```

Our `ImmutableType` overrides the **`WrapperOptions`** variants (`nullSafeGet(rs, position, WrapperOptions)`, `nullSafeSet(st, value, index, WrapperOptions)`) — the since-7.0 forms, which **survive** in 8.0. The `nullSafeSet(st, value, index, SharedSessionContractImplementor)` we also override is the **`Type`** interface method (still present in 8.0, confirmed), not the removed `UserType` one. **No break here**, but it's the kind of overlap to re-verify when building `-80`.

### 3. Deprecated `JavaType`/`JdbcType` members removed — unused by us

8.0 removes `JavaType.CoercionContext` + `JdbcType.getJdbcRecommendedJavaTypeMapping(...)`, `JdbcType.wrapWriteExpression(String, Dialect)` / `appendWriteExpression(String, SqlAppender, Dialect)` (all `@Deprecated(forRemoval = true, since = "7.2")`). **We call/override none of these** — our `BasicJavaType`/`JdbcType` usage is `getRecommendedJdbcType`, `getDefaultSqlLength`, `unwrap`/`wrap`, and extending `UUIDJdbcType`/`VarbinaryJdbcType`. No impact.

### 4. New `default` methods in 8.0 — free for us

`Generator.requiresIdentityColumn()` (8.0) joins `getGeneratedType()` (7.4) and `GeneratorCreationContext.getMemberDetails()` (7.3) as `default` methods we inherit without edits.

### 5. Internal `@Nullable` provider switch — not our concern

`Type`/`JavaType`/`JdbcType` switched their `@Nullable` import from Checker Framework to `jakarta.annotation.Nullable` in 8.0. This is internal to Hibernate's own source; our modules use `org.jspecify.annotations.Nullable` independently. No impact.


### 6. `SqmExpressible` and `SqmDomainType` moved to `spi` packages (since 8.0.0.Beta2, found 2026-11-14)

```
7.4.15      org.hibernate.query.sqm.SqmExpressible            org.hibernate.query.sqm.tree.domain.SqmDomainType
8.0.0.CR1   org.hibernate.query.sqm.spi.SqmExpressible        org.hibernate.query.sqm.tree.spi.domain.SqmDomainType
```

`ImmutableType` implements both, so its imports fail to compile under 8.0. The interfaces themselves only lost their license header and switched `@Nullable` to `jakarta.annotation.Nullable`. Commit `1f51d2eef0`, not listed in the 8.0 migration guide.

## Compatibility-strategy implications

What the above means for module structure and the version matrix (surfacing, not deciding):

- **7.3/7.4 are range-extensions of `-72`, not new modules** — the SPI is source-compatible 7.2→7.4 (only additive `default` methods). The cheapest faithful coverage is new `testing-typed-ids-hibernate-{73,74}-indexed` modules depending on `:typed-ids-hibernate-72`, mirroring the existing `-71-indexed`/`-66-indexed` pattern. The `-72` module's stated range would then read "7.2–7.4".
- **8.0 wants its own `-80` module** — but a *small* one. The verified breaks (compile probe against `8.0.0.CR1`) are the `beforeAssemble` removal and the move of `SqmExpressible`/`SqmDomainType` into `spi` packages; everything else we touch is stable or additive. An `-80` module is a copy of `-72` with two changed imports and without the `beforeAssemble` override. 8.0 is at CR1, so the SPI is close to final.
- **All of this is additive and preserves the older paths.** Nothing in 7.3/7.4/8.0 reaches back into the `-61`…`-71` code, so the 6.1+ upgrade paths stay supported. The per-version-module design is exactly what makes "support new Hibernate without dropping old" cheap here.
- *(Decided 2026-11-14: drop Hypersistence Utils from the tests and replace it with framefork's own Hibernate testing library in a separate repository, covering Hibernate 6.1 through 8.0. See `docs/changelog-hypersistence-utils.md`.)* **hypersistence-utils coupling is test-only and lagging.** HU appears only in `*-testing` modules (`api(libs.hypersistence.utils.hibernateNN)`), and `-72-testing` already reuses `hypersistence-utils-hibernate-71` (3.13.3) because no HU `hibernate-72` build is pinned. A 7.3/7.4 test setup would reuse whatever HU build is current; the exact newest HU module is tracked in the hypersistence-utils doc, not here.

### Open questions / decisions to make later (not decided here)

1. **Confirm 7.3/7.4 with a CI run.** The static interface diff is clean, but the decision "extend `-72` range vs. new module" should be gated on `testing-…-{73,74}-indexed` actually compiling **and** passing the integration tests against 7.3.10 / 7.4.3 (id-generation and DDL paths exercise more than the interface surface — e.g. the reflective `InsertGeneratedIdentifierDelegate` proxy and the `SequenceStyleGenerator.initialize` flow).
2. **Where does the `-72` range stop?** If 7.3/7.4 fold in, do we also fold a future 7.5, or cap at 7.4 and start fresh? (Hibernate's SPI has been stable across 7.2–7.4; the risk is the next minor, not these.)
3. *(Decided 2026-11-14: add `typed-ids-hibernate-80` to the repo now, built and tested against the newest 8.0 pre-release (8.0.0.CR1) and unpublished (`library-internal`); it switches to the GA version and `library-published` once Hibernate 8.0 is GA.)* **`-80` module timing.** Build it now against `8.0.0.Beta1` to de-risk, or wait for GA? Beta SPI can still shift — the 740-line 8.0 migration guide suggests more removals may land before GA.
4. **6.6 ceiling bump.** Trivially bump `hibernate-orm-v66` (and the `-66-indexed` test) from 6.6.38 to 6.6.54 — no code change, just keeps the range-test honest. Same optionally for a `-72-indexed` against the latest 7.2 patch.
5. **hibernate-models version floor.** `-72` pins `hibernate-models` 1.0.0 (`compileOnly`); 7.3 ships 1.1. We don't reference `MemberDetails`, so it's a non-issue today — but a future `default` method that *we do* need to implement could force a hibernate-models floor bump per range.
