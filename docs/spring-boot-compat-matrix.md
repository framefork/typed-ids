# Spring Boot → Hibernate / Jackson compatibility matrix (typed-ids starter boundaries)

> Research doc deriving the version boundaries for versioned `typed-ids-spring*` starter artifacts. Compiled 2026-07-05.
> The question: at which Spring Boot versions does upgrading Boot **force** a consumer to switch their `typed-ids-hibernate-NN` module or their Jackson flavor? Those forced switches are the natural starter boundaries. Legacy support target: Spring Boot **3.0.x** onwards.
> Sources read:
> - `github.com/spring-projects/spring-boot` git tags — every GA tag `v3.0.0` … `v4.1.0` (all `-M*`/`-RC*` skipped). Managed versions read directly from the platform BOM at each tag: `spring-boot-project/spring-boot-dependencies/build.gradle` on the 3.x lines, `platform/spring-boot-dependencies/build.gradle` on the 4.x lines (the BOM moved out of `spring-boot-project/` as part of Boot 4's modularization). Where the BOM declares a version as a `${…}` placeholder (Spring Framework from 3.0.7; Jackson from 3.0.12) the concrete value is resolved from root `gradle.properties` at the same tag. Release dates are tag commit dates (`git log -1 --format=%cs`).
> - typed-ids module ranges from `docs/changelog-hibernate.md` / `docs/changelog-jackson.md` and the restructuring proposal (`docs/restructuring-proposal.md`).
> Every version below is read from the BOM at the tag — none from memory. Spot-checks pass: `3.5.8` → Hibernate `6.6.36` / Jackson `2.19.4`; `4.0.0` → `7.1.8` / `3.0.2`; `4.0.1` → `7.2.0`; `4.1.0` → `7.4.1` / `3.1.4`.

## typed-ids module overlay (the axes we switch on)

- **Hibernate module:** `-61` ↔ 6.1.x · `-62` ↔ 6.2.x · `-63` ↔ 6.3–6.6 · `-70` ↔ 7.0–7.1 · `-72` ↔ 7.2 (planned range extension covers 7.3/7.4, per `docs/changelog-hibernate.md`).
- **Jackson flavor:** Jackson 2.x (`com.fasterxml.jackson`) ↔ `typed-ids-jackson2` · Jackson 3.x (`tools.jackson`) ↔ `typed-ids-jackson3`.

## 1. TL;DR — the derived boundary set

The minimal set of contiguous Boot ranges within which **(required hibernate module, jackson flavor)** is constant. Each block is a candidate `typed-ids-spring*` starter boundary:

| # | Spring Boot range | Hibernate module | Jackson flavor | Driving Hibernate / Jackson |
|---|---|---|---|---|
| **A** | **3.0.0 – 3.0.13** | `typed-ids-hibernate-61` | `typed-ids-jackson2` | Hibernate 6.1.x, Jackson 2.14.x |
| **B** | **3.1.0 – 3.1.12** | `typed-ids-hibernate-62` | `typed-ids-jackson2` | Hibernate 6.2.x, Jackson 2.15.x |
| **C** | **3.2.0 – 3.5.16** | `typed-ids-hibernate-63` | `typed-ids-jackson2` | Hibernate 6.3 → 6.6, Jackson 2.15 → 2.21 |
| **D** | **4.0.0 only** | `typed-ids-hibernate-70` | `typed-ids-jackson3` | Hibernate 7.1.8, Jackson 3.0.2 |
| **E** | **4.0.1 – 4.1.0** | `typed-ids-hibernate-72` | `typed-ids-jackson3` | Hibernate 7.2.0 → 7.4.1, Jackson 3.0.3 → 3.1.4 |

Five blocks. The three "dangerous" facts baked into this set:

1. **Block C spans four Boot minor lines (3.2 → 3.5)** with no forced switch: Hibernate walks 6.3 → 6.4 → 6.5 → 6.6 entirely inside the `-63` module's range, and Jackson stays on the 2.x flavor the whole way. One starter can honestly declare "Spring Boot 3.2 through 3.5".
2. **Block D is a single Boot patch (4.0.0 alone).** Boot 4.0.0 pins Hibernate 7.1.8 (`-70`); the very next patch, 4.0.1, moves to 7.2.0 (`-72`). This is the **only mid-patch-line module crossing in the entire 3.0 → 4.1 history** — see §3.
3. **The Jackson 2 → 3 flip and the 6.6 → 7.1 (`-63` → `-70`) Hibernate jump happen at the exact same tag: 4.0.0.** So the Boot-3 → Boot-4 boundary switches both axes at once; there is no Boot version that needs `jackson3` with a Hibernate-6 module, or vice-versa.

## 2. Full matrix — one row per Boot minor line

Hibernate/Jackson/Spring-Framework values are the managed versions in Boot's platform BOM at the line's first and last GA tag (`.Final` suffix dropped from Hibernate for width). "J2 mgmt" is Boot 4's retained Jackson-2 dependency-management floor (`jackson2Version`) — the deprecated `spring-boot-jackson2` stop-gap; Boot 3 has a single Jackson so the column is n/a there.

| Boot line | GA patch range | Hibernate (start → end) | Jackson (start → end) | J2 mgmt (Boot 4) | Spring Framework | typed-ids-hibernate | Jackson flavor |
|---|---|---|---|---|---|---|---|
| **3.0.x** | 3.0.0 – 3.0.13 | 6.1.5 → 6.1.7 | 2.14.1 → 2.14.3 | — | 6.0.2 → 6.0.14 | `-61` | `jackson2` |
| **3.1.x** | 3.1.0 – 3.1.12 | 6.2.2 → 6.2.25 | 2.15.0 → 2.15.4 | — | 6.0.9 → 6.0.21 | `-62` | `jackson2` |
| **3.2.x** | 3.2.0 – 3.2.12 | 6.3.1 → 6.4.10 | 2.15.3 → 2.15.4 | — | 6.1.1 → 6.1.15 | `-63` | `jackson2` |
| **3.3.x** | 3.3.0 – 3.3.13 | 6.5.2 → 6.5.3 | 2.17.1 → 2.17.3 | — | 6.1.8 → 6.1.21 | `-63` | `jackson2` |
| **3.4.x** | 3.4.0 – 3.4.13 | 6.6.2 → 6.6.39 | 2.18.1 → 2.18.5 | — | 6.2.0 → 6.2.15 | `-63` | `jackson2` |
| **3.5.x** | 3.5.0 – 3.5.16 | 6.6.15 → 6.6.53 | 2.19.0 → 2.21.4 | — | 6.2.7 → 6.2.19 | `-63` | `jackson2` |
| **4.0.x** | 4.0.0 – 4.0.7 | **7.1.8 → 7.2.19** | 3.0.2 → 3.1.4 | 2.20.1 → 2.21.4 | 7.0.1 → 7.0.8 | **`-70` (4.0.0) → `-72` (4.0.1+)** | `jackson3` |
| **4.1.x** | 4.1.0 | 7.4.1 | 3.1.4 | 2.21.4 | 7.0.8 | `-72` (extended range) | `jackson3` |

Notes on line internals (full per-patch data in the appendix):
- The **3.5.x line starts on an earlier Hibernate than the 3.4.x line ends** (3.5.0 = 6.6.15 vs 3.4.13 = 6.6.39) because the two lines were branched and patched in parallel; both stay entirely inside 6.6 → `-63`, so it is not a boundary event.
- Boot **4.0.x is not a fixed Hibernate**: it slides across a Hibernate *minor* boundary mid-line (7.1 → 7.2), the one row where the "typed-ids-hibernate" cell is not a single value. This is the load-bearing case in §3 and §5.
- Boot **4.1.0** lands on Hibernate 7.4.1 — inside the `-72` module only under the planned 7.3/7.4 range extension (`docs/changelog-hibernate.md`). Boot never managed a 7.3 (it jumps 7.2.19 → 7.4.1).

## 3. Boundary crossings (every forced module switch)

A "crossing" = the managed Hibernate moves from one typed-ids module's range into another's, forcing a consumer who follows Boot to swap their `typed-ids-hibernate-NN` dependency. Exact patch pairs:

### Across-minor crossings (the expected, safe-to-plan ones)

| Boot step | Hibernate | typed-ids module | Also crosses Jackson flavor? |
|---|---|---|---|
| 3.0.13 → 3.1.0 | 6.1.7 → 6.2.2 | `-61` → `-62` | no (both `jackson2`) |
| 3.1.12 → 3.2.0 | 6.2.25 → 6.3.1 | `-62` → `-63` | no (both `jackson2`) |
| **3.5.16 → 4.0.0** | 6.6.53 → 7.1.8 | **`-63` → `-70`** | **YES — `jackson2` → `jackson3`** (the major boundary; both axes flip at one tag) |

### Mid-line crossing — ⚠️ THE DANGEROUS ONE ⚠️

| Boot step | Hibernate | typed-ids module | Note |
|---|---|---|---|
| **4.0.0 → 4.0.1** | **7.1.8 → 7.2.0** | **`-70` → `-72`** | **Same minor line, one patch apart.** A consumer who "uses Spring Boot 4.0.x" is really on Hibernate 7.1 *or* 7.2 depending on the patch — different typed-ids modules. This is the only crossing that happens *inside* a Boot minor line across the whole 3.0 → 4.1 range, and the whole reason Block D (4.0.0) is a range of one. |

### Mid-line Hibernate bumps that do NOT cross a typed-ids boundary (informational)

These move Hibernate but stay inside one module's range, so they are safe — no consumer action:

- **3.2.x:** Hibernate 6.3.1 → 6.4.10 within the line (a Hibernate *minor* 6.3 → 6.4 bump at 3.2.0 → 3.2.1), all inside `-63`.
- **3.3.x → 3.4.x → 3.5.x tails:** Hibernate walks 6.5 → 6.6 across these lines, all inside `-63`.
- **3.0.x / 3.1.x / 3.3.x / 3.4.x / 3.5.x:** each line's internal patch bumps stay within a single Hibernate minor (`-61` / `-62` / `-63`).
- **4.0.1 → 4.0.7:** Hibernate 7.2.0 → 7.2.19, all inside `-72`.
- **4.0.7 → 4.1.0:** Hibernate 7.2.19 → 7.4.1 — a Hibernate *minor* jump (7.2 → 7.4) but **not** a typed-ids crossing, since `-72`'s planned range covers 7.4.

## 4. Jackson 2 → 3 boundary

Confirmed **exactly at Spring Boot 4.0.0**, and confirmed by the BOM group-id, not just the version number:

- **3.5.16** (last 3.x GA): `library("Jackson Bom", "2.21.4")` under `group("com.fasterxml.jackson")` → Jackson **2** → `typed-ids-jackson2`.
- **4.0.0** (first 4.x GA): `library("Jackson Bom", "3.0.2")` under `group("tools.jackson")` (with a `permit` for the lone `com.fasterxml.jackson.core:jackson-annotations` module) → Jackson **3** → `typed-ids-jackson3`.
- **No Boot 3.x line ever managed Jackson 3.** The 3.x managed Jackson climbs monotonically 2.14.1 (3.0.0) → 2.21.4 (3.5.16) and never leaves the 2.x/`com.fasterxml` line. So a Boot-3 consumer never needs `jackson3`.
- **Boot 4 keeps Jackson 2 *dependency management* alive** (`jackson2Version` 2.20.1 at 4.0.0 → 2.21.4 at 4.0.7/4.1.0) for the deprecated `spring-boot-jackson2` / `spring.jackson.use-jackson2-defaults` stop-gap. So a Jackson-2 typed-ids artifact still *resolves* under Boot 4 for a consumer on the stop-gap — but the steady state on Boot 4 is `jackson3`, and the starter should target that.

The Jackson boundary therefore coincides exactly with the `-63` → `-70` Hibernate boundary at 4.0.0 — one Boot major bump, both axes.

## 5. Starter design implications

Mapping the five boundary blocks (§1) onto `typed-ids-spring*` starters. For each: the Boot range it can declare, and what it would pull.

| Starter candidate | Declares support for Boot | Pulls hibernate | Pulls jackson | springdoc line (if openapi) |
|---|---|---|---|---|
| block A | 3.0.x | `-61` | `jackson2` | 2.x |
| block B | 3.1.x | `-62` | `jackson2` | 2.x |
| block C | 3.2.x – 3.5.x | `-63` | `jackson2` | 2.x |
| block D | 4.0.0 | `-70` | `jackson3` | 3.x |
| block E | 4.0.1 – 4.1.x | `-72` | `jackson3` | 3.x |

Key observations and edge cases to decide (surfacing options, not deciding):

- **Blocks A and B are legacy tails (Boot 3.0 / 3.1, both EOL upstream).** If the starter effort only needs to cover *maintained* Boot, blocks A+B collapse away and the practical set is **C (3.2–3.5) / D (4.0.0) / E (4.0.1–4.1)** — or even just C + E if the 4.0.0-only case is handled by requiring 4.0.1+. Whether to ship `-61`/`-62`-backed starters at all is a scope call; the underlying `typed-ids-hibernate-61/-62` modules exist and work regardless, so a consumer on Boot 3.0/3.1 can always wire them by hand without a starter.
- **The 4.0.0-vs-4.0.1 split (Block D) is the sharp edge.** 4.0.0 alone needs `-70` (Hibernate 7.1); 4.0.1+ needs `-72` (Hibernate 7.2). A single "Boot 4" starter pulling one Hibernate module cannot be correct for both. Options:
  1. **Starter requires Boot ≥ 4.0.1** and pulls `-72`, documenting that 4.0.0 is unsupported (cleanest; 4.0.0 was superseded within a month by 4.0.1 on 2025-12-18).
  2. **A dedicated micro-range starter** pinned to 4.0.0 pulling `-70`, plus the main 4.0.1+ starter pulling `-72` (mirrors the Hibernate `-indexed` range-test pattern, but adds an artifact for a single superseded patch).
  3. **Starter pulls `-70`** and relies on `-70`'s runtime tolerance against Hibernate 7.2 (the changelog notes `-70` covers 7.0–7.1; running it on 7.2 is out-of-declared-range and would need its own validation — not recommended without a test module).
  Option 1 is the low-friction default; option 2 is the fully-correct-but-heavier one.
- **Block E already merges Boot 4.0.1 and 4.1.x under one `-72` starter** — but only because `-72`'s range is *planned* to extend to 7.3/7.4. That extension is a static-diff claim in `docs/changelog-hibernate.md` not yet proven under a Boot-4.1-managed stack end-to-end. If a Boot-4.1 test module (Hibernate 7.4.1) is not added, block E's upper half (4.1.x) is unvalidated; if the extension is rejected, 4.1.x splits off into its own `-74`-style block.
- **Beyond Hibernate + Jackson, the only other Boot-major-aligned pick for a starter that pulls `typed-ids-openapi-spring-boot` / `typed-ids-spring-convert` is the springdoc line: springdoc-openapi 2.x for Boot 3, 3.x for Boot 4** (proposal §"OpenAPI stack" — the one compiled jar is proven to serve both, so this is a BOM/starter-dependency pick, not a code split). springdoc is *not* in the Boot BOM (it ships its own BOM), so the starter would pin it explicitly per block. Both `typed-ids-openapi-spring-boot` and `typed-ids-spring-convert` are single-jar across the whole Boot 3.5 ↔ 4.0 range (proven in the proposal), so they do not add boundaries of their own — the starter's boundaries stay driven purely by the Hibernate module + Jackson flavor.
- **Spring Framework is not an independent boundary axis:** it moves in lockstep with the Boot major (6.0 → 6.1 → 6.2 across Boot 3.x, 7.0 across Boot 4.x) and `typed-ids-spring-convert` is proven single-jar across the 6.2 ↔ 7.0 split, so Spring Framework never forces a starter boundary that Hibernate/Jackson haven't already forced.

## 6. Open questions for the maintainer

1. **Scope of legacy starters:** ship starters for blocks A (3.0.x) and B (3.1.x) at all, or start coverage at block C (3.2–3.5) since 3.0/3.1 are upstream-EOL and the raw `-61`/`-62` modules remain hand-wireable?
2. **The 4.0.0 edge:** require Boot ≥ 4.0.1 for the "Boot 4" starter (pull `-72`), or add a dedicated 4.0.0-pinned `-70` micro-starter? (§5, options 1–3.)
3. **4.1.x validation:** is `-72`'s planned 7.4 range extension proven enough to fold 4.1.x into block E, or should a Boot-4.1 (`-72`-on-Hibernate-7.4) test module land first, with 4.1.x as its own provisional block until green?
4. **Starter granularity vs Boot minors:** block C intentionally spans four Boot minor lines (3.2–3.5). Is one starter declaring "Boot 3.2–3.5" acceptable, or is per-Boot-minor starter naming preferred for discoverability even though the pulled modules are identical?
5. **Jackson-2-under-Boot-4 stop-gap:** do we care to support a consumer running `spring.jackson.use-jackson2-defaults` on Boot 4 (Jackson-2 management retained through 4.1.0) with a `jackson2`-flavored variant, or is `jackson3` the only supported Boot-4 flavor?

## Appendix — full per-patch data (all GA tags v3.0.0 … v4.1.0)

Managed versions from the platform BOM at each tag; `J2 mgmt` = `jackson2Version` (Boot 4 only). Read via `git show <tag>:<bom-path>` + `gradle.properties` resolution.

| Boot | Hibernate | Jackson | J2 mgmt | Spring Fw | Released |
|---|---|---|---|---|---|
| 3.0.0 | 6.1.5.Final | 2.14.1 | — | 6.0.2 | 2022-11-24 |
| 3.0.1 | 6.1.6.Final | 2.14.1 | — | 6.0.3 | 2022-12-22 |
| 3.0.2 | 6.1.6.Final | 2.14.1 | — | 6.0.4 | 2023-01-20 |
| 3.0.3 | 6.1.7.Final | 2.14.2 | — | 6.0.5 | 2023-02-23 |
| 3.0.4 | 6.1.7.Final | 2.14.2 | — | 6.0.6 | 2023-03-03 |
| 3.0.5 | 6.1.7.Final | 2.14.2 | — | 6.0.7 | 2023-03-23 |
| 3.0.6 | 6.1.7.Final | 2.14.2 | — | 6.0.8 | 2023-04-20 |
| 3.0.7 | 6.1.7.Final | 2.14.3 | — | 6.0.9 | 2023-05-18 |
| 3.0.8 | 6.1.7.Final | 2.14.3 | — | 6.0.10 | 2023-06-22 |
| 3.0.9 | 6.1.7.Final | 2.14.3 | — | 6.0.11 | 2023-07-20 |
| 3.0.10 | 6.1.7.Final | 2.14.3 | — | 6.0.11 | 2023-08-24 |
| 3.0.11 | 6.1.7.Final | 2.14.3 | — | 6.0.12 | 2023-09-21 |
| 3.0.12 | 6.1.7.Final | 2.14.3 | — | 6.0.13 | 2023-10-19 |
| 3.0.13 | 6.1.7.Final | 2.14.3 | — | 6.0.14 | 2023-11-23 |
| 3.1.0 | 6.2.2.Final | 2.15.0 | — | 6.0.9 | 2023-05-18 |
| 3.1.1 | 6.2.5.Final | 2.15.2 | — | 6.0.10 | 2023-06-22 |
| 3.1.2 | 6.2.6.Final | 2.15.2 | — | 6.0.11 | 2023-07-20 |
| 3.1.3 | 6.2.7.Final | 2.15.2 | — | 6.0.11 | 2023-08-24 |
| 3.1.4 | 6.2.9.Final | 2.15.2 | — | 6.0.12 | 2023-09-21 |
| 3.1.5 | 6.2.13.Final | 2.15.3 | — | 6.0.13 | 2023-10-19 |
| 3.1.6 | 6.2.13.Final | 2.15.3 | — | 6.0.14 | 2023-11-23 |
| 3.1.7 | 6.2.17.Final | 2.15.3 | — | 6.0.15 | 2023-12-21 |
| 3.1.8 | 6.2.20.Final | 2.15.3 | — | 6.0.16 | 2024-01-19 |
| 3.1.9 | 6.2.22.Final | 2.15.4 | — | 6.0.17 | 2024-02-22 |
| 3.1.10 | 6.2.22.Final | 2.15.4 | — | 6.0.18 | 2024-03-21 |
| 3.1.11 | 6.2.24.Final | 2.15.4 | — | 6.0.19 | 2024-04-18 |
| 3.1.12 | 6.2.25.Final | 2.15.4 | — | 6.0.21 | 2024-05-23 |
| 3.2.0 | 6.3.1.Final | 2.15.3 | — | 6.1.1 | 2023-11-23 |
| 3.2.1 | 6.4.1.Final | 2.15.3 | — | 6.1.2 | 2023-12-21 |
| 3.2.2 | 6.4.1.Final | 2.15.3 | — | 6.1.3 | 2024-01-19 |
| 3.2.3 | 6.4.4.Final | 2.15.4 | — | 6.1.4 | 2024-02-22 |
| 3.2.4 | 6.4.4.Final | 2.15.4 | — | 6.1.5 | 2024-03-21 |
| 3.2.5 | 6.4.4.Final | 2.15.4 | — | 6.1.6 | 2024-04-18 |
| 3.2.6 | 6.4.8.Final | 2.15.4 | — | 6.1.8 | 2024-05-23 |
| 3.2.7 | 6.4.9.Final | 2.15.4 | — | 6.1.10 | 2024-06-20 |
| 3.2.8 | 6.4.9.Final | 2.15.4 | — | 6.1.11 | 2024-07-18 |
| 3.2.9 | 6.4.10.Final | 2.15.4 | — | 6.1.12 | 2024-08-22 |
| 3.2.10 | 6.4.10.Final | 2.15.4 | — | 6.1.13 | 2024-09-19 |
| 3.2.11 | 6.4.10.Final | 2.15.4 | — | 6.1.14 | 2024-10-24 |
| 3.2.12 | 6.4.10.Final | 2.15.4 | — | 6.1.15 | 2024-11-21 |
| 3.3.0 | 6.5.2.Final | 2.17.1 | — | 6.1.8 | 2024-05-23 |
| 3.3.1 | 6.5.2.Final | 2.17.1 | — | 6.1.10 | 2024-06-20 |
| 3.3.2 | 6.5.2.Final | 2.17.2 | — | 6.1.11 | 2024-07-18 |
| 3.3.3 | 6.5.2.Final | 2.17.2 | — | 6.1.12 | 2024-08-22 |
| 3.3.4 | 6.5.3.Final | 2.17.2 | — | 6.1.13 | 2024-09-19 |
| 3.3.5 | 6.5.3.Final | 2.17.2 | — | 6.1.14 | 2024-10-24 |
| 3.3.6 | 6.5.3.Final | 2.17.3 | — | 6.1.15 | 2024-11-21 |
| 3.3.7 | 6.5.3.Final | 2.17.3 | — | 6.1.16 | 2024-12-19 |
| 3.3.8 | 6.5.3.Final | 2.17.3 | — | 6.1.16 | 2025-01-23 |
| 3.3.9 | 6.5.3.Final | 2.17.3 | — | 6.1.17 | 2025-02-20 |
| 3.3.10 | 6.5.3.Final | 2.17.3 | — | 6.1.18 | 2025-03-20 |
| 3.3.11 | 6.5.3.Final | 2.17.3 | — | 6.1.19 | 2025-04-24 |
| 3.3.12 | 6.5.3.Final | 2.17.3 | — | 6.1.20 | 2025-05-22 |
| 3.3.13 | 6.5.3.Final | 2.17.3 | — | 6.1.21 | 2025-06-19 |
| 3.4.0 | 6.6.2.Final | 2.18.1 | — | 6.2.0 | 2024-11-21 |
| 3.4.1 | 6.6.4.Final | 2.18.2 | — | 6.2.1 | 2024-12-19 |
| 3.4.2 | 6.6.5.Final | 2.18.2 | — | 6.2.2 | 2025-01-23 |
| 3.4.3 | 6.6.8.Final | 2.18.2 | — | 6.2.3 | 2025-02-20 |
| 3.4.4 | 6.6.11.Final | 2.18.3 | — | 6.2.5 | 2025-03-20 |
| 3.4.5 | 6.6.13.Final | 2.18.3 | — | 6.2.6 | 2025-04-24 |
| 3.4.6 | 6.6.15.Final | 2.18.4 | — | 6.2.7 | 2025-05-22 |
| 3.4.7 | 6.6.18.Final | 2.18.4.1 | — | 6.2.8 | 2025-06-19 |
| 3.4.8 | 6.6.22.Final | 2.18.4.1 | — | 6.2.9 | 2025-07-24 |
| 3.4.9 | 6.6.26.Final | 2.18.4.1 | — | 6.2.10 | 2025-08-21 |
| 3.4.10 | 6.6.29.Final | 2.18.4.1 | — | 6.2.11 | 2025-09-18 |
| 3.4.11 | 6.6.33.Final | 2.18.4.1 | — | 6.2.12 | 2025-10-23 |
| 3.4.12 | 6.6.36.Final | 2.18.5 | — | 6.2.14 | 2025-11-20 |
| 3.4.13 | 6.6.39.Final | 2.18.5 | — | 6.2.15 | 2025-12-18 |
| 3.5.0 | 6.6.15.Final | 2.19.0 | — | 6.2.7 | 2025-05-22 |
| 3.5.1 | 6.6.18.Final | 2.19.1 | — | 6.2.8 | 2025-06-19 |
| 3.5.2 | 6.6.18.Final | 2.19.1 | — | 6.2.8 | 2025-06-19 |
| 3.5.3 | 6.6.18.Final | 2.19.1 | — | 6.2.8 | 2025-06-19 |
| 3.5.4 | 6.6.22.Final | 2.19.2 | — | 6.2.9 | 2025-07-24 |
| 3.5.5 | 6.6.26.Final | 2.19.2 | — | 6.2.10 | 2025-08-21 |
| 3.5.6 | 6.6.29.Final | 2.19.2 | — | 6.2.11 | 2025-09-18 |
| 3.5.7 | 6.6.33.Final | 2.19.2 | — | 6.2.12 | 2025-10-23 |
| 3.5.8 | 6.6.36.Final | 2.19.4 | — | 6.2.14 | 2025-11-20 |
| 3.5.9 | 6.6.39.Final | 2.19.4 | — | 6.2.15 | 2025-12-18 |
| 3.5.10 | 6.6.41.Final | 2.19.4 | — | 6.2.15 | 2026-01-22 |
| 3.5.11 | 6.6.42.Final | 2.19.4 | — | 6.2.16 | 2026-02-19 |
| 3.5.12 | 6.6.44.Final | 2.19.4 | — | 6.2.17 | 2026-03-19 |
| 3.5.13 | 6.6.45.Final | 2.21.2 | — | 6.2.17 | 2026-03-26 |
| 3.5.14 | 6.6.49.Final | 2.21.2 | — | 6.2.18 | 2026-04-23 |
| 3.5.15 | 6.6.53.Final | 2.21.4 | — | 6.2.19 | 2026-06-10 |
| 3.5.16 | 6.6.53.Final | 2.21.4 | — | 6.2.19 | 2026-06-25 |
| 4.0.0 | 7.1.8.Final | 3.0.2 | 2.20.1 | 7.0.1 | 2025-11-20 |
| 4.0.1 | 7.2.0.Final | 3.0.3 | 2.20.1 | 7.0.2 | 2025-12-18 |
| 4.0.2 | 7.2.1.Final | 3.0.4 | 2.20.2 | 7.0.3 | 2026-01-22 |
| 4.0.3 | 7.2.4.Final | 3.0.4 | 2.20.2 | 7.0.5 | 2026-02-19 |
| 4.0.4 | 7.2.7.Final | 3.1.0 | 2.21.1 | 7.0.6 | 2026-03-19 |
| 4.0.5 | 7.2.7.Final | 3.1.0 | 2.21.2 | 7.0.6 | 2026-03-26 |
| 4.0.6 | 7.2.12.Final | 3.1.2 | 2.21.2 | 7.0.7 | 2026-04-23 |
| 4.0.7 | 7.2.19.Final | 3.1.4 | 2.21.4 | 7.0.8 | 2026-06-10 |
| 4.1.0 | 7.4.1.Final | 3.1.4 | 2.21.4 | 7.0.8 | 2026-06-10 |
