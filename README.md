# TypedIds

This library provides base classes and tooling to create typed IDs.
The original goal was to allow easy and safe creation of Value Objects (VOs) backed by UUIDv7 for Hibernate ORM entities.
Contributions for other ORMs or alternative JPA implementations are welcome.

## Why?

[Blog: Specialized Value Objects for entity identifiers](https://filip-prochazka.com/blog/specialized-value-objects-for-entity-identifiers)

## Installation

For seamless type support in Hibernate ORM, you should pick one of the following variants:

| Hibernate Version             | Artifact                                                                                                             |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------|
| 6.6, 6.5, 6.4, and 6.3        | [org.framefork:typed-ids-hibernate-63](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-63)   |
| 6.2                           | [org.framefork:typed-ids-hibernate-62](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-62)   |
| 6.1 and 6.0                   | TBD                                                                                                                  |
| 5.6 and 5.5                   | TBD                                                                                                                  |

Find the latest version in this project's GitHub releases or on Maven Central.

If you want just the plain classes, you can install only the [org.framefork:typed-ids](https://central.sonatype.com/artifact/org.framefork/typed-ids).

### Application-generated (random) IDs

This library supports several libraries for generating the IDs in the JVM but does not pull them in, instead it expects you to pick one and add it yourself.

* UUIDs with [`com.fasterxml.uuid:java-uuid-generator`](https://central.sonatype.com/artifact/com.fasterxml.uuid/java-uuid-generator/versions)
* BigInts/longs with [`io.hypersistence:hypersistence-tsid`](https://central.sonatype.com/artifact/io.hypersistence/hypersistence-tsid/versions)

If you want to use a different library, the `$Generators.setFactory()` extension point should hopefully be self-explanatory.

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

## UUID as an application-generated identifier

One of the goals of this library is to enable generated typed IDs _in application code_ - specifically in entity constructors.
Being able to generate identifiers in app code solves many problems around application design and architecture by getting rid of the dependency of entity on the database.
The classic approach is to let the database generate the identifiers, which is perfectly fine if you prefer that, but it breaks entity state because until you persist them they're invalid and incomplete.
But when you generate the ID at construction time, the entity is valid from the first moment.

The only way to do this reliably is to generate random identifiers so that you don't get conflicts when persisting the entities, but using perfectly random values has its problems...

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

## Usage: ObjectUUID

The base type is designed to wrap a native UUID, and allows you to expose any utility functions you may need.
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

## Usage: Enabling automatic type registration with Hibernate ORM

Install the [newest version](https://central.sonatype.com/artifact/org.atteo.classindex/classindex) of [org.atteo.classindex:classindex](https://github.com/atteo/classindex), and register it as an annotation processor.
The classes of this library are annotated with `@IndexSubclasses`, so once the classindex library is correctly installed,
you should see `META-INF/services/org.framefork.typedIds.uuid.ObjectUuid` being populated somewhere in your build output directory.
The `ObjectUuidTypesContributor` should then read it, and register the types automatically when Hibernate ORM is initialized.

When used with a Kotlin project, you might want to explicitly add also a `kapt("org.atteo.classindex:classindex")` dependency.

Without the automatic registration, the field has to be annotated like this

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

But with the class indexer installed, The system will know that the `User.Id` should be handled by `ObjectUuidType` and the `@Type(...)` can be dropped.
This also simplifies usage on every other place, where Hibernate might need to resolve a type for the `Id` instance, like queries.
