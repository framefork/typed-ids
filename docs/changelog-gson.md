# Changelog research: Gson

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-07-05.
> Sources read:
> - `github.com/google/gson` git tags `gson-parent-2.10.1`…`gson-parent-2.14.0` — full tag list enumerated with `git tag`; dates verified with `git log -1 --format=%ci <tag>`. Confirmed latest is `gson-parent-2.14.0` (2026-04-23); there is no 3.x.
> - `github.com/google/gson` `CHANGELOG.md` (stops at 2.10, defers newer versions to GitHub Releases) plus the GitHub Release notes for `2.11.0`, `2.12.0`, `2.12.1`, `2.13.0`, `2.13.1`, `2.13.2`, `2.14.0`.
> - `github.com/google/gson` `pom.xml` at tags `2.10.1`/`2.11.0`/`2.12.0`/`2.14.0` (`git show <tag>:pom.xml`) for the Java baseline, and `gson/src/main/java/module-info.java` + `JsonWriter.java` at `2.14.0` for the JPMS descriptor and the writer methods we call.
> - **Verified there is NO `ServiceLoader` usage anywhere in Gson's source** (`grep -rn ServiceLoader` over the whole `google/gson` tree at `2.14.0` → zero hits). This is the load-bearing finding below.
> - typed-ids source: `gradle/libs.versions.toml`, the four Gson classes under `modules/typed-ids/src/main/java/org/framefork/typedIds/{uuid,bigint}/json/gson/`, their tests, and the `README.md` Gson section.

## TL;DR for typed-ids

- **We pin `com.google.code.gson:gson:2.10.1`** (catalog alias `gson`, a plain version literal — not a `[versions]` ref), released 2023-01-06. The latest is **`2.14.0`** (2026-04-23). The gap is entirely a **stable `2.x` maintenance line — no major bump, no `3.x`, no artifact/coordinate/package rename.** Everything Gson-facing our four classes touch is byte-for-byte stable across the whole range.
- **Headline finding — the `@AutoService(TypeAdapterFactory.class)` file is inert as far as Gson is concerned.** Gson has **no ServiceLoader auto-discovery** for `TypeAdapterFactory` (or for anything). Unlike Jackson (`ObjectMapper.findAndRegisterModules()` / `findModulesViaServiceLoader`), Gson has **no equivalent hook** — `GsonBuilder` reads no `META-INF/services` file. The generated `META-INF/services/com.google.gson.TypeAdapterFactory` is only useful if the *consumer* writes their own `ServiceLoader.load(...)` loop and feeds each factory to `GsonBuilder.registerTypeAdapterFactory(...)`. Gson never does it for them. **This makes the README's "registered automatically via the standard `java.util.ServiceLoader` mechanism" claim misleading for the Gson axis** (see below).
- **Our Gson API surface is ancient-stable.** `TypeAdapterFactory.create(Gson, TypeToken<T>)`, `TypeAdapter.read/write`, `TypeToken.getRawType()`, `JsonReader.nextString()/nextLong()`, `JsonWriter.value(String)/jsonValue(String)` have existed unchanged since well before 2.10 and are present, unmodified, at `2.14.0`. No deprecation, rename, or removal in-range touches anything we call.
- **Java baseline is a non-issue for us.** Gson `2.10.1`/`2.11.0` compile to Java 7; `2.12.0`+ raise it to Java 8. typed-ids builds on **Java 17**, so any Gson in range is fine. (`2.11.0` also raised Android's floor to API 21 — irrelevant server-side.)
- **Floor/target for the extracted `typed-ids-gson` module: floor can stay low (`2.10.1`, or bump to `2.11.0`); compile/test target `2.14.0`.** Because the SPI we use is invariant across all of `2.x`, the floor choice is cosmetic — it only decides which version we compile/test against, not source compatibility. Gson stays `compileOnly` (POM-`optional`), so consumers bring their own version regardless. Gson is a proper JPMS module (`com.google.gson`) exporting every package we import, so the extraction adds no module-path friction.

## How typed-ids uses Gson

All Gson usage lives in the core `typed-ids` module today (`compileOnly(libs.gson)`), in four classes — a factory + adapter pair for each id kind, in their existing packages `org.framefork.typedIds.uuid.json.gson` and `org.framefork.typedIds.bigint.json.gson`:

### The two `TypeAdapterFactory` implementations

`ObjectUuidTypeAdapterFactory` / `ObjectBigIntIdTypeAdapterFactory` each `implements com.google.gson.TypeAdapterFactory`, annotated `@AutoService(TypeAdapterFactory.class)`. They override the single SPI method:

```java
<T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
```

They call `typeToken.getRawType()`, check assignability against our id base class, and either return a fresh adapter or `null` (Gson's "not my type, try the next factory" contract). `gson` is received but unused.

### The two `TypeAdapter` implementations

`ObjectUuidTypeAdapter` / `ObjectBigIntIdTypeAdapter` each `extends com.google.gson.TypeAdapter<T>` and override `write(JsonWriter, T)` + `read(JsonReader)`:

- UUID: `writer.value(uuid.toString())` / `UUID.fromString(in.nextString())`.
- BigInt: `writer.jsonValue(String.valueOf(value.toLong()))` (emits a bare number, not a quoted string) / `in.nextLong()`.

### The registration mechanics — stated definitively

**Gson does not auto-discover `TypeAdapterFactory` service files.** Confirmed by grepping the entire `google/gson` source at `2.14.0`: there is **zero `ServiceLoader` usage**. `GsonBuilder`/`Gson` construct their factory list programmatically and from `registerTypeAdapterFactory(...)` calls only. Consequences:

- The `@AutoService(TypeAdapterFactory.class)` processor emits `META-INF/services/com.google.gson.TypeAdapterFactory` into our jar, but **Gson never reads it.** It is not wired into any Gson bootstrap. It is only consumable if the *application* does `ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory)` by hand — Gson offers no built-in equivalent to Jackson's `findAndRegisterModules()`.
- Our **own tests register manually** — `new GsonBuilder().registerTypeAdapterFactory(new ObjectUuidTypeAdapterFactory()).create()` — i.e. they do not exercise any auto-discovery path, because there is none.
- **README mismatch to flag:** the README's Gson section says the factories "can be registered automatically via the standard `java.util.ServiceLoader` mechanism, or explicitly." For Gson, "automatically" is only true in the sense that the service file *exists* for a consumer-written `ServiceLoader` loop — **Gson itself has no mechanism that picks them up automatically.** This is unlike the Jackson and Swagger axes in this repo, where the host framework genuinely `ServiceLoader`-loads the registered SPI. Worth tightening the README wording (and/or documenting the exact `ServiceLoader.load(...).forEach(...)` snippet a consumer must write) during the module extraction.

### Exact upstream API surface we consume

| Symbol | Package | How we use it |
|---|---|---|
| `TypeAdapterFactory` | `com.google.gson` | **implemented** SPI; `create(...)` override + `@AutoService` |
| `TypeAdapter<T>` | `com.google.gson` | **extended**; `read` / `write` overrides |
| `Gson` | `com.google.gson` | `create(...)` parameter (unused) |
| `TypeToken<T>` | `com.google.gson.reflect` | `create(...)` parameter; `.getRawType()` |
| `JsonReader` | `com.google.gson.stream` | `.nextString()`, `.nextLong()` |
| `JsonWriter` | `com.google.gson.stream` | `.value(String)`, `.jsonValue(String)` |

## Version timeline (newer-than-ours)

One `2.x` line, flat patch cadence. Dates verified against git tags.

| gson | Released | Java baseline | Relevance to us |
|---|---|---|---|
| **2.10.1** *(our pin)* | 2023-01-06 | 7 | baseline |
| 2.11.0 | 2024-05-19 | 7 | ProGuard/R8 rules, `Strictness` API, `FormattingStyle`, `TypeToken` no longer captures type variables by default, adds optional `error_prone_annotations` dep, Android floor → API 21 |
| 2.12.0 | 2025-01-27 | **8** (drops Java 7) | `@CheckReturnValue` on packages, `JsonReader` nesting limit, `NullSafeTypeAdapter` |
| 2.12.1 | 2025-01-30 | 8 | OSGi-only: marks `error_prone_annotations` optional. No code change |
| 2.13.0 | 2025-04-11 | 8 | collection-deserialization bugfix; internal `$Gson$Types`/`$Gson$Preconditions` renamed (internal only) |
| 2.13.1 | 2025-04-23 | 8 | `FieldNamingStrategy` multi-name support; dependency bumps |
| 2.13.2 | 2025-09-10 | 8 | **JPMS packaging fix** (Eclipse/VS Code could not resolve module `com.google.gson`); dependency bumps |
| **2.14.0** *(latest)* | 2026-04-23 | 8 | `java.time` adapters (no more `--add-opens`), removed unreleased `com.google.gson.graph`, stricter ASCII-only integer parsing, duplicate-key-with-null fix, internal `Type` impls no longer `Serializable` |

### What changed on our integration points across 2.10.1 → 2.14.0

**Nothing.** Every symbol in the surface table is present and unmodified at `2.14.0`. Verified structurally: `JsonWriter.value(String)`, `value(long)`, and `jsonValue(String)` all exist at `2.14.0`; `TypeAdapter`/`TypeAdapterFactory`/`TypeToken`/`JsonReader` are untouched by any in-range release note. Triaging the in-range changes that even sit *near* our code:

- **2.11.0 `TypeToken` no longer captures type variables by default** — does not touch us: we never subclass `TypeToken` to capture a type; we only *receive* a `TypeToken<T>` from Gson and call `getRawType()`.
- **2.13.0 internal-class renames (`$Gson$Types` → `GsonTypes`)** — Gson internals, not on our import list.
- **2.14.0 stricter integer parsing / duplicate-key fix / `java.time` adapters** — behavioural changes to Gson's *own* built-in adapters and number parsing. Our `nextLong()`/`nextString()` calls and our custom adapters are unaffected (we own the read/write for our id types).
- **2.11.0 optional `error_prone_annotations` transitive dep** — harmless; typed-ids already declares `error_prone_annotations` on its own.

## Breaking-change deep dive

**There is no major boundary to deep-dive.** Gson has been on `2.x` for its entire modern history and shows no sign of a `3.x`. The coordinate (`com.google.code.gson:gson`), package roots (`com.google.gson`, `.reflect`, `.stream`), and the `TypeAdapter`/`TypeAdapterFactory` SPI are constant from `2.10.1` through `2.14.0` and long before. The only moving parts in range are the Java baseline (7 → 8, below our 17) and internal/behavioural fixes that do not reach our surface. Gson's famed stability holds up under inspection here.

### JPMS / module extraction notes

Gson ships a real module descriptor — `module com.google.gson` — exporting `com.google.gson`, `com.google.gson.annotations`, `com.google.gson.reflect`, and `com.google.gson.stream`, i.e. **every package we import.** `2.13.2` fixed a jar-packaging bug that had prevented some IDEs (Eclipse, VS Code) from resolving the `com.google.gson` module name. If the extracted `typed-ids-gson` module ever declares its own `module-info`, targeting Gson `2.13.2+` avoids that historical rough edge — but since typed-ids does not currently ship module descriptors, this is a forward note, not a requirement.

## Compatibility-strategy implications

Surfacing for the `typed-ids-gson` extraction (`docs/restructuring-proposal.md`), not deciding:

- **No `-2x`/`-3x` split is forced on the Gson axis.** There is one stable SPI; the same compiled bytecode loads against any `2.10.1`…`2.14.0` (and older). A second source set would only be warranted if Gson ever shipped a Jackson-style major rename — which has not happened and is not staged.
- **Floor can stay low; test/compile target should be current.** Recommend **compile/test against `2.14.0`** (latest) and declaring the floor at **`2.10.1` (keep) or `2.11.0`** — the API we touch predates all of these, so the floor is about what we build against, not about source compatibility. Gson stays `compileOnly` → POM-`optional`, so a consumer's own Gson version wins at runtime regardless of our declared value; the Boot BOM / app stays in charge.
- **Fix the registration story as part of extraction.** The `@AutoService(TypeAdapterFactory.class)` file is not auto-loaded by Gson. Either (a) keep it and document the exact consumer-side `ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory)` snippet, correcting the README's "automatically via ServiceLoader" wording; or (b) drop the `@AutoService` annotation as inert and tell consumers to `registerTypeAdapterFactory(...)` explicitly (which is what our own tests already do). Either way, the current README overstates what Gson does on its own.

### Open questions / decisions to make later (not decided here)

1. **README correction + registration approach.** Decide whether to keep the (consumer-driven-only) `@AutoService` service file or drop it, and align the README wording either way. This is the one genuine finding on the Gson axis; everything else is confirmed-stable.
2. **Floor value for `typed-ids-gson`.** `2.10.1` (keep) vs `2.11.0` vs current — all compile our code identically. Pick based on how modern a floor the project wants to advertise; nothing technical forces the choice.
