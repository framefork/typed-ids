# TypedIds

This library provides base classes and tooling to create typed IDs.
The original goal was to allow easy and safe creation of Value Objects (VOs) backed by UUIDv7 for Hibernate ORM entities.
Contributions for other ORMs or alternative JPA implementations are welcome.

## Why?

[Blog: Specialized Value Objects for entity identifiers](https://filip-prochazka.com/blog/specialized-value-objects-for-entity-identifiers)

## Installation

For seamless type support in Hibernate ORM, you should pick one of the following variants:

| Hibernate Version             | Artefact                                                                                                             |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------|
| 6.6, 6.5, 6.4, and 6.3        | [org.framefork:typed-ids-hibernate-63](https://central.sonatype.com/artifact/org.framefork/typed-ids-hibernate-63)   |
| 6.2                           | TBD                                                                                                                  |
| 6.1 and 6.0                   | TBD                                                                                                                  |
| 5.6 and 5.5                   | TBD                                                                                                                  |

Find the latest version in this project's GitHub releases or on Maven Central.

If you want just the plain classes, you can install only the [org.framefork:typed-ids](https://central.sonatype.com/artifact/org.framefork/typed-ids).

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

        public static Id fromString(final String value)
        {
            return ObjectUuid.fromString(Id::new, value);
        }

        public static Id fromUuid(final UUID value)
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

The `ObjectUuid.randomUUID()` generates [UUIDv7](https://www.toomanyafterthoughts.com/uuids-are-bad-for-database-index-performance-uuid7/#uuid-7-time-ordered)
using [com.fasterxml.uuid:java-uuid-generator](https://github.com/cowtowncoder/java-uuid-generator). If desired, this library could be improved to allow configuring the used generator,
but given the goal of having typed PK IDs for tables/entities I doubt the need for anything other than UUIDv7.

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
