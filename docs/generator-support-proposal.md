# Generator support for custom-typed bigint primary keys — diagnosis and proposal

> Research + proposal doc for restoring (or consciously not restoring) full `@GeneratedValue` generator-configuration support for `ObjectBigIntId`-typed primary keys. Compiled 2026-07-05. Status: **proposal, nothing implemented — decisions are Filip's.**
> Sources read:
> - `github.com/hibernate/hibernate-orm` source at tags `7.2.21`, `7.4.0`–`7.4.3`, `8.0.0.Beta1`, and `main` (`9d60309cca`) — the full id-generation pipeline: `org.hibernate.boot.model.internal.{GeneratorAnnotationHelper,GeneratorBinder}`, `org.hibernate.mapping.{SimpleValue,Property,GeneratorCreator}`, `org.hibernate.generator.{Generator,GeneratorCreationContext}`, `org.hibernate.generator.internal.GeneratorTypeHelper`, `org.hibernate.id.{IdentityGenerator,IdentifierGeneratorHelper}`, `org.hibernate.id.enhanced.{SequenceStyleGenerator,TableGenerator}`, `org.hibernate.id.insert.GetGeneratedKeysDelegate`, `org.hibernate.generator.values.internal.GeneratedValuesHelper`, `org.hibernate.sql.results.graph.basic.{BasicResult,BasicResultAssembler}`, `org.hibernate.usertype.UserType`, `org.hibernate.type.CustomType`, `org.hibernate.boot.model.process.internal.UserTypeResolution`; plus `migration-guide.adoc` at `7.4.3` and `8.0.0.Beta1`.
> - typed-ids source: `modules/typed-ids-hibernate-{61,62,63,70,72}`, the `fp/restructure-modules` worktree (`typed-ids-hibernate-72` with the 7.4 fix, `testing-typed-ids-hibernate-{73,74}-indexed`), and repo history (`44752fa`, `d40fc31`, `3a38f74`, PR #27).
> - Upstream trackers: `hibernate.atlassian.net` (HHH-18210, HHH-18124, HHH-16692), `discourse.hibernate.org`, Stack Overflow (links inline).

## TL;DR

- **The diagnosis is confirmed, with sharper edges than remembered.** Hibernate's id-generation pipeline consistently hands generators the *domain* (attribute) type — for us a `CustomType` whose `getReturnedClass()` is the `ObjectBigIntId` subclass — and provides **no seam anywhere** to say "generate relationally as `Long`, convert at the boundary". Sequence/table-style generators are hardwired to an integral domain class (`IdentifierGeneratorHelper.getIntegralDataTypeHolder` accepts only `Long/Integer/Short/BigInteger/BigDecimal`), and a `BeforeExecutionGenerator`'s return value is assigned to the entity id **verbatim, with zero coercion** (`AbstractSaveEventListener.generateId`). So the two-part hack (feed `super.configure(...)` a surrogate `Long` type through a wrapped context, wrap the raw value in `generate()`) is not an inelegance we chose — it is the only way in, on every 7.x line and unchanged in 8.0.0.Beta1.
- **The IDENTITY path working natively on 7.x is deliberate, not luck** — with one asterisk. The generated key is read back through the entity **identifier mapping's** `JdbcMapping`/`JavaType` (`GeneratedValueBasicResultBuilder` → `BasicResult` → `BasicResultAssembler`), so the raw JDBC `Long` is wrapped into the VO by the same machinery that reads the column in a query. That path is identical at 7.2.21 and 7.4.3. The asterisk: the 7.4 up-front type validation (HHH-18210) passes for IDENTITY only because it is **skipped** — the identity generator is created by a bare `creationContext -> new IdentityGenerator()` lambda that never runs `configure()`, so `getGeneratedType()` returns `null` and the check short-circuits.
- **The "overengineering" complaint is substantiated and quotable**: a 6-argument static helper (`GeneratorAnnotationHelper.prepareForUse`) in an `.internal.` package that we must call with four `null`s; an 11-method `@Incubating` interface (`GeneratorCreationContext`) with no delegating adapter, forcing a 10-method boilerplate class to override one getter; the canonical context implementation being a `private` inner class (`SimpleValue.IdGeneratorCreationContext`); and one new context/generator method landing in *each* of 7.2 (`getValue()`), 7.3 (`getMemberDetails()`), 7.4 (`getGeneratedType()`), 8.0 (`requiresIdentityColumn()`).
- **`UserType.getValueConverter()` is not the silver bullet.** It does turn the mapping into a converted basic value (column read/written as `Long`, VO at the domain boundary) and it is honored on the identity read path — but the sequence/table generator path never consults it: `GeneratorCreationContext.getType()` still reports the VO class and the generator's output is still used raw. It could *replace* the identity-era machinery we already deleted; it cannot delete the sequence subclass.
- **The gap is unreported upstream, and the 8.0 Beta window is favorable.** HHH-18210 (the 7.4 validation) only made the mismatch fail earlier; no ticket asks for standard IDENTITY/SEQUENCE generators to work with a UserType-backed `@Id` via boundary conversion. 8.0 just accepted a small `@Incubating default` method on `Generator` (`requiresIdentityColumn()`, added precisely so custom/wrapping generators can participate in identity DDL) — the exact shape a fix for our gap would take.
- **README.md:47 over-claims today**: "explicitly supports `AUTO`, `IDENTITY` and `SEQUENCE`" — true as user-visible behavior on the tested happy paths, but IDENTITY on 7.x is Hibernate's native path outside our control, TABLE has never worked, and the 6.x-only exception-propagation guarantee (PR #27) has no 7.x equivalent test.
- **Recommended direction** (details at the end): do E now (README honesty + port the identity exception-propagation test to 7.x to pin the native path's semantics), file D upstream while 8.0 is in Beta (converter-aware generation contract; small, precedented), keep C (`@IdGeneratorType` custom annotations) as a designed escape hatch only if users ask, and drop A (re-proxying identity on 7.x buys nothing the native path doesn't already do, and reintroduces the exact fragility class PR #27 had to fix).

## The reconstructed history (condensed)

| When | Commit / ref | What happened |
|---|---|---|
| 2025-04-18 | `44752fa` "add ObjectBigIntId identity ID generator" | 6.x-era custom `ObjectBigIntIdIdentityGenerator` added to `-61/-62/-63`: extends `org.hibernate.id.IdentityGenerator`, **sequence-emulates** identity (PG DDL gets `nextval(...)`, `is_identity = NO`) and wraps two JDK proxies — one over `PostInsertIdentityPersister` faking `getIdentifierType()` to a fabricated `Long` basic type, one over `InsertGeneratedIdentifierDelegate` wrapping `performInsert`'s raw value into the VO. |
| 2025-07-27 (merged 2025-10-04) | `d40fc31` "add support for Hibernate ORM 7.0 and 7.1" | **The give-up.** Hibernate 7 replaced the string generator-strategy (`SimpleValue.get/setIdentifierGeneratorStrategy`) with the opaque `GeneratorCreator` functional interface, killing the 6.x string-switch remap. The new `-70` module ships `ObjectBigIntIdGeneratorCreator` wrapping the original creator: `instanceof SequenceStyleGenerator` → substitute our subclass; **anything else → pass through untouched**, with the comment "the IdentityGenerator seems to be capable of handling custom types now". `ObjectBigIntIdIdentityGenerator` is deleted from 7.x; the PG assertion flips to `is_identity = YES`. Enumerated support shrank from {SequenceStyleGenerator, IdentityGenerator} to {SequenceStyleGenerator}. |
| 2026-02-06 | `3a38f74` (PR #27) | Fixes `UndeclaredThrowableException` masking real causes (e.g. `ConstraintViolationException`) in the 6.x proxies — `Method.invoke` wraps checked exceptions in `InvocationTargetException`, which an interface-less proxy re-wraps. Fix + regression test (`IdentityGeneratorExceptionPropagationTest`) exist **only in `-61/-62/-63`**; on 7.x the class is gone so there is nothing to fix — and no test proving the native path propagates correctly either. |
| 2026-07 | `fp/restructure-modules` worktree | First test run against Hibernate **7.4.4.Final** (catalog pin; the first 7.4.x we ever tested) hits the HHH-18210 up-front validation — present since **7.4.0.CR1**, commit `21d6758921` in hibernate-orm — which rejects our sequence subclass for declaring the surrogate `Long`. Fixed by a no-`@Override` `getGeneratedType()` on `ObjectBigIntIdSequenceStyleGenerator` returning the true id class (no `@Override` because the method doesn't exist on the 7.2 compile target; on 7.4+ it becomes the effective override via virtual dispatch). |

The cautionary tale in one line: an `@Incubating default` method added upstream in a *patch-invisible* way (empty migration-guide section, javadoc says "intended only for validation and error reporting") converted a working-but-lying generator into a boot failure, and only the new `testing-typed-ids-hibernate-74-indexed` range test caught it.

## Support matrix as it actually is

For `ObjectBigIntId`-typed `@Id` fields (UUID ids are app-generated and unaffected by all of this; `isAssigned()` short-circuits everywhere):

| Strategy / configuration | 6.x (`-61/-62/-63`) | 7.x (`-70/-72`) | Mechanism |
|---|---|---|---|
| Application-assigned (no `@GeneratedValue`) | works | works | `isAssigned()` pass-through, no generator involved |
| `SEQUENCE` (default allocator) | works, tested | works, tested | our `SequenceStyleGenerator` subclass (surrogate-`Long` configure + wrap in `generate()`); on 7.4+ additionally the `getGeneratedType()` fix |
| `@GenericGenerator` sequence + `pooled-lo` optimizer | works, tested | works, tested | same subclass (optimizer config flows through) |
| `AUTO` → resolves to sequence | works, tested | works, tested | same subclass |
| `IDENTITY` | works, tested; **sequence-emulated** (`is_identity = NO`), exception propagation guaranteed by test | works, tested; **true identity column** (`is_identity = YES`), Hibernate-native path, *no exception-propagation test* | 6.x: custom generator + two JDK proxies. 7.x: untouched native `IdentityGenerator`; VO wrap happens in Hibernate's generated-values read (see diagnosis). |
| `TABLE` | never worked, no fixture | never worked, no fixture | `TableGenerator.configure` needs an integral id class (`getIntegralDataTypeHolder`), and our creator passes non-sequence generators through untouched |
| other custom strategies (`@GenericGenerator` non-sequence, `@IdGeneratorType`, ...) | unsupported | unsupported (pass-through; whether they boot or blow up depends on whether they can digest the VO type) | — |

**The README discrepancy:** `README.md:47` says "The library explicitly supports `AUTO`, `IDENTITY` and `SEQUENCE` strategies". As a user-visible behavior statement this is *currently true on the tested paths* — but "explicitly supports" overstates it: IDENTITY on 7.x is not under library control (we neither generate nor convert the value; Hibernate's native machinery does), its 6.x↔7.x DDL semantics differ (emulated sequence vs true identity column — a visible behavior change when upgrading modules), TABLE is silently unsupported, and the exception-propagation guarantee is only tested on 6.x. The README also doesn't mention that the sequence path depends on version-specific internals that a Hibernate patch release can (and did) break.

## Hibernate-internals diagnosis

All citations `github.com/hibernate/hibernate-orm` at tag `7.4.3` unless stated; paths under `hibernate-core/src/main/java/`.

### How a standard `@GeneratedValue` becomes a generator, and where we hook in

Since 7.0, `SimpleValue` carries an opaque creator slot instead of a strategy string: `private GeneratorCreator customIdGeneratorCreator` (`org/hibernate/mapping/SimpleValue.java:112`, accessors at `:416/:421`). `GeneratorCreator` itself is tiny — and marked `@Internal`:

```java
@Internal
@FunctionalInterface
public interface GeneratorCreator {
    Generator createGenerator(GeneratorCreationContext context);
    default boolean isAssigned() { return false; }   // @since 7.0
}
```

Annotation binding fills that slot from lambdas in `org.hibernate.boot.model.internal`. The two relevant ones:

- **SEQUENCE**: `GeneratorAnnotationHelper.handleSequenceGenerator` (`GeneratorAnnotationHelper.java:173–204`) sets a creator lambda that instantiates `SequenceStyleGenerator` and runs it through `prepareForUse(...)`, which calls the generator's `configure(creationContext, properties)`.
- **IDENTITY**: `GeneratorAnnotationHelper.handleIdentityStrategy` (`GeneratorAnnotationHelper.java:369`):

  ```java
  public static void handleIdentityStrategy(SimpleValue idValue) {
      idValue.setCustomIdGeneratorCreator( creationContext -> new IdentityGenerator() );
      idValue.setColumnToIdentity();
  }
  ```

  Note: bare `new`, **no `prepareForUse`, no `configure()`** — load-bearing for the 7.4 validation below.

typed-ids hooks in *after* binding via `AdditionalMappingContributor` (`ObjectBigIntIdTypeGenerationMetadataContributor`): for every entity whose id resolves to an `ObjectBigIntId`, wrap the bound creator in `ObjectBigIntIdGeneratorCreator`, which calls the original creator, checks `instanceof SequenceStyleGenerator`, and either substitutes our subclass or passes the original through.

Everywhere in this pipeline, "the id's type" means the **domain attribute type**. `GeneratorCreationContext.getType()` defaults to `getProperty().getType()` (`org/hibernate/generator/GeneratorCreationContext.java:77`), and `SimpleValue`'s own context implementation returns `SimpleValue.this.getType()` (`SimpleValue.java:1152`) — for us the `CustomType` whose `getReturnedClass()` is the VO. Hibernate never assumes `Long`; it assumes *the attribute type is what generators produce and consume*, with no relational/domain distinction on the generation path.

### Why SEQUENCE needs both halves of the hack

1. **Configure-time**: `SequenceStyleGenerator.configure` captures `identifierType = creationContext.getType()` (`org/hibernate/id/enhanced/SequenceStyleGenerator.java:197`) and builds its optimizer from `identifierType.getReturnedClass()` (`:237`). That class feeds `IdentifierGeneratorHelper.getIntegralDataTypeHolder(Class)` (`org/hibernate/id/IdentifierGeneratorHelper.java:63–72`), which accepts exactly `Long/Integer/Short/BigInteger/BigDecimal` and throws for anything else — same dependency in `SequenceStructure:118`, `TableGenerator:523`, `TableStructure:135`, `IncrementGenerator:131`. A VO class cannot pass. Hence the wrapped context feeding `super.configure(...)` a surrogate `ImmutableNamedBasicTypeImpl<Long>`.
2. **Generate-time**: `SequenceStyleGenerator.generate` (`:561`) returns the raw optimizer value, and the caller uses it **verbatim** — `AbstractSaveEventListener.generateId` (`org/hibernate/event/internal/AbstractSaveEventListener.java:158–177`) is essentially `final Object id = generator.generate(...); ... return id;` with no coercion, no converter, no JavaType wrap. A `BeforeExecutionGenerator` must itself return the exact attribute type. Hence the `wrapJdbcValue(...)` in our `generate()` override.

Neither half is optional, and this is unchanged at 8.0.0.Beta1 and on `main`.

### Why IDENTITY works natively on 7.x — deliberate design, plus one skipped check

After the insert, the generated key is not hand-read as a `Long`. `GetGeneratedKeysDelegate.extractGeneratedValues` (`org/hibernate/id/insert/GetGeneratedKeysDelegate.java:120–130`) hands the JDBC `ResultSet` to `GeneratedValuesHelper.readGeneratedValues` (`org/hibernate/generator/values/internal/GeneratedValuesHelper.java:127`), which builds a full results-assembly pass whose result builders are keyed off the **entity identifier mapping**: `GeneratedValueBasicResultBuilder.buildResult` passes `modelPart.getJdbcMapping()` (`org/hibernate/generator/values/GeneratedValueBasicResultBuilder.java:57–65`, `modelPart` resolved from `getIdentifierMapping()` at `GeneratedValuesHelper.java:226–235`) into `BasicResult`, whose assembler is constructed from `jdbcMapping.getJavaTypeDescriptor()` and `jdbcMapping.getValueConverter()` (`org/hibernate/sql/results/graph/basic/BasicResult.java:57–58`) and applies `toDomainValue(jdbcValue)` in `BasicResultAssembler.assemble`. For a UserType-mapped id that `JdbcMapping` is our `CustomType`, so the raw `Long` is wrapped into the VO by the same code path that materializes the column in any query. **This is deliberate: the extraction is keyed off the id mapping, not off a hardcoded basic type.** The path is identical at 7.2.21 — the `d40fc31` comment ("seems to be capable of handling custom types now") was correct, and the 6.x proxies exist only because 6.1's delegate path consulted `persister.getIdentifierType()` directly.

The asterisk: the 7.4 boot validation passes for IDENTITY only because it never runs. `IdentityGenerator` derives its `generatedType` inside `configure()` (`org/hibernate/id/IdentityGenerator.java:45–52`), and the identity creator lambda never calls `configure()` — so `getGeneratedType()` returns `null` and `GeneratorTypeHelper.checkGeneratorGeneratedType` short-circuits on its null check. Not a correctness problem today (even if `configure()` did run, it would capture the *real* context's VO type and stay self-consistent), but it illustrates how thin the contract is: IDENTITY-with-custom-type is compatible by a chain of accidents-that-happen-to-align, not by a stated guarantee.

### The 7.4 validation (HHH-18210) — what actually broke us

`Generator.getGeneratedType()` (`org/hibernate/generator/Generator.java:172`, `@Incubating`, `@since 7.4`, default `null`, javadoc: "Intended only for use in validation and error reporting") is consumed by exactly one thing, `GeneratorTypeHelper.checkGeneratorGeneratedType` (`org/hibernate/generator/internal/GeneratorTypeHelper.java:16`), called immediately after generator creation at `SimpleValue.java:456` and `Property.java:575` — on the **real** context:

```java
final var attributeType = context.getType().getReturnedClass();
if ( attributeType != null && !wrap( attributeType ).isAssignableFrom( wrap( generatedType ) ) ) {
    throw new MappingException( ... "Generator '%s' declares generated type '%s', which is not assignable to generated attribute '%s' of type '%s'" ... );
}
```

Our subclass was configured through the wrapped context, so the base class's `getGeneratedType()` (`SequenceStyleGenerator.java:184`, returns `identifierType.getReturnedClass()`) reported the surrogate `Long`; the check compared `ObjectBigIntId.isAssignableFrom(Long)` → `MappingException` at boot. The fix (declare our own `getGeneratedType()` returning the true id class, no `@Override` so it compiles on 7.2) makes the check trivially self-consistent. Verified: no `getGeneratedType`/`GeneratorTypeHelper` anywhere at 7.2.21; present and identical from 7.4.0 through `main`; the 7.4 migration guide does not mention it.

### The "overengineering" section — what a clean extension point would need vs what exists

What our use case needs is one sentence: *"let a generator declare that it produces the relational type, and let Hibernate convert to the attribute type at assignment."* What exists instead:

1. **`GeneratorAnnotationHelper.prepareForUse`** (`boot/model/internal/GeneratorAnnotationHelper.java:277`) — the only way to replay Hibernate's own generator initialization sequence — is a 6-argument generic static in an `.internal.` package:

   ```java
   public static <A extends Annotation> void prepareForUse(
           Generator generator, A annotation, MemberDetails idMember,
           Consumer<Properties> configBaseline, BiConsumer<A,Properties> configExtractor,
           GeneratorCreationContext creationContext)
   ```

   Two of the six parameters are functional interfaces that the built-in callers construct inline inside `setCustomIdGeneratorCreator(creationContext -> { ... })` lambdas; one (`idMember`) is threaded through but never read by the method body at 7.4.3. External callers (us) pass four `null`s and hope the inert half stays inert.
2. **`GeneratorCreationContext`** (`generator/GeneratorCreationContext.java:31`, `@Incubating` since 6.2) has 11 methods and no delegating adapter, so overriding the single `getType()` seam costs a 10-method hand-written delegate (`TypeOverrideGeneratorCreationContext` in `-70/-72`). Meanwhile the canonical implementation, `SimpleValue.IdGeneratorCreationContext`, is a **`private` inner class** (`SimpleValue.java:1093`) — not subclassable, not reusable.
3. **The interception point is semantically blind.** `GeneratorCreator` is an opaque `@Internal` lambda; the only way to know what strategy the user picked is to *run it* and `instanceof` the product — which means the original generator is fully instantiated and configured (against the VO type it can't digest) before we can decide to discard it. The 6.x string strategy, for all its stringliness, was at least inspectable *before* instantiation.
4. **The SPI grows by one method per minor, silently.** Verified by grep across tags: `GeneratorCreationContext.getValue()` present by 7.2; `getMemberDetails()` added 7.3 (default); `Generator.getGeneratedType()` added 7.4 (default, HHH-18210); `Generator.requiresIdentityColumn()` added 8.0 (default). Each is an `@Incubating default` that never appears in migration guides — invisible until an integration that lies to one of them (like ours) trips a new consumer of it.

The comparison with what Hibernate considers first-class is instructive: `@IdGeneratorType` (see alternative C) gives a custom generator a supported constructor contract `(A annotation, GeneratorCreationContext context)` — but is explicitly *not* meant to be combined with plain `@GeneratedValue` (team statement: [discourse.hibernate.org/t/deprecation-of-genericgenerator-in-hibernate-6-5/9482](https://discourse.hibernate.org/t/deprecation-of-genericgenerator-in-hibernate-6-5/9482)). Intercepting what `@GeneratedValue` binds — our requirement, because the whole point is that users keep standard JPA annotations — has no supported path at all.

### What 8.0 changes (spoiler: nothing for us, but the door is open)

Diffed 7.4.3 → 8.0.0.Beta1 → `main`: `BeforeExecutionGenerator`, `OnExecutionGenerator`, `GeneratorCreationContext`, `GeneratorCreator`, `InsertGeneratedIdentifierDelegate`, `SequenceStyleGenerator`, `UserTypeResolution` — **zero relevant diff**. `Generator` gains exactly one method: `@Incubating default boolean requiresIdentityColumn() { return false; }` (`@since 8.0`), and `GeneratorBinder` now honors it on the `@IdGeneratorType` path (`identifierValue.setColumnToIdentity()` when true) — added specifically so custom/wrapping generators (see the new `org/hibernate/id/GenericGeneratorGeneration.java` delegating wrapper) can request identity DDL without being `instanceof IdentityGenerator`. `GeneratorTypeHelper` validation is logic-identical on `main` (cosmetic `wrap()` → `PrimitiveHelper.boxedType()` refactor). `UserType.getValueConverter()` unchanged. The 8.0 migration guide's generator content is only the `@GenericGenerator` un-deprecation/rework.

Net: the custom-typed-generated-PK gap is unchanged in 8.0, but 8.0 just accepted a small incubating `Generator` default method solving an adjacent problem for wrapping generators — strong precedent that a well-scoped proposal in this exact area lands.

## Upstream state

- **HHH-18210** — "Up-front validation of generator types" — [hibernate.atlassian.net/browse/HHH-18210](https://hibernate.atlassian.net/browse/HHH-18210), Improvement, Closed/Fixed, fixVersion 7.4.0.CR1, commit `21d6758921`. For custom-typed ids it is strictly negative-but-honest: what used to fail at first insert now fails at boot. No converter awareness.
- **HHH-18124** — "Portable strategy with the new @IdGeneratorType annotation" — [hibernate.atlassian.net/browse/HHH-18124](https://hibernate.atlassian.net/browse/HHH-18124), open/untriaged. Closest neighbor (interop of `@IdGeneratorType` with overrides), does not touch custom id types.
- **HHH-16692** — "Hibernate 6.2 broke hybrid before/after execution ID generators" — Closed/Fixed in 6.4.0; historical evidence that the generator SPI churns and that breakage reports do get fixed.
- Community threads confirming the pain without a tracked enhancement: [discourse.hibernate.org/t/convert-ignored-when-id-is-used/2281](https://discourse.hibernate.org/t/convert-ignored-when-id-is-used/2281) (JPA forbids `@Convert` on `@Id`, so UserType is the only route for VO ids — which is exactly why the converter gap matters), [stackoverflow.com/questions/76418374](https://stackoverflow.com/questions/76418374) (the verbatim `IdentifierGenerationException: Unknown integral data type for ids` failure for IDENTITY + custom type, answered with "write a fully custom generator").

**Conclusion: the specific gap is unreported.** Nobody has filed "standard generators should work with a UserType/converter-backed `@Id`, generating relationally and converting at the boundary" — neither the boot-binding half nor the generated-key/converter half. An enhancement request would be breaking new ground, not piling onto a wontfix.

## Alternatives

### A. Modernized identity hook on 7.x (re-take control of the identity path)

**Mechanism:** ship a 7.x-native `ObjectBigIntIdIdentityGenerator` again — either a custom `OnExecutionGenerator` substituted in `ObjectBigIntIdGeneratorCreator` (extending the `instanceof` gate to `IdentityGenerator`), or a wrapper around the modern `InsertGeneratedIdentifierDelegate`.

**Evidence from source:** there is nothing left for such a hook to do. The 6.x proxies existed to (a) fake `getIdentifierType()` to `Long` and (b) wrap `performInsert`'s return value — on 7.x the generated-values read already flows through the id mapping's `JdbcMapping` and produces the VO (`GeneratedValueBasicResultBuilder.java:57–65`, `BasicResult.java:57–58`), and there is no type question left to answer. A wrapper could only re-add indirection on a hot, churn-prone internal surface (`GeneratedValuesHelper`, delegate selection) — the same fragility class that produced the PR #27 `UndeclaredThrowableException` bug in the first place. The one real 6.x↔7.x delta, emulated-sequence vs true-identity DDL, is arguably a *fix* in 7.x, not a regression to restore.

**Effort:** medium. **Fragility:** high (tracks internals across `-70/-72` and future modules). **User-facing gain:** none demonstrated. **Verdict:** not recommended; the missing piece is a *test* proving the native path's semantics (see E), not code.

### B. Value-converter route (`UserType.getValueConverter()`)

**Mechanism:** `ObjectBigIntIdType` implements `getValueConverter()` (`org/hibernate/usertype/UserType.java:584`, `@Incubating` since 7.0, returns `jakarta.persistence.AttributeConverter<J,?>`, default `null`). When non-null, the `CustomType` constructor (`org/hibernate/type/CustomType.java:78–92`) switches its extractor/binder/literal-formatter to the **relational** JavaType (`Long`) and exposes the converter via `JdbcMapping.getValueConverter()` — the mapping becomes a converted basic value (domain VO, relational `Long`), and this is honored regardless of registration path (our `TypeContributor` route lands in the same `CustomType` constructor via `BasicTypeRegistration`).

**Evidence from source — what it does and does not fix:** every JDBC-boundary consumer becomes converter-aware: attribute read/write, query parameter binding (`SqmParameterInterpretation.java:95`, `JdbcParameterBindingsImpl.java:62`), and the identity generated-key read (`BasicEntityIdentifierMappingImpl.java:442`, `BasicResult.java:57–58`). But the **before-execution generator path never consults it**: `GeneratorCreationContext.getType()` still returns the property's domain `Type` whose `getReturnedClass()` is the VO (converter changes nothing there), `getIntegralDataTypeHolder` still rejects the VO class, and `AbstractSaveEventListener.generateId` still assigns the generator's output verbatim. Unchanged at 8.0.0.Beta1 (`UserTypeResolution.getValueConverter()` still deliberately returns `null` with the same comment; `SequenceStyleGenerator` identical).

**What breaks / costs:** the internal binder/extractor switch is a behavior change across every module that adopts it (SQL literal rendering becomes `Long`-formatted; regression surface on all read/write paths); the converter must be instantiated per resolved VO class since `ObjectBigIntIdJavaType` is a `DynamicParameterizedType` (domain class known only after `setParameterValues`); and 6.x modules can't share it (`getValueConverter` is 7.0+).

**Effort:** medium (plus a real regression-test pass). **Gain today:** none for the generator matrix — sequence hacks stay, identity already works without it. **Verdict:** not worth adopting *for its own sake*; its value is as the library-side half of alternative D, if upstream makes the generation path converter-aware.

### C. `@IdGeneratorType`-based custom annotations (`@ObjectBigIntSequence`, ...)

**Mechanism:** define annotations meta-annotated `@IdGeneratorType(ObjectBigIntIdSequenceStyleGenerator.class)` (`org/hibernate/annotations/IdGeneratorType.java`, since 6.0, javadoc: "the best way to work with customized identifier generation in Hibernate"). Hibernate instantiates the generator through a supported contract — constructor `(A annotation, GeneratorCreationContext context)` or `AnnotationBasedGenerator.initialize` — with no `AdditionalMappingContributor`, no creator wrapping, no `prepareForUse`-with-four-nulls, no `TypeOverrideGeneratorCreationContext` reachable from `.internal.` packages.

**What it buys:** registration moves entirely onto supported API, per-entity generator configuration becomes natural annotation members (sequence name, allocation size, optimizer), and since 8.0 the `@IdGeneratorType` path honors `requiresIdentityColumn()` — so even an identity-flavored typed generator becomes expressible (impossible before 8.0). **What it does not buy:** the generator *internals* stay exactly as hacky — `context.getType()` still reports the VO, so the surrogate-`Long` configure trick, the `generate()` wrap, and the `getGeneratedType()` override all remain; only the *wiring* becomes clean.

**What it costs users:** the headline feature dies — entities annotate `@Id @ObjectBigIntSequence(...)` instead of standard `@Id @GeneratedValue(strategy = SEQUENCE)`. Portable-JPA annotations were the point of the interception design; the Hibernate team's position that `@IdGeneratorType` is not meant to combine with `@GeneratedValue` (discourse 9482, spawned HHH-18124) means this is a genuine fork in user experience, not a drop-in.

**Effort:** low-medium (annotations + wiring + docs; generators already exist). **Fragility:** low (supported API). **Verdict:** good *additive* escape hatch for users who need generator configurations the standard-annotation path can't cover (custom optimizers per entity, future identity-on-8.0) — but as a replacement it trades our users' standard annotations for vendor-specific ones, so it should stay optional.

### D. Upstream fix: make custom-typed generated ids first-class

**The contract to propose:** *when the id attribute's `JdbcMapping` carries a `BasicValueConverter` (e.g. from `UserType.getValueConverter()`), generators operate on the relational type and Hibernate converts at the boundary.* Concretely, three small changes, all shaped like things 7.4/8.0 already accepted:

1. **Type derivation** — `SequenceStyleGenerator.configure` (and `TableGenerator`, `IncrementGenerator`) derive their integral working class from the relational side when a converter is present (`jdbcMapping.getValueConverter().getRelationalJavaType()` instead of raw `getType().getReturnedClass()`), so `getIntegralDataTypeHolder` sees `Long`. Alternatively: a `default Type getRelationalType()` on `GeneratorCreationContext` that resolves the converter — one incubating default method, the 7.2–8.0 house pattern.
2. **Output conversion** — at the single seam where a `BeforeExecutionGenerator`'s output is assigned (`AbstractSaveEventListener.generateId`, or better the generator-invocation helper), apply `valueConverter.toDomainValue(...)` when the generated value is not already assignable to the attribute type. The identity path needs nothing — it already converts (that's the existing deliberate design; this proposal just extends the same principle to before-execution generators).
3. **Validation awareness** — `GeneratorTypeHelper.checkAssignable` accepts a generated type assignable to the *relational* JavaType when a converter exists, instead of comparing raw attribute classes.

**Viability:** 8.0 is in Beta; `requiresIdentityColumn()` (`@Incubating default`, `@since 8.0`, added for wrapping generators, undocumented in the migration guide) proves the maintainers are actively taking exactly this size and shape of `Generator`-area change right now. The gap is unreported, so the first move is an HHH enhancement request with a minimal reproducer (VO id + `@GeneratedValue(SEQUENCE)` + UserType-with-converter), offering a PR. If it lands, typed-ids' 8.0 module could be nearly hack-free: `getValueConverter()` on the type (alternative B) + standard generators, delete `ObjectBigIntIdGeneratorCreator`/`TypeOverrideGeneratorCreationContext`/the sequence subclass for `-80`, keep the existing machinery for 6.1–7.x forever (those lines are closed to SPI additions).

**Effort:** low to write the ticket; medium for the PR; zero control over timeline. **Risk:** proposal rejected or reshaped (fallback: the documented enhancement request still improves the ecosystem conversation and costs a day); even if accepted, helps only 8.0+ users. **Verdict:** highest strategic value per unit of effort, precisely *because* the per-version module matrix means upstream fixes only ever help the newest module — cheap now, compounding later.

### E. Document-and-accept (fix the claims, pin the semantics)

**Mechanism:** three concrete work items, no behavior change:

1. **README honesty** — rewrite the `README.md:47` paragraph: SEQUENCE/AUTO-to-sequence supported via the library's generator (all lines); IDENTITY supported — library-managed on 6.x (sequence-emulated, `is_identity = NO`), Hibernate-native on 7.x (true identity column, `is_identity = YES`); note the DDL difference as a 6→7 migration visible change; TABLE and other strategies unsupported.
2. **Port the exception-propagation test to 7.x** — `IdentityGeneratorExceptionPropagationTest` + the `BigIntDbIdentityGeneratedUniqueTitleEntity` fixture exist only in `-61/-62/-63`. On 7.x it tests Hibernate's native path rather than our code, which is exactly the point: it converts "the native path seems fine" into a pinned, range-tested behavioral contract (constraint violations surface as `ConstraintViolationException`, never wrapped), and it is the tripwire for any future Hibernate change to the generated-values read — the class of change that today we would only notice via user bug reports.
3. **A TABLE-strategy negative test or documented non-support** — cheap way to make the silent gap explicit (assert the boot/insert failure mode so it at least fails *predictably*).

**Effort:** low. **Fragility:** none — it *reduces* fragility by adding the missing tripwire. **Verdict:** do regardless of every other decision.

## Weighing

| | User-facing capability | Maintenance burden across 61–74 matrix | Fragility vs Hibernate churn | Upstream dependency |
|---|---|---|---|---|
| A. 7.x identity re-hook | none proven | +1 hacky class per 7.x+ module | high (delegate internals) | none |
| B. value converter alone | none for generators | touches every 7.x module's type internals | medium (`@Incubating` SPI) | none |
| C. `@IdGeneratorType` annotations | new: per-entity generator config, 8.0 identity | small additive surface | low (supported API) | none |
| D. upstream contract | eventually: full standard-annotation matrix on 8.0+ | negative long-term (deletes hacks for `-80`) | low once landed | total |
| E. document + pin | honesty + regression tripwire | negligible | negative (adds the tripwire) | none |

## Recommendation

**E now, D in parallel, C on demand, B only as D's library-side half, A never** — final call is Filip's:

1. **E immediately** (fits the restructure cycle or right after): README rewrite, 7.x `IdentityGeneratorExceptionPropagationTest` port, TABLE non-support made explicit. This closes the gap between what we claim and what we test, and installs the tripwire that would have caught a 7.4.4-style surprise on the identity path.
2. **D while the 8.0 Beta window is open**: file the HHH enhancement request (converter-aware generation contract, three-seam scope as above, minimal reproducer, PR offer). Cheap, unreported, precedented by `requiresIdentityColumn()` — and it is the only alternative that ever deletes the hacks rather than adding to them.
3. **C when a user actually asks** for a generator configuration the standard path can't express — it is the designed, supported shape for that, and 8.0's `requiresIdentityColumn()` makes even a typed identity annotation expressible.
4. **B not standalone** — adopt `getValueConverter()` only when/if D lands and defines what it buys; adopting it speculatively is regression risk with no generator payoff.
5. **A rejected** — the 7.x native identity path converts via the id mapping by design; re-wrapping it restores the 6.x fragility class (the PR #27 bug lived in exactly that proxy layer) for zero demonstrated gain.

## Open questions

1. **Upstream timing** — file the HHH request now against 8.0 Beta (maximizes the chance it lands in 8.0 GA) or wait until the restructure ships and a `-80` module is concretely planned? Filing now costs a reproducer project and a well-written ticket; the research above is most of the ticket text.
2. **How honest should the README get about IDENTITY?** Options: minimal ("supported; on 7.x via Hibernate's native path") vs full disclosure of the 6→7 DDL semantics change (`is_identity` flip) — the latter matters to anyone diffing schemas across a module upgrade, but lengthens the README. A middle path is a short README note linking to a docs page.
3. **Where does the 7.x exception-propagation fixture live?** `-70` and `-72` testFixtures are currently near-identical copies; the restructure consolidates the *-testing* modules but keeps per-module fixtures. Add the fixture+test to both, or only to `-72` (whose range now extends through 7.4 and carries the real churn risk)?
4. **Is TABLE worth a fixture at all** — negative test asserting the failure mode, or a single README sentence? (No user has ever asked for it; the fixture's only value is making the failure predictable.)
5. **Should the 7.4 `getGeneratedType()` fix be backported to `master`'s `-72` module ahead of the restructure landing**, so a plain 0.11.x user pinning Hibernate 7.4.x isn't broken? The fix is self-contained (one method, no `@Override`) and independent of the restructure — an argument for cherry-picking it out of the worktree branch early.
6. **If D is filed, what exactly do we propose for the type-derivation seam** — converter-aware `SequenceStyleGenerator.configure` (smallest diff, three generator classes) or a `GeneratorCreationContext.getRelationalType()` default (one interface method, cleaner contract, bigger conversation)? Worth deciding before writing the ticket, since it frames the review.
