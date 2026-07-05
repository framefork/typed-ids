# Changelog research: Hypersistence Utils

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/vladmihalcea/hypersistence-utils` git tags (`hypersistence-utils-parent-3.x`; release-date verification via `git log -1 --format=%ci <tag>`) — `3.9.4`/`3.9.5`, `3.13.0`/`3.13.3`, `3.14.0`/`3.14.1`, `3.15.0`/`3.15.1`/`3.15.2`/`3.15.3`.
> - `github.com/vladmihalcea/hypersistence-utils` module layout per tag (`git ls-tree --name-only <tag>` of the `hypersistence-utils-hibernate-*` subdirs) and per-module `pom.xml` `<hibernate.version>` (`git show <tag>:hypersistence-utils-hibernate-NN/pom.xml`) — to enumerate which `hibernate-NN` artifacts EXIST at 3.9.4 vs 3.13.3 vs 3.15.3 and which Hibernate line each actually compiles against.
> - `github.com/vladmihalcea/hypersistence-utils/README.md` @ `3.15.3` (the Hibernate-version → artifact mapping / "broad range of Hibernate versions" section) and the commit that added the `hibernate-73` module (`733fddea Add support for Hibernate ORM 7.3 and Jackson 3`).
> - typed-ids source: `gradle/libs.versions.toml`, all five `modules/typed-ids-hibernate-{61,62,63,70,72}-testing/build.gradle.kts`, `modules/typed-ids/build.gradle.kts`, `modules/typed-ids-testing/build.gradle.kts`, and the test/production sources that import `io.hypersistence.*`.

## TL;DR for typed-ids

- **Two unrelated Hypersistence coordinates are in play, and only one is "Hypersistence Utils".** (1) `io.hypersistence:hypersistence-utils-hibernate-NN` — the Hibernate add-on library — is **test-only** here: it appears solely as `api(...)` in the `typed-ids-hibernate-NN-testing` support modules, never in a published runtime module. (2) `io.hypersistence:hypersistence-tsid` (the standalone TSID generator, a *different* artifact, version `2.1.4`) is the only one that reaches production code, and even there it is `compileOnly` + reflection-guarded. So **nothing about Hypersistence Utils (the `-hibernate-NN` artifacts) can break a published typed-ids artifact** — same "test-only, no production impact" posture as the Spring docs.
- **What we actually consume from HU is its test harness, not its type system.** Every `io.hypersistence.utils.*` import in our tree is `io.hypersistence.utils.test.AbstractHibernateTest` and `io.hypersistence.utils.test.providers.DataSourceProvider` — the JUnit base class + datasource-provider SPI that HU ships in its *main* jar. We use **none** of HU's headline features (JSON `UserType`s, `BaseEntity`, array types, the TSID-backed Hibernate id generator). That narrow surface is why HU version drift is almost a non-event for us.
- **HU publishes a separate artifact per Hibernate line and it LAGS Hibernate by collapsing adjacent lines into one module.** As of latest (`3.15.3`, 2026-06-02) the existing modules are `hibernate-{52,55,60,62,63,70,71,73}`. **There is no `hibernate-72` and no `hibernate-74`** — HU folds 7.1+7.2 into `hibernate-71` and 7.3+7.4 into `hibernate-73`. That is exactly why our `-72-testing` module already reuses `hypersistence-utils-hibernate-71`.
- **The decisive new fact for our 7.3/7.4 work: HU added a `hibernate-73` module in `3.15.0` (2026-01-27), and at `3.15.3` it compiles against Hibernate `7.4.0.Final`** (with `7.3.6.Final` as the commented alternate). So a typed-ids `-73`/`-74` test setup is **possible today** — point it at `hypersistence-utils-hibernate-73:3.15.3`, which already targets 7.4. We would not need to drop HU from those test modules.
- **Our current per-line pins are a mix of frozen-old and current:** `hibernate61→hibernate-60` and `hibernate62→hibernate-62` are pinned at **3.9.4** (the last HU version that still shipped those modules — they are now "commercial support only" and frozen upstream), while `hibernate63`/`hibernate70`/`hibernate71` are at **3.13.3**. The frozen-old pins must stay as-is to keep the old modules building; the active pins can move to `3.15.3` cheaply.
- **Direction:** newer-HU support is additive and low-stakes because the surface is a test harness. Bumping the active `hibernate-{63,70,71}` pins `3.13.3 → 3.15.3` and adding a `hibernate-73:3.15.3` entry for 7.3/7.4 coverage is the whole story; the frozen `3.9.4` pins for `-60`/`-62` stay untouched to preserve the old Hibernate upgrade paths.

## How typed-ids uses Hypersistence

### The `-hibernate-NN` artifacts — test-only, and only the test harness

The Hibernate add-on appears **exclusively** in the per-version testing support modules, each as a single `api(...)` line:

| typed-ids module | catalog alias | resolves to artifact | version |
|---|---|---|---|
| `typed-ids-hibernate-61-testing` | `hypersistence-utils-hibernate61` | `hypersistence-utils-hibernate-60` | **3.9.4** |
| `typed-ids-hibernate-62-testing` | `hypersistence-utils-hibernate62` | `hypersistence-utils-hibernate-62` | **3.9.4** |
| `typed-ids-hibernate-63-testing` | `hypersistence-utils-hibernate63` | `hypersistence-utils-hibernate-63` | **3.13.3** |
| `typed-ids-hibernate-70-testing` | `hypersistence-utils-hibernate70` | `hypersistence-utils-hibernate-70` | **3.13.3** |
| `typed-ids-hibernate-72-testing` | `hypersistence-utils-hibernate71` *(note: 71, not 72)* | `hypersistence-utils-hibernate-71` | **3.13.3** |

Note the two indirections that already encode HU's "lag":
- the catalog alias `hypersistence-utils-hibernate61` maps to artifact **`hibernate-60`** — HU never shipped a `hibernate-61` module, so Hibernate 6.1 is covered by HU's `-60` build;
- `-72-testing` deliberately wires `hypersistence-utils-hibernate71` (HU has **no** `hibernate-72` module — it tests 7.2 with the `-71` build).

What we import from these artifacts is just the test scaffolding (verified across the `*-testing` source trees):

```java
import io.hypersistence.utils.test.AbstractHibernateTest;            // JUnit base: builds a SessionFactory, init()/destroy() lifecycle
import io.hypersistence.utils.test.providers.DataSourceProvider;     // SPI our PostgreSQL/MySQL container providers implement
```

`AbstractPostgreSQLIntegrationTest`/`AbstractMySQLIntegrationTest` extend `AbstractHibernateTest`; `AbstractContainerDataSourceProvider` implements `DataSourceProvider`. These classes live in HU's `src/main/java/io/hypersistence/utils/test/...` (i.e. they are part of the published main jar, not a `tests` classifier — hence `api`, not `testFixtures`). **We touch none of HU's `UserType`/JSON/array/id-generator API**, so HU's per-Hibernate type-system code — the part that genuinely drifts between modules — is irrelevant to us.

### The `hypersistence-tsid` artifact — the only production touch (optional)

Separate coordinate `io.hypersistence:hypersistence-tsid` (catalog `hypersistence-tsid = 2.1.4`), **not** part of Hypersistence Utils' Hibernate modules. Used in the core module:

- `modules/typed-ids/build.gradle.kts`: `compileOnly(libs.hypersistence.tsid)` — optional, never forced on consumers.
- `org/framefork/typedIds/bigint/random/HypersistenceTsidGeneratorFactory.java` imports `io.hypersistence.tsid.TSID` and guards every use behind `ReflectionHacks.classExists("io.hypersistence.tsid.TSID")` (`AVAILABLE`), calling only `TSID.Factory.getTsid().toLong()`.
- `modules/typed-ids-testing/build.gradle.kts`: `api(libs.hypersistence.tsid)` so the optional path is exercised in tests.

This is a tiny, stable surface (`TSID.Factory.getTsid()`), on its own version line (`2.1.4`), and is **out of scope for the Hibernate-artifact question** below — flagged here only so "Hypersistence in production" isn't mistaken for HU-the-Hibernate-library leaking into our runtime. It does not.

## The artifact-availability mapping (the deliverable)

Which `hypersistence-utils-hibernate-NN` modules **exist** at a given HU release, and which Hibernate line each module's `pom.xml` actually compiles against. Verified by listing the module dirs at each tag and reading each module's `<hibernate.version>`.

### Module set per HU release

| HU release | Hibernate modules shipped |
|---|---|
| **3.9.4** (2025-03-11) | `52`*, `55`, `60`, `62`, `63` |
| **3.13.3** (2025-12-12) | `63`, `70`, `71` |
| **3.14.0 / 3.14.1** (2025-12) | `63`, `70`, `71` |
| **3.15.0** (2026-01-27) | `63`, `70`, `71`, **`73`** ← `hibernate-73` introduced here |
| **3.15.3** (2026-06-02, latest) | `63`, `70`, `71`, `73` |

\* `hibernate-52` is documented (README) at `3.7.6`; the active set narrowed over time. The point: HU **drops** old modules from new releases (the `-55/-60/-62` modules vanished after the `3.9.x` line) and **adds** modules for new Hibernate lines (`-70`/`-71` arrived for the 3.13 line; `-73` for 3.15). Old modules remain on Maven Central at their last-built version — they are not deleted, just frozen.

### What each *latest-available* module actually targets

Per HU README @ `3.15.3` and the modules' own `<hibernate.version>`:

| HU artifact | covers Hibernate line(s) | compiled against (at its newest pinned version) | newest HU version shipping it |
|---|---|---|---|
| `hypersistence-utils-hibernate-73` | **7.3, 7.4** | **`7.4.0.Final`** (alt `7.3.6.Final`) | 3.15.3 |
| `hypersistence-utils-hibernate-71` | **7.1, 7.2** | **`7.2.17.Final`** (alt `7.1.11.Final`) | 3.15.3 |
| `hypersistence-utils-hibernate-70` | 7.0 | 7.0.x | 3.15.3 |
| `hypersistence-utils-hibernate-63` | 6.3, 6.4, 6.5, 6.6 | 6.6.x | 3.15.3 |
| `hypersistence-utils-hibernate-62` | 6.2 *(commercial-support only)* | 6.2.x | **3.9.4** (frozen) |
| `hypersistence-utils-hibernate-60` | 6.0, 6.1 *(commercial-support only)* | 6.0/6.1 | **3.9.4** (frozen) |
| `hypersistence-utils-hibernate-55` | 5.5 | 5.5.x | 3.9.5 (frozen) |
| `hypersistence-utils-hibernate-52` | 5.2 | 5.2.x | 3.7.6 (frozen) |

**The two facts that matter for our newer-Hibernate work:**

1. **There is no `hibernate-72` and no `hibernate-74` artifact, and there likely never will be** — HU's policy is one module per *pair* of adjacent lines (`-71` = 7.1/7.2, `-73` = 7.3/7.4). HU artifacts deliberately work against a Hibernate line *newer* than their name suggests: `-71` compiles against **7.2.17**, `-73` compiles against **7.4.0**. So "the artifact name lags the Hibernate version it supports" is by design.
2. **`hibernate-73` already exists and already targets 7.4** (since `3.15.0`, hardened through `3.15.3`). This is the green light for adding typed-ids 7.3 *and* 7.4 test coverage backed by a real HU build, rather than having to reuse an older artifact or drop HU.

### Diff: 3.13.3 (our active pins) vs 3.15.3 (latest)

- `-71` module's compile target moved **7.1.11 → 7.2.17** — i.e. bumping our `hibernate71` pin `3.13.3 → 3.15.3` makes the `-72-testing` HU build actually compile against 7.2, a *closer* match than today (where it's a 7.1 build used to test our 7.2 module).
- **New `-73` module** appeared (3.15.0), targeting 7.3/7.4.
- `-63` and `-70` modules unchanged in scope, just patch-bumped.
- No module our catalog references was **removed** between 3.13.3 and 3.15.3, so an active-pin bump is purely additive.

## Version timeline (newer-than-ours)

HU does not keep a top-level CHANGELOG; the relevant deltas are module-set and Hibernate-target changes, captured above. Dates verified against `hypersistence-utils-parent-*` git tags.

### 3.13.x — our active line (3.13.0 2025-12-01, 3.13.3 2025-12-12)

- Baseline. Ships `hibernate-{63,70,71}`; `-71` targets Hibernate 7.1.11. This is what `hibernate63`/`hibernate70`/`hibernate71` pin today.
- **Impact on typed-ids:** none beyond what we already build. The test harness (`AbstractHibernateTest`, `DataSourceProvider`) is what we use, and it is stable across this whole window.

### 3.14.0 — 2025-12-16 (3.14.1 2025-12-17)

- Module set unchanged (`63/70/71`). Maintenance + dependency bumps on the 3.13 → 3.14 step.
- **Impact on typed-ids: none.** No new module, no change to the test-harness classes we consume. A `3.13.3 → 3.14.x` bump of the active pins would be a no-op for us.

### 3.15.0 — 2026-01-27 (3.15.1 01-28, 3.15.2 02-09, 3.15.3 2026-06-02)

- **Headline: `hibernate-73` module added** (commit `Add support for Hibernate ORM 7.3 and Jackson 3`). The same release line also bumps `-71`'s compile target to Hibernate 7.2.17 and `-73`'s to 7.4.0. README's supported-range banner now leads with **"Hibernate ORM 7.3, 7.2, 7.1, 7.0, … 6.6 … down to 5.0"**.
- **Impact on typed-ids: opportunity, not breakage.** Nothing we consume changed signature; the test harness classes are still `io.hypersistence.utils.test.*`. What 3.15.x *enables* is (a) moving the active pins onto a current line, and (b) a real `hibernate-73` artifact to back 7.3/7.4 test modules. `3.15.3` is the latest as of 2026-06-28.

## Breaking-change deep dive

**There is no major boundary on the HU axis for us.** HU is on a single `3.x` line; the only "structural" event in our window is the *addition* of the `hibernate-73` module (additive) and the *narrowing* of which old modules ship in new releases (which doesn't affect us because we pin the old modules at their last-shipping version, `3.9.4`). Because our entire HU consumption is the test harness (`AbstractHibernateTest` + `DataSourceProvider` SPI) and not HU's type system, the per-Hibernate-version API drift that HU itself wrestles with **never reaches our code**. Should HU ever change the `AbstractHibernateTest`/`DataSourceProvider` shape, it would break a test compile — caught immediately by CI, never shipped — so the blast radius is contained to our build, never a published artifact.

The one thing worth restating as a non-break: HU's `hibernate-71` and `hibernate-73` artifacts are *named* one line behind the Hibernate version they target (7.2 and 7.4 respectively). This is intentional and is the mechanism by which a single HU artifact spans two Hibernate lines — not a versioning bug to route around.

## Compatibility-strategy implications

Surfacing, not deciding:

- **Test-only ⇒ HU never gates a typed-ids release.** Breaking changes anywhere in HU's Hibernate type system cannot affect our published modules; at worst they affect a `*-testing` module's compile, visible in CI. This mirrors the Spring Data / Spring Framework conclusion: the HU axis is a *testing-convenience* axis, decoupled from our runtime contract.
- **7.3/7.4 coverage is unblocked by HU.** The Hibernate doc recommends extending the `-72` module's range with `testing-typed-ids-hibernate-{73,74}-indexed` modules. HU can back exactly that: add a catalog entry `hypersistence-utils-hibernate73 = { …, name = "hypersistence-utils-hibernate-73", version = "3.15.3" }` and have both the 7.3 and 7.4 test modules depend on it (the `-73` artifact compiles against 7.4.0, so it suits both). No need to reuse `-71` for 7.3/7.4, and no need to drop HU from those modules.
- **The `-71`-for-7.2 reuse should arguably move to a current pin.** Today `-72-testing` uses `hibernate-71:3.13.3` (a 7.1.11 build). Bumping the `hibernate71` pin to `3.15.3` makes that HU build a 7.2.17 build — a strictly better match for testing our `-72` module — at zero source cost (test harness API unchanged).
- **Preserve-old-paths: the `3.9.4` pins are load-bearing and must stay.** `hibernate61→hibernate-60` and `hibernate62→hibernate-62` are pinned at `3.9.4` precisely because that is the **last** HU release that shipped those modules; newer HU releases dropped them. Bumping those two aliases would fail to resolve. They stay frozen at `3.9.4`, which is exactly what keeps the old Hibernate 6.0/6.1/6.2 test modules building — consistent with Filip's intent to preserve old upgrade paths.
- **`hypersistence-tsid` is a separate decision.** Its `2.1.4` pin is independent of the `-hibernate-NN` story and of any Hibernate version; it only needs revisiting if the TSID API (`TSID.Factory.getTsid()`) changes, which it has not.

### Open questions / decisions to make later (not decided here)

1. **Active-pin bump.** Move `hibernate63`/`hibernate70`/`hibernate71` from `3.13.3 → 3.15.3`? Low-risk (test-harness API stable), and it aligns the `-71` build with Hibernate 7.2. Decide together with the Hibernate-catalog bumps.
2. **Wire 7.3/7.4 test modules to `hibernate-73:3.15.3`.** Confirm with a CI run that `AbstractHibernateTest` + our container `DataSourceProvider`s compile and run against the `-73` artifact (it pulls Hibernate 7.4.0 transitively — verify that doesn't fight whatever Hibernate version the `testing-…-{73,74}-indexed` module forces; the `*-indexed` modules override the Hibernate version explicitly, so the HU transitive must be allowed to be overridden, not pinned).
3. **Leave the frozen `3.9.4`/`3.9.5` pins alone.** Confirm we don't accidentally try to align them to the active line — they cannot move without losing the old modules.
4. **Watch for an HU `hibernate-74`/`-80`.** None exists today; HU pairs lines (`-73` = 7.3/7.4). If/when Hibernate 8.0 lands, HU will likely ship a new module — track it for whenever a typed-ids `-80` module materialises, but it is not on the critical path now.
