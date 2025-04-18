# TypedIds

This library provides base classes and tooling to easily and safely create immutable typed Value Object IDs (VO-IDs) internally backed by a UUIDv7 or bigint.

Contributions for serialization libraries, other ORMs, alternative JPA implementations or anything else are welcome.

## Why?

[Blog: Specialized Value Objects for entity identifiers](https://filip-prochazka.com/blog/specialized-value-objects-for-entity-identifiers)

## Installation

For seamless type support in Hibernate ORM, you should pick one of the following variants:

| Hibernate Version             | Artifact                                                                                                             |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------|
| 6.6, 6.5, 6.4, and 6.3        | [org.framefork:typed-ids-hibernate-63](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-63)   |
| 6.2                           | [org.framefork:typed-ids-hibernate-62](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-62)   |
| 6.1                           | [org.framefork:typed-ids-hibernate-61](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-61)   |

Find the latest version in this project's GitHub releases or on Maven Central.

If you want just the plain base classes without ORM support, you can install just the [org.framefork:typed-ids](https://central.sonatype.com/artifact/org.framefork/typed-ids).

## Hibernate type mapping

The library tries to make sure your data is stored with the types best fit for the job.
In some cases, that means changing the default DDL that Hibernate uses for the `SqlTypes.UUID` jdbc type.

| Database            | Database Type for UUID                         |
|---------------------|------------------------------------------------|
| PostgreSQL          | `uuid`                                         |
| MySQL               | `binary(16)`                                   |
| MariaDB 10.7+       | `uuid`                                         |
| MariaDB before 10.7 | `binary(16)`                                   |
| other               | whatever Hibernate uses as the dialect default |

The library only sets the type if there is no JDBC type for `SqlTypes.UUID` already set,
which means that if you want to use something different you should be able to do so using a custom `org.hibernate.boot.model.TypeContributor`.

## Database-generated identifiers and ORM Entities

The library explicitly supports `AUTO`, `IDENTITY` and `SEQUENCE` strategies for generating bigint identifiers via the `@GeneratedValue` annotation for the VO-IDs.

This pattern is supported only for `ObjectBigIntId`.

## Application-generated identifiers and ORM Entities

One of the primary goals of this library is to enable generated typed IDs _in application code_ - specifically in entity constructors.

Being able to generate identifiers in app code solves many problems around application design and architecture by getting rid of the dependency of entity on the database.
The classic approach is to let the database generate the identifiers, which is perfectly fine if you prefer that, but it breaks entity state because until you persist them they're invalid and incomplete.
But when you generate the ID at construction time, the entity is valid from the first moment.
The only way to do this reliably is to generate random identifiers so that you don't get conflicts when persisting the entities,
but using perfectly random values has its problems - see [UUID as a primary key](#uuid-as-a-primary-key) for a solution.

This pattern is supported for both `ObjectUuid` and `ObjectBigIntId`.

### Supported random identifier generation strategies

This library supports several libraries for generating the IDs in the JVM but does not pull them in, instead it expects you to pick one and add it yourself.

* UUIDs with [`com.fasterxml.uuid:java-uuid-generator`](https://central.sonatype.com/artifact/com.fasterxml.uuid/java-uuid-generator/versions) (see [project homepage](https://github.com/cowtowncoder/java-uuid-generator))
* BigInts/longs with [`io.hypersistence:hypersistence-tsid`](https://central.sonatype.com/artifact/io.hypersistence/hypersistence-tsid/versions) (see [project homepage](https://github.com/vladmihalcea/hypersistence-tsid))

If you want to use a different library, the `$Generators.setFactory()` extension point should hopefully be self-explanatory.

## UUID as a primary key

The `ObjectUuid.randomUUID()` generates [UUIDv7](https://www.toomanyafterthoughts.com/uuids-are-bad-for-database-index-performance-uuid7/#uuid-7-time-ordered) instead of Java's default UUIDv4.
The UUIDv4 is not well suited to be used in indexes and primary keys due to performance reasons.
The UUIDv7, while still larger than plain `long`, does not suffer from the performance problems that UUIDv4 has, and can be safely used for primary keys.

In case you don't like the default generator, you can opt to replace it using `ObjectUuid.Generators.setFactory(UuidGenerator.Factory)`.
It might come in handy in tests where you might want to use a deterministic generator.

Some additional resources:

* [Illustrating Primary Key models in InnoDB and their impact on disk usage - Percona Blog](https://www.percona.com/blog/illustrating-primary-key-models-in-innodb-and-their-impact-on-disk-usage/)
* [GUID/UUID Performance Breakthrough - Rick James](https://mysql.rjweb.org/doc.php/uuid)
* [MySQL InnoDB Primary Key Choice: GUID/UUID vs Integer Insert Performance - KCCoder](https://kccoder.com/mysql/uuid-vs-int-insert-performance/)
* [Unreasonable Defaults: Primary Key as Clustering Key - Use The Index, Luke](https://use-the-index-luke.com/blog/2014-01/unreasonable-defaults-primary-key-clustering-key)
* [SQL server full-text index and its stop words - Jiangong Sun](https://jiangong-sun.medium.com/sql-server-full-text-index-and-its-stop-words-492b0b589bff)
* [MySQL UUIDs – Bad For Performance - Percona Blog](https://www.percona.com/blog/uuids-are-popular-but-bad-for-performance-lets-discuss/)
* [Choose the right primary key to save a large amount of disk I/O - Too Many Afterthoughts](https://www.toomanyafterthoughts.com/primary-key-random-sequential-performance/)
* [UUID vs Bigint Battle! - Scaling Postgres 302](https://www.scalingpostgres.com/episodes/302-uuid-vs-bigint-battle/)

## Usage: ObjectUuid

This base type is designed to wrap a native UUID, and allows you to expose any utility functions you may need.
The following snippet is the standard boilerplate, but you may opt to skip some of the methods, or add a few custom ones.

```java
public record User(Id id)
{

    public static final class Id extends ObjectUuid<Id>
    {

        private Id(final UUID inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectUuid.randomUUID(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectUuid.fromString(Id::new, value);
        }

        public static Id from(final UUID value)
        {
            return ObjectUuid.fromUuid(Id::new, value);
        }

    }

}

// ...

var user = new User(Id.random());
```

With Kotlin, the standard boilerplate should look like the following snippet

```kt
data class User(id: Id) {

    class Id private constructor(id: UUID) : ObjectUuid<Id>(id) {
        companion object {
            fun random() = randomUUID(::Id)
            fun from(value: String) = fromString(::Id, value)
            fun from(value: UUID) = fromUuid(::Id, value)
        }
    }

}
```

## Usage: ObjectBigIntId

This base type is designed to wrap a native `long`, and allows you to expose any utility functions you may need.
The following snippet is the standard boilerplate, but you may opt to skip some of the methods, or add a few custom ones.

```java
public record User(Id id)
{

    public static final class Id extends ObjectBigIntId<Id>
    {

        private Id(final long inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectBigIntId.randomBigInt(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectBigIntId.fromString(Id::new, value);
        }

        public static Id from(final long value)
        {
            return ObjectBigIntId.fromLong(Id::new, value);
        }

    }

}

// ...

var user = new User(Id.random());
```

With Kotlin, the standard boilerplate should look like the following snippet

```kt
data class User(id: Id) {

    class Id private constructor(id: UUID) : ObjectBigIntId<Id>(id) {
        companion object {
            fun random() = randomBigInt(::Id)
            fun from(value: String) = fromString(::Id, value)
            fun from(value: Long) = fromLong(::Id, value)
        }
    }

}
```

## Typed IDs indexing at compile time

This library provides a mechanism to index your ID classes at compile time, which is useful for pleasant integrations with various frameworks.

### Setting up subtype indexing in a Java project

Set up the `org.framefork:typed-ids-index-java-classes-processor` as an annotation processor.

It's based on [org.atteo.classindex:classindex](https://github.com/atteo/classindex), and when executed,
it writes (somewhere in your build output directory) to `META-INF/services/org.framefork.typedIds.uuid.ObjectUuid` or `META-INF/services/org.framefork.typedIds.bigint.ObjectBigIntId`,
which can be later read by the standard `java.util.ServiceLoader` mechanism.

With Gradle, register the processor like this:

```kotlin
dependencies {
    annotationProcessor("org.framefork:typed-ids-index-java-classes-processor")
    testAnnotationProcessor("org.framefork:typed-ids-index-java-classes-processor")
}
```

With Maven, you can register it as an optional dependency if annotation processors discovery works in your project

```xml
<dependencies>
    <dependency>
        <groupId>org.framefork</groupId>
        <artifactId>typed-ids-index-java-classes-processor</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

Or if you want to be safe, you can register the processor explicitly

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.framefork</groupId>
                <artifactId>typed-ids-index-java-classes-processor</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Setting up subtype indexing in a Kotlin project

With Kotlin, you have to register the indexer as KAPT processor:

```kotlin
dependencies {
    kapt("org.framefork:typed-ids-index-java-classes-processor") // instead of annotationProcessor(...)
}
```

## Usage: Automatic type registration with Hibernate ORM

Without the Typed IDs indexing at compile time, the field has to be annotated like this (the `@Type(...)` is the important part):

```java
@Entity
public class User
{

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectUuidType.class)
    private Id id;

    // ...

    public static final class Id extends ObjectUuid<Id>
    {
        // ...
```

But with the index, the `ObjectUuidTypesContributor` reads it, and registers the types automatically when Hibernate ORM is initialized.

With the classes indexed, the system will know that the `User.Id` should be handled by `ObjectUuidType` and the `@Type(...)` can be dropped.
This also simplifies usage on every other place, where Hibernate might need to resolve a type for the `Id` instance, like queries.

## Usage: (de)serialization with Jackson

This library provides `ObjectBigIntIdJacksonModule` and `ObjectUuidJacksonModule`, which can be registered automatically via the standard `java.util.ServiceLoader` mechanism, or explicitly.

## Usage: (de)serialization with Kotlin Serialization

This library supports two mechanism for the standard Kotlin Serialization.

### Kotlin Serialization: Explicit

This mechanism requires explicit setup for each ID class

```kotlin
@Serializable(with = UserId.Serializer::class)
class UserId private constructor(id: UUID) : ObjectUuid<UserId>(id) {
    // standard boilerplate ...

    object Serializer : ObjectUuidSerializer<UserId>(::UserId)
}
```

but in return every time you use it, it just works

```kotlin
@Serializable
data class UserDto(val id: UserId)
```

### Kotlin Serialization: Contextual

With contextual, the standard ID boiler can be used, but you have to register the module

```kotlin
val json = Json {
    serializersModule = ObjectUuidKotlinxSerializationModule.fromIndex
}
```

and then mark the type as `@Contextual` on every usage

```kotlin
@Serializable
data class UserDto(@Contextual val id: UserId)
```

## More examples

To learn more you can explore the `testing/` directory of this library,
for example the [testing/testing-typed-ids-hibernate-66-indexed](https://github.com/framefork/typed-ids/tree/master/testing/testing-typed-ids-hibernate-66-indexed/src/test/java/org/framefork/typedIds)
has several working use-cases written in the form of tests that you can study for inspiration.
