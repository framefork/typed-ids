# Changelog research: Hibernate ORM

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/hibernate/hibernate-orm` git tags (release-date verification via `git log -1 --format=%ci <tag>`) — `7.2.0`/`7.2.21`, `7.3.0`/`7.3.10`, `7.4.0`/`7.4.3`, `8.0.0.Alpha1`, `8.0.0.Beta1`, `6.6.38`/`6.6.54`.
> - `github.com/hibernate/hibernate-orm/migration-guide.adoc` and `whats-new.adoc` at tags `7.3.10`, `7.4.3`, `8.0.0.Beta1`.
> - `github.com/hibernate/hibernate-orm` source — diffed the exact SPI interfaces/classes our modules implement across tags `7.2.0` → `7.4.3` → `8.0.0.Beta1` (and `6.6.38` → `6.6.54` for the 6.x range): `org.hibernate.usertype.UserType`/`EnhancedUserType`, `org.hibernate.type.Type`, `org.hibernate.type.descriptor.java.{JavaType,BasicJavaType}`, `org.hibernate.type.descriptor.jdbc.JdbcType`, `org.hibernate.query.sqm.tree.domain.SqmDomainType`, `org.hibernate.generator.{Generator,GeneratorCreationContext}`, `org.hibernate.mapping.GeneratorCreator`, `org.hibernate.id.enhanced.SequenceStyleGenerator`, `org.hibernate.boot.spi.AdditionalMappingContributor`.
> - typed-ids source: all five `modules/typed-ids-hibernate-{61,62,63,70,72}/`, the `testing/testing-typed-ids-hibernate-{64,65,66,71}-indexed/` range-coverage test modules, `gradle/libs.versions.toml`.

## TL;DR for typed-ids

- We ship **separate per-Hibernate-version modules** because Hibernate's type-system SPI drifts across versions. Current modules and their stated ranges (compiled against the floor, range-tested against higher patches via `testing-*-indexed` modules): **`-61`** (6.1.7), **`-62`** (6.2.46), **`-63`** (6.3.2, range-tested to 6.4.10 / 6.5.3 / 6.6.38), **`-70`** (7.0.10, range-tested to 7.1.11), **`-72`** (7.2.0). Top module caps at **7.2**.
- **7.3 and 7.4 are released and entirely uncovered**, but they are a **non-event for the SPI we implement.** Diffing every interface/class our `-72` module extends from tag `7.2.0` to `7.4.3` shows **zero breaking changes**: the only additions are new **`default` methods** (`GeneratorCreationContext.getMemberDetails()` since 7.3, `Generator.getGeneratedType()` since 7.4) which existing implementors inherit for free, plus Javadoc/cosmetic edits. The `7.3` and `7.4` migration guides have **empty type/SPI-change sections** for our surface. **Strong signal: 7.3/7.4 need no new module — extend the `-72` range with `testing-*-indexed` modules instead.** (Compile + run still to be confirmed by CI, but the API surface is clean.)
- **Hibernate 8.0 (Alpha1 2026-02-02, Beta1 2026-06-16) is the next breaking boundary** and it **does** touch us, though lightly so far: `org.hibernate.type.Type.beforeAssemble(...)` is **removed** in 8.0, and our `ImmutableType` carries an `@Override public void beforeAssemble(...)` — that `@Override` will **fail to compile** under 8.0. 8.0 also removes the long-deprecated `UserType` `SharedSessionContractImplementor`-based `nullSafeGet`/`nullSafeSet` overloads (we don't override those specific ones, so no break there) and assorted deprecated `JavaType`/`JdbcType` methods (unused by us). **Flag as upcoming, not actionable** — 8.0 is still Beta.
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
- **Impact on typed-ids: none.** Interface diff `7.2.0` → `7.4.3` confirms: `Generator` gained a `default @Nullable Class<?> getGeneratedType()` (since 7.4) and `SequenceStyleGenerator` provides it — `default`, so our `ObjectBigIntIdSequenceStyleGenerator` inherits it, no edit. `UserType` got new Javadoc + an optional `AnnotationBasedUserType` construction path (purely additive — existing default-constructor user types are unaffected). `JdbcType` changes `7.2→7.4` are body-only (`StringBuilder`→`var`) with no signature change. `BasicJavaType`, `JavaType`, `SqmDomainType`, `EnhancedUserType`, `GeneratorCreator`, `AdditionalMappingContributor` all **unchanged**.

> **Bottom line for 7.3/7.4:** the SPI we implement is **source-compatible from 7.2 through 7.4**. The realistic plan is to **extend the `-72` module's range** (add `testing-…-{73,74}-indexed` against `:typed-ids-hibernate-72`) rather than create new modules — pending a green CI run, which is the only thing the static diff can't prove.

### 8.0.0.Alpha1 — 2026-02-02 / 8.0.0.Beta1 — 2026-06-16 — next major (upcoming, not actionable)

- Headline: next major; `migration-guide.adoc`@8.0.0.Beta1 is large (740 lines). Removes a long tail of `@Deprecated(forRemoval=true)` members across the type system; internal `@Nullable` annotation switched from Checker Framework (`org.checkerframework.checker.nullness.qual.Nullable`) to `jakarta.annotation.Nullable`.
- **Impact on typed-ids: a real but small compile break (see deep dive).** The headline item is the removal of `Type.beforeAssemble(...)`, which our `ImmutableType` `@Override`s. Treat as **upcoming** — 8.0 is Beta; pin the analysis now, act when 8.0 nears GA.

### 6.6.x latest patch — 6.6.54 (2026-06-28), inside the `-63` module's 6.3–6.6 range

- **Impact on typed-ids: none.** `UserType`, `Type`, `JavaType`, `SequenceStyleGenerator` are **byte-for-byte unchanged** between `6.6.38` (current `-63` range-test ceiling) and `6.6.54`. The `-63` module's 6.6 coverage holds to the latest patch; bumping `hibernate-orm-v66` in the catalog (and the `-66-indexed` test) to 6.6.54 is a no-op for the integration code.

## Breaking-change deep dive: Hibernate 7.x → 8.0 (the only newer boundary that touches us)

Source-verified by diffing the interfaces/classes our modules implement at tag `7.4.3` vs `8.0.0.Beta1`.

### 1. `Type.beforeAssemble(...)` removed — our one concrete break

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

## Compatibility-strategy implications

What the above means for module structure and the version matrix (surfacing, not deciding):

- **7.3/7.4 are range-extensions of `-72`, not new modules** — the SPI is source-compatible 7.2→7.4 (only additive `default` methods). The cheapest faithful coverage is new `testing-typed-ids-hibernate-{73,74}-indexed` modules depending on `:typed-ids-hibernate-72`, mirroring the existing `-71-indexed`/`-66-indexed` pattern. The `-72` module's stated range would then read "7.2–7.4".
- **8.0 wants its own `-80` module** — but a *small* one. The only verified break is the `beforeAssemble` removal; everything else we touch is stable or additive. An `-80` module would be a near-copy of `-72` minus the `beforeAssemble` override. Worth waiting for 8.0 GA (currently Beta1) before investing.
- **All of this is additive and preserves the older paths.** Nothing in 7.3/7.4/8.0 reaches back into the `-61`…`-71` code, so the 6.1+ upgrade paths stay supported. The per-version-module design is exactly what makes "support new Hibernate without dropping old" cheap here.
- **hypersistence-utils coupling is test-only and lagging.** HU appears only in `*-testing` modules (`api(libs.hypersistence.utils.hibernateNN)`), and `-72-testing` already reuses `hypersistence-utils-hibernate-71` (3.13.3) because no HU `hibernate-72` build is pinned. A 7.3/7.4 test setup would reuse whatever HU build is current; the exact newest HU module is tracked in the hypersistence-utils doc, not here.

### Open questions / decisions to make later (not decided here)

1. **Confirm 7.3/7.4 with a CI run.** The static interface diff is clean, but the decision "extend `-72` range vs. new module" should be gated on `testing-…-{73,74}-indexed` actually compiling **and** passing the integration tests against 7.3.10 / 7.4.3 (id-generation and DDL paths exercise more than the interface surface — e.g. the reflective `InsertGeneratedIdentifierDelegate` proxy and the `SequenceStyleGenerator.initialize` flow).
2. **Where does the `-72` range stop?** If 7.3/7.4 fold in, do we also fold a future 7.5, or cap at 7.4 and start fresh? (Hibernate's SPI has been stable across 7.2–7.4; the risk is the next minor, not these.)
3. **`-80` module timing.** Build it now against `8.0.0.Beta1` to de-risk, or wait for GA? Beta SPI can still shift — the 740-line 8.0 migration guide suggests more removals may land before GA.
4. **6.6 ceiling bump.** Trivially bump `hibernate-orm-v66` (and the `-66-indexed` test) from 6.6.38 to 6.6.54 — no code change, just keeps the range-test honest. Same optionally for a `-72-indexed` against the latest 7.2 patch.
5. **hibernate-models version floor.** `-72` pins `hibernate-models` 1.0.0 (`compileOnly`); 7.3 ships 1.1. We don't reference `MemberDetails`, so it's a non-issue today — but a future `default` method that *we do* need to implement could force a hibernate-models floor bump per range.
