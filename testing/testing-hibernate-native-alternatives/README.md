# Native Hibernate Alternatives to `framefork/typed-ids`

This module provides concrete implementations and integration tests demonstrating native JPA/Hibernate approaches for strongly-typed identifiers, specifically focusing on the limitations of using `@EmbeddedId` and `@IdClass` with database-generated keys.

## Overview

This module demonstrates the **actual runtime behavior** of native alternatives when attempting to use database-generated identifiers, providing empirical evidence of their limitations compared to the `framefork/typed-ids` library.

## What `framefork/typed-ids` Library Solves

The `framefork/typed-ids` library provides a comprehensive solution for strongly-typed identifiers that addresses the following challenges:

### Core Features
- **Compile-time Type Safety** - Prevents accidental ID misuse (e.g., passing `OrderId` where `UserId` expected)
- **Minimal Boilerplate** - Simple inheritance from base classes with automatic registration
- **Application-side ID Generation** - Built-in UUIDv7 and TSID generation for optimal database performance
- **Database-side ID Generation** - Full `@GeneratedValue` support (IDENTITY, SEQUENCE, AUTO) for `ObjectBigIntId`
- **Transparent Querying** - Direct JPQL/HQL usage: `WHERE entity.id = :id` (no property dereferencing)
- **Clean Serialization** - Automatic JSON serialization to primitive values via auto-discoverable Jackson modules
- **Proper OpenAPI Schemas** - Generates primitive type schemas (string/integer) in API documentation
- **Database Optimization** - Intelligent column type selection per database dialect (e.g., native UUID on PostgreSQL)
- **Ecosystem Integration** - Out-of-box support for Jackson, Gson, Kotlinx Serialization, SpringDoc

### Advanced Capabilities
- **Automatic Type Registration** - Compile-time indexing eliminates manual `@Type` annotations
- **Custom ID Generators** - Seamless integration with Hibernate's internal generator system
- **Multiple Hibernate Versions** - Dedicated modules for different Hibernate versions
- **Zero Configuration** - ServiceLoader-based auto-discovery for all integrations

## Alternative Approaches Analysis

### 1. @EmbeddedId with Java Records (Not viable with Hibernate 7.0+)

**What it solves:**
- ✅ Compile-time Type Safety
- ✅ Minimal Boilerplate (record syntax)
- ✅ JPA Standard Compliance
- ✅ Transparent Querying (supports both `entity.id = :idObject` and `entity.id.value = :primitiveValue`)

**What it doesn't solve:**
- ❌ Database-side ID Generation (`@GeneratedValue` fails with composite ID error)
- ❌ Clean Serialization (produces nested JSON objects by default)
- ❌ Proper OpenAPI Schemas (generates complex object schemas)
- ❌ Ecosystem Integration (manual serializers required)
- ❌ Application-side ID Generation (no built-in generators)
- ❌ Database Optimization (no dialect-specific column types)
- ❌ Automatic Type Registration (N/A)

**Test Results in this module:**
- ✅ Schema Generation: Creates `auto_increment` column
- ❌ Runtime: `IdentifierGenerationException: Identity generation isn't supported for composite ids`
- ✅ JPQL Querying: Both `WHERE e.id = :embeddableObject` and `WHERE e.id.value = :primitiveValue` work
- ✅ SELECT NEW Constructor: Supports both direct embedded object mapping and inline constructor calls

### 2. @IdClass with Java Records (Not viable with Hibernate 7.0+)

**What it solves:**
- ✅ Compile-time Type Safety
- ✅ JPA Standard Compliance
- ✅ Transparent Querying (direct field access in JPQL)

**What it doesn't solve:**
- ❌ Database-side ID Generation (same composite ID limitation)
- ❌ Minimal Boilerplate (requires both record and entity field mapping)
- ❌ Clean Serialization (entity structure depends on implementation)
- ❌ Proper OpenAPI Schemas (depends on entity serialization)
- ❌ Ecosystem Integration (manual configuration required)
- ❌ Application-side ID Generation (no built-in generators)
- ❌ Database Optimization (no dialect-specific optimizations)
- ❌ Automatic Type Registration (N/A)

**Test Results in this module:**
- ✅ Schema Generation: Creates `auto_increment` column
- ❌ Runtime: `IdentifierGenerationException: Identity generation isn't supported for composite ids`
- ✅ JPQL Querying: Direct field access works (`WHERE e.value = :primitiveValue`) but object comparison fails
- ✅ SELECT NEW Constructor: Supports primitive value mapping and inline IdClass object construction, but cannot auto-convert primitives to IdClass objects

### 3. JPA AttributeConverter (Not Implemented)

**What it solves:**
- ✅ Compile-time Type Safety
- ✅ Transparent Querying
- ✅ Clean Serialization (primitive field exposure)

**What it doesn't solve:**
- ❌ JPA Standard Compliance (explicitly forbidden for `@Id` fields)
- ❌ Database-side ID Generation (workarounds required)
- ❌ Minimal Boilerplate (requires converter per ID type)
- ❌ Proper OpenAPI Schemas (depends on workaround implementation)
- ❌ Ecosystem Integration (manual configuration required)
- ❌ Application-side ID Generation (no built-in generators)
- ❌ Database Optimization (no dialect-specific optimizations)
- ❌ Automatic Type Registration (N/A)

**Why not implemented:** JPA specification explicitly prohibits `@Convert` on `@Id` fields. While some implementations like modern Hibernate may not reject this at startup, the behavior is undefined and non-portable across JPA providers.

## Empirical Test Results

### Common Failure Pattern
Both `@EmbeddedId` and `@IdClass` approaches exhibit the same failure pattern:

1. **Schema Generation Succeeds** - Hibernate creates proper `auto_increment` columns
2. **Entity Compilation Succeeds** - No compile-time errors or warnings
3. **Runtime Failure** - `IdentifierGenerationException` during `persist()` and `flush()`

### Root Cause
Hibernate treats both approaches as **composite identifiers** even when using single fields:
- `@EmbeddedId`: Embedded object is inherently composite from Hibernate's perspective
- `@IdClass`: Any use of `@IdClass` signals composite identity to Hibernate

The error message is identical: `"Identity generation isn't supported for composite ids"`

### Hibernate Version Regression
**Note:** These native approaches that previously worked with Hibernate 6.6.x now fail consistently with Hibernate 7.0+ due to stricter composite identifier validation.
This represents a regression in functionality where previously working code now fails at runtime,
further emphasizing the value of the `framefork/typed-ids` library which maintains consistent behavior across Hibernate versions.

## Conclusion

This empirical analysis demonstrates that **native JPA/Hibernate approaches fail** when combining strongly-typed identifiers with database-generated keys.
While schema generation succeeds, runtime failures occur due to Hibernate's treatment of these patterns as composite identifiers.

**Key Findings:**
1. Native approaches work only with **application-generated identifiers**
2. Database-generated keys require significant compromises in other areas
3. No native approach provides the comprehensive feature set of `framefork/typed-ids`
4. Integration testing reveals gaps between theoretical capabilities and practical limitations

**Recommendation**: For production applications requiring both type safety and database-generated keys, `framefork/typed-ids` library provides the only viable comprehensive solution.
