# Changelog research: Spring Framework

> Research doc for the typed-ids newer-versions compatibility effort. Compiled 2026-06-28.
> Sources read:
> - `github.com/spring-projects/spring-framework` git tags — release-date verification via `git log -1 --format=%ci <tag>` for `v6.2.14`/`v6.2.19`, `v7.0.0-M1`/`v7.0.0-RC1`/`v7.0.0`/`v7.0.1`/`v7.0.8`, plus the full `v6.2.x` and `v7.0.x` tag list (latest GA is **v7.0.8**; there is **no `v7.1.x` GA tag** yet — 7.1 is milestone/preview only).
> - `github.com/spring-projects/spring-framework` wiki — `Spring-Framework-7.0-Release-Notes`, `Spring-Framework-7.1-Release-Notes` (preview page, "scheduled for November 2026").
> - `github.com/spring-projects/spring-framework` source diff — `org.springframework.context.annotation.Bean` declaration at `v6.2.14` vs `v7.0.8` (the one Spring Framework symbol our shipped code touches) — **byte-identical** across the major boundary.
> - typed-ids source: `gradle/libs.versions.toml`, `buildSrc/src/main/kotlin/framefork.java.gradle.kts`, `modules/typed-ids-openapi-springdoc/`, and a repo-wide grep for `org.springframework.*` imports.
> - Cross-reference: the sibling `docs/changelog-spring-boot.md` spine table (Boot 3.5 → SF 6.2.x, Boot 4.0 → SF 7.0.x).

## TL;DR for typed-ids

- **Spring Framework is overwhelmingly a transitive dependency**, not a declared one. There is **no `org.springframework:spring-*` (non-Boot) coordinate anywhere** in `gradle/libs.versions.toml` or any module's `build.gradle.kts`. It arrives on the classpath only because the Spring Boot BOM (in the `testing/` modules) and `springdoc-openapi-starter-common` (in `typed-ids-openapi-springdoc`) pull it in.
- **There is exactly one direct Spring Framework symbol in shipped code:** `org.springframework.context.annotation.@Bean`, used once in `typed-ids-openapi-springdoc`'s `TypedIdsOpenApiAutoConfiguration`. That annotation is **identical at `v6.2.14` and `v7.0.8`** (source-verified), so it spans the 6.2 → 7.0 major with zero friction. Everything else (`@AutoConfiguration`, `@EnableConfigurationProperties`) is *Spring Boot*, covered by `docs/changelog-spring-boot.md`.
- **No `Converter` / `ConverterFactory` / `GenericConverter` / `Formatter` / `ConversionService` integration exists.** typed-ids does **not** register Spring-side String↔id converters for MVC path/param binding or for Spring Data. So the Spring `core.convert` / `format` SPI — the surface most id libraries would couple to, and the one that *could* break across a major — is simply **not part of our integration**. (id↔DB conversion is done via Hibernate `UserType`/`JavaType`; id↔JSON via Jackson; id↔OpenAPI via swagger's `ModelConverter`. None of these are Spring types.)
- **Spring Framework versions only matter as the version Spring Boot drags in:** Boot **3.5.8 → SF 6.2.14**, Boot **4.0.0 → SF 7.0.1**, Boot **4.0.7 → SF 7.0.8**. Our Boot test modules exercise typed-ids' Hibernate integration *under* these SF versions, but assert nothing about Spring Framework APIs directly.
- **Net assessment: low / no direct impact.** The 6.2 → 7.0 major (JSpecify null-safety, Jakarta EE 11, removed deprecations, new resilience/API-versioning features) does not touch any symbol we compile against. The honest conclusion is "matters only as the transitive stack Boot 4 selects" — and that coupling is already tracked in the Spring Boot, Hibernate, and Jackson docs.

## How typed-ids uses Spring Framework

### Direct usage: one annotation, in one shipped module

A repo-wide grep for `org.springframework.*` over `modules/` (shipped code) returns Spring **Framework** (non-Boot) in a single place:

| File | Import | Role |
|---|---|---|
| `modules/typed-ids-openapi-springdoc/.../config/TypedIdsOpenApiAutoConfiguration.java` | `org.springframework.context.annotation.Bean` | declares the `typedIdsModelConverter()` `@Bean` |

The surrounding annotations in that same class — `org.springframework.boot.autoconfigure.AutoConfiguration` and `org.springframework.boot.context.properties.EnableConfigurationProperties` — are **Spring Boot**, not Spring Framework, and belong to the Spring Boot doc. The module's `build.gradle.kts` declares **no** Spring coordinate at all; `spring-context` (which owns `@Bean`) and `spring-boot-autoconfigure` arrive transitively through `api(libs.springdoc.openapi.starter.common)` (`org.springdoc:springdoc-openapi-starter-common`, pinned `2.1.0` in the catalog).

`@Bean` is one of the most stable annotations in the framework. Source-diffing its declaration confirms it:

```
v6.2.14  public @interface Bean { String[] value() default {}; String[] name() default {}; … }
v7.0.8   public @interface Bean { String[] value() default {}; String[] name() default {}; … }   // identical
```

So the only Spring Framework symbol we ship against is invariant across the 6.2 → 7.0 boundary.

### Indirect usage: build tooling and test wiring (not shipped)

- **NullAway/Error Prone config** in `buildSrc/.../framefork.java.gradle.kts` references Spring annotations as *strings* (`org.springframework.beans.factory.InitializingBean.afterPropertiesSet` as a known-initializer; `@Autowired`/`@Value` as excluded-field annotations). These are static-analysis hints, compiled into no artifact, and only relevant where a test actually uses those annotations.
- **Test modules** consume real Spring Framework types — `@Autowired`, `@Bean`, `ResponseEntity`, `@GetMapping`, `@RestController`, `MockMvc`, `MockMvcRequestBuilders`/`ResultMatchers` — in `testing/testing-typed-ids-springdoc-2x-openapi` (a Boot 3.5 / springdoc 2.x web app) and indirectly in the Spring Data Boot modules. These are **test code**, exercising typed-ids *through* Spring, not integration points typed-ids exposes.

### What is NOT there (the important negative result)

The brief flagged the obvious candidate surfaces. Confirmed absent from the entire codebase:

| Candidate Spring SPI | Package | Present in typed-ids? |
|---|---|---|
| `Converter<S,T>` / `ConverterFactory` / `GenericConverter` | `org.springframework.core.convert.converter` | **No** |
| `ConversionService` / `FormattingConversionService` | `org.springframework.core.convert` / `format.support` | **No** |
| `Formatter` / `Printer` / `Parser` | `org.springframework.format` | **No** |
| `@Component` / `@Service` / `@Repository` (`stereotype`) | `org.springframework.stereotype` | **No** (only the one `@Bean` factory method) |
| Spring MVC argument resolvers / `WebMvcConfigurer` | `org.springframework.web.*` | **No** (only in the springdoc *test* app) |

This is the load-bearing finding: there is **no typed-ids Spring `Converter`/`Formatter` integration** that would let `ObjectUuid`/`ObjectBigIntId` bind from request params/path variables or be registered in a `ConversionService`. The id types reach the wire and the DB through *non-Spring* SPIs (Hibernate `JavaType`/`UserType`, Jackson `Module`, swagger `ModelConverter`). So the Spring `core.convert`/`format` API churn — the thing that would normally make a Spring major a real event for an id library — does not apply to us.

## Version timeline (newer-than-ours)

Dates are tag commit dates (`git log -1 --format=%ci`). "Ours" = whatever Boot pins: **6.2.14** under our Boot-3.5.8 pin, **7.0.1** under the Boot-4.0.0 test module.

### 6.2.x — the current 6.x maintenance line (6.2.14 → 6.2.19)

- **6.2.14** (2025-11-20) is what **Boot 3.5.8** (our `springBoot` pin) manages; **6.2.19** (2026-06-08) is the latest 6.2.x and is managed by Boot 3.5.16. Pure maintenance: bug fixes, dependency bumps, no Jakarta/Servlet/baseline movement, no API removals.
- **Impact on typed-ids: none.** This is the SF line our classic stack already runs on. `@Bean` and every transitively-used type are stable across the whole 6.2.x tail. Bumping the Boot 3.5 pin forward (which slides SF 6.2.14 → 6.2.19) is a no-op for our code.

### 7.0.0 — 2025-11-13 — **the major boundary** (GA; full deep dive below)

- First GA of the 7.x generation (milestones from `v7.0.0-M1` on 2025-01-23; `v7.0.0-RC1` 2025-10-16). **Boot 4.0.0 pins 7.0.1** (2025-11-20), so our `4x` test module already runs on 7.0.x.
- Headline themes: **JSpecify null-safety adoption**, **Jakarta EE 11 / Servlet 6.1** baseline, **JDK 17 retained** (JDK 25 recommended), removed long-deprecated APIs, deprecation of `RestTemplate` and Jackson-2 support, and new feature surfaces (resilience `@Retryable`/`RetryTemplate`, API versioning, `JmsClient`, `BeanRegistrar`, `RestTestClient`).
- **Impact on typed-ids: effectively none for our code.** None of the removed/changed APIs are ones we compile against — the removals are in web (Undertow, path-extension content negotiation, `ListenableFuture`, OkHttp3), `javax.annotation`/`javax.inject` support, `spring-jcl`, and `HttpHeaders`. We touch none of these. It matters **only** because it is the SF version Boot 4 brings, under which our Hibernate-7 modules are tested.

### 7.0.1 → 7.0.8 — the 7.0.x maintenance line (latest 7.0.8, 2026-06-08)

- Maintenance on 7.x. A couple of items are technically "breaking" but irrelevant to us: **7.0.8** introduces a default **SpEL max-operations limit (10,000)** and **trusted-packages** restriction for Jackson JMS converters; **7.0.7** added a `spring.test.extension.context.scope` toggle for the `@Nested` test-context change. We use no SpEL, no JMS, no custom `TestExecutionListener`.
- **Impact on typed-ids: none.** Boot 4.0.0 → SF 7.0.1; Boot 4.0.7 → SF 7.0.8 — so "support Boot 4.0.x" already means "tested under SF 7.0.1 through 7.0.8", and nothing in that range touches our surface.

### 7.1 — **upcoming, NOT released** (preview page says "scheduled for November 2026")

- There is **no `v7.1.x` tag in the repo** (latest GA is `v7.0.8`); 7.1 exists only as a preview/milestone line. Do not treat it as available.
- Planned baseline bumps from the 7.1 release-notes preview: **Jackson 3.1 (LTS)** as the floor, **Hibernate ORM 7.3 support** (with runtime compatibility for 7.1/7.2 retained). Planned: Jackson **2.x auto-detection disabled in 7.1, removed entirely in 7.2**; `RestTemplate` formally `@Deprecated` (removal slated for 8.0); new `MultipartHttpMessageConverter`.
- **Impact on typed-ids:** indirect and forward-looking. The Hibernate-7.3 floor lines up with the Hibernate doc's "extend `-72` range" question, and the Jackson-2 removal timeline reinforces the Jackson doc's push toward a Jackson-3 flavor — but **nothing here changes a Spring Framework symbol we compile against**. Relevant only via Boot 4.1+ eventually pinning a 7.1.x.

## Breaking-change deep dive: Spring Framework 6.2 → 7.0

Framed for a library that is *tested under* Spring but ships only one `@Bean`-annotated factory method. Each 7.0 theme is triaged as **affects us / irrelevant**. Drawn from `Spring-Framework-7.0-Release-Notes`.

### 1. JSpecify null-safety adoption — **mild positive, no action**

7.0 migrated the entire framework codebase from JSR-305 nullness annotations to **JSpecify** (`org.jspecify.annotations.*`), deprecating the old Spring `@Nullable`/`@NonNull` JSR-305 semantics. typed-ids **already standardizes on JSpecify 1.0.0** — declared `api("org.jspecify:jspecify:1.0.0")` in `buildSrc/.../framefork.java.gradle.kts`, with `@NullMarked` applied package-wide and `@Nullable`/`@NonNull` used throughout the Hibernate modules. So our null-safety vocabulary now **matches** the framework's, which is the convergence Spring intended; there is no annotation-version conflict (both on JSpecify 1.0.0) and nothing to migrate. This is purely favorable. (Kotlin consumers of *Spring's* APIs may see nullability refinements — irrelevant to typed-ids, which is Java.)

### 2. Jakarta EE 11 / Servlet 6.1 baseline — **realized through Hibernate/Boot, not us**

7.0 raises the baseline to **Jakarta EE 11**, Servlet 6.1, **JPA 3.2 (Hibernate ORM 7.1/7.2)**, Bean Validation 3.1. This is the same EE-11 bump described in the Spring Boot and Hibernate docs. typed-ids tracks Jakarta Persistence transitively *through Hibernate* (our `-70`/`-71`/`-72` modules), not directly through Spring — so the EE 11 floor reaches us via the Hibernate version Boot selects, already covered by `docs/changelog-hibernate.md`. No direct Spring action.

### 3. JDK 17 retained — **no floor change**

7.0 keeps a **JDK 17 minimum** (recommends JDK 25 LTS, "Java 25 friendly"). This matches Hibernate 7 and Jackson 3's own Java-17 floors, so the entire Boot-4 transitive stack is Java-17-clean. typed-ids' JDK floor is unaffected by the Spring major.

### 4. Removed / deprecated APIs — **none on our path**

The 7.0 removals (`#33809` and friends) are concentrated in areas typed-ids never imports: `spring-jcl`; `javax.annotation`/`javax.inject` annotation support; deprecated MVC path-mapping/content-negotiation options; Undertow WebSocket/HTTP support; `ListenableFuture`, OkHttp3, `Theme`, webjars-locator-core; the `HttpHeaders` `MultiValueMap` contract change. Deprecations (`RestTemplate`, `<mvc:*>` XML, `PathMatcher`, JUnit-4 TestContext, **Jackson 2.x support**) likewise miss us. The one cross-reference worth noting: **"Jackson 2.x support has been deprecated in favor of Jackson 3.x"** in Spring 7 is the framework-side echo of the Boot-4 Jackson-2→3 switch in `docs/changelog-jackson.md` — but it concerns Spring's *own* `MappingJackson2*` converters, which we don't use.

### 5. New feature surfaces — **irrelevant to us**

API versioning (MVC/WebFlux), resilience (`RetryTemplate`/`@Retryable`/`@ConcurrencyLimit`, `@EnableResilientMethods`), `JmsClient`, `BeanRegistrar` programmatic registration, `@Proxyable`, `RestTestClient`, GraalVM unified reachability-metadata format, Class-File API for JDK 24+. None intersect typed-ids' integration surface. Listed only to confirm the triage: these are the "headline" 7.0 features and **not one of them touches us**.

### 6. `@Bean` / `spring-context` core — **verified stable**

The single symbol we depend on, `org.springframework.context.annotation.Bean`, is **byte-identical** at `v6.2.14` and `v7.0.8` (source-diffed). The `@Configuration`/`@Bean` programming model the springdoc auto-config relies on is unchanged in 7.0 (7.0 *adds* `BeanRegistrar` and consistent CGLIB proxy defaulting, but does not alter the `@Bean` contract our factory method uses).

## Compatibility-strategy implications

What this means for the module structure and version matrix (surfacing, not deciding):

- **Spring Framework is not an axis we need to model independently.** Unlike Jackson and Hibernate — where typed-ids ships version-specific modules because it *extends* their SPIs — typed-ids extends **no** Spring Framework SPI. The SF version is a pure function of the Boot version (Boot 3.5 → SF 6.2, Boot 4.0 → SF 7.0), so it is already captured by the Boot axis in `docs/changelog-spring-boot.md`. There is no `typed-ids-spring-6` / `typed-ids-spring-7` split to consider, because there is nothing Spring-version-specific to compile.
- **The springdoc module is the only place a Spring change could ever reach us**, and only through its lone `@Bean` and (Boot-owned) `@AutoConfiguration`/`@EnableConfigurationProperties`. Whether that module needs a Boot-4 / springdoc-3.x counterpart is a **Spring Boot / springdoc** decision (see the Boot doc's open question on the missing Boot-4 springdoc axis), not a Spring Framework one — `@Bean` survives the major untouched.
- **JSpecify alignment is a quiet win.** Both typed-ids and Spring 7 now standardize on JSpecify 1.0.0; if a future integration *did* want to expose Spring-annotated null-safety, the vocabularies already match.
- **Preserving the old upgrade path is free on this axis.** Keeping the Boot-3.5 test module keeps SF 6.2.x coverage; adding Boot-4 coverage adds SF 7.0.x — both validated transitively, neither requiring Spring-version-specific typed-ids code.

### Open questions / decisions to make later (not decided here)

1. **Should typed-ids offer a Spring `Converter`/`Formatter` integration at all?** Today it deliberately doesn't — ids bind to MVC/Spring-Data only if the *consumer* registers a converter. If demand exists, *that* would introduce a genuine Spring Framework integration surface (and a real 6.2-vs-7.0 SPI question), but it's a net-new feature, not a compatibility fix. Flagging it only because the brief asked whether the surface exists: it does not, yet.
2. **Does the Boot-4 / springdoc-3.x gap (Boot doc, open question) drag in any Spring 7 specific code?** When that module is built, re-confirm its `@Bean`/auto-config wiring still compiles unchanged under SF 7.0.x (expected: yes — `@Bean` is invariant; the risk, if any, is springdoc-3.x and Boot-4 auto-config, not Spring Framework core).
3. **Track Spring Framework only as a *reported* version, not a managed one.** Since we never declare it, the compatibility matrix should show SF as a derived column of the Boot row (6.2.14 / 7.0.1 / 7.0.8 / future 7.1.x) for documentation completeness, without a corresponding typed-ids module.
</content>
</invoke>
