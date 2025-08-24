# **Gemini Deep-Research: A Comparative Analysis of Strongly-Typed Identifier Strategies in Hibernate ORM**

## **Introduction: The Case for Strongly-Typed Entity Identifiers**

### **The Problem with Primitive IDs**

In modern domain-driven design, ensuring type safety is paramount to building robust, maintainable, and bug-resistant applications. While this principle is widely applied to business logic and domain objects, it is often overlooked at one of the most critical points of the data model: the entity identifier. The common practice of using primitive types such as long, java.lang.Long, or java.util.UUID as primary keys introduces a subtle but significant category of logical errors. A method signature like updateUser(long orderId, long userId) offers no compile-time protection against a developer accidentally transposing the arguments. Such an error, updateUser(userId, orderId), would compile without issue but result in catastrophic data corruption at runtime. This lack of type safety at the identifier level creates a persistent vulnerability throughout the application's lifecycle.
Strongly-typed identifiers address this problem by wrapping the primitive value in a dedicated, non-interchangeable type. For instance, UserId and OrderId become distinct classes, making an accidental swap a compile-time error. This elevates the identifier from a simple data value to a first-class concept within the domain model, enhancing clarity, reducing cognitive load for developers, and eliminating an entire class of potential bugs.

### **Introducing the Contenders**

The objective of this report is to conduct an exhaustive technical analysis of the primary strategies for implementing strongly-typed identifiers within a Java or Kotlin application using Hibernate ORM. This analysis will provide a clear, evidence-based guide for architects and senior engineers to make an informed decision on the most effective approach for their projects. The strategies under evaluation are:

1. **The Integrated Third-Party Solution:** The framefork/typed-ids library, a purpose-built framework designed specifically to solve this problem with deep integration into the Hibernate ecosystem.1
2. **The Native JPA Approaches:** This report will investigate several methods that leverage standard JPA and Hibernate features, with a primary focus on a proposal to use Java Records as @Embeddable identifiers. It will also explore the viability of using JPA AttributeConverter and the @IdClass annotation as alternative native solutions.2

### **Defining the Evaluation Criteria**

To ensure a comprehensive and objective comparison, each strategy will be evaluated against a consistent set of criteria that covers the entire lifecycle of a typed identifier:

* **Type Safety:** The degree to which the approach prevents the misuse of identifiers at compile time.
* **Boilerplate & Ergonomics:** The amount of code and ceremony required to define and use a new typed ID.
* **ID Generation:** Support for both application-generated (e.g., UUIDv7, TSID) and database-generated (@GeneratedValue) strategies.
* **Persistence & Querying:** The underlying persistence mechanism and its impact on the usability of identifiers in JPQL and Criteria API queries.
* **Ecosystem Integration:** The out-of-the-box behavior and required configuration for integration with key frameworks for serialization (e.g., Jackson) and API documentation (e.g., SpringDoc/OpenAPI).

## **Baseline Analysis: The framefork/typed-ids Library**

### **Architectural Overview**

An examination of the framefork/typed-ids library reveals a well-structured and comprehensive solution designed to abstract away the complexities of typed ID management.1 The architecture is centered around two abstract base classes,
ObjectUuid\<T\> and ObjectBigIntId\<T\>, which serve as immutable Value Object wrappers for java.util.UUID and long respectively. Developers create new typed IDs by extending these base classes, which provides a consistent and reusable pattern with minimal boilerplate code. For example, a UserId would be defined as public static final class Id extends ObjectUuid\<Id\> {... }.1 This approach immediately establishes strong type safety and provides a foundation for all subsequent features.

### **Hibernate Integration via Custom UserType**

The library's core persistence mechanism is its deep integration with Hibernate's type system, achieved through the implementation of the org.hibernate.usertype.UserType interface.5 This is a powerful, Hibernate-specific extension point that grants complete control over how a Java type is mapped to one or more database columns. The library provides
ObjectUuidType and ObjectBigIntIdType as concrete implementations.1
A key element of this integration is the use of a org.hibernate.boot.model.TypeContributor. The ObjectUuidTypesContributor and ObjectBigIntIdTypesContributor automatically scan the application's classpath for all subclasses of ObjectUuid and ObjectBigIntId at startup. This scanning is made efficient through a compile-time annotation processor (typed-ids-index-java-classes-processor) that creates an index of all typed ID classes.1 For each discovered typed ID, the contributor registers the corresponding custom
UserType with Hibernate's TypeConfiguration. This automated registration process is a significant ergonomic advantage, as it eliminates the need for developers to manually annotate every entity's ID field with @Type(ObjectUuidType.class), thereby reducing boilerplate and preventing configuration errors.1

### **ID Generation Capabilities**

The library demonstrates a sophisticated understanding of real-world ID generation requirements by providing robust support for both application-side and database-side strategies.

#### **Application-Generated Identifiers**

A primary design goal of the library is to enable the generation of identifiers within the application code, typically in an entity's constructor. This practice decouples the entity's identity from the database persistence lifecycle, ensuring that an entity object is fully valid and complete from the moment of its creation.1 To support this, the library integrates with best-in-class, high-performance generator libraries without creating a hard dependency:

* **For UUIDs:** It leverages com.fasterxml.uuid:java-uuid-generator to produce UUIDv7 identifiers. UUIDv7 is a time-ordered variant that is vastly superior to the standard UUIDv4 for use as a primary key in database indexes due to its sequential nature, which mitigates index fragmentation and improves insert performance.1
* **For BigInts:** It uses io.hypersistence:hypersistence-tsid to generate Time-Sorted Identifiers (TSIDs), which are k-sortable, 64-bit long values that are also highly performant as primary keys.1

#### **Database-Generated (@GeneratedValue) Identifiers**

One of the library's most compelling features is its explicit and seamless support for standard JPA @GeneratedValue strategies, including AUTO, IDENTITY, and SEQUENCE, for ObjectBigIntId types.1 This is a non-trivial capability, as native JPA and Hibernate mechanisms often struggle to apply generation strategies to non-primitive or complex ID types.7
The library achieves this by providing its own implementations of Hibernate's internal generator strategies: ObjectBigIntIdIdentityGenerator and ObjectBigIntIdSequenceStyleGenerator.1 These custom generators are automatically substituted for Hibernate's default generators by the
ObjectBigIntIdTypeGenerationMetadataContributor. This contributor intercepts Hibernate's metadata building process and remaps the generator strategy for any ObjectBigIntId field. The custom generators then orchestrate the interaction with the database to retrieve the generated primitive long value and subsequently wrap it in the correct typed ID object before returning it to the persistence context. This deep, internal integration solves a major pain point that plagues native approaches, demonstrating a level of completeness that goes beyond simple type mapping.

### **Ecosystem Support (Serialization and OpenAPI)**

The typed-ids library recognizes that an identifier's lifecycle extends beyond the database. It provides dedicated modules for seamless integration with major serialization and API documentation frameworks.

* **Serialization:** It includes ObjectUuidJacksonModule and ObjectBigIntIdJacksonModule for Jackson, ObjectUuidTypeAdapterFactory and ObjectBigIntIdTypeAdapterFactory for Gson, and contextual/explicit serializers for Kotlin Serialization.1 These modules are discoverable via
  java.util.ServiceLoader, which simplifies configuration and ensures that typed IDs are serialized to their underlying primitive representation (e.g., a UserId becomes a JSON string "..." or number 123\) rather than a nested object. This maintains a clean and simple data transfer contract.
* **OpenAPI:** The library offers a TypedIdsModelConverter for both generic Swagger v3 and SpringDoc.1 This converter integrates with the OpenAPI schema generation process to ensure that typed ID fields are correctly represented as their primitive counterparts in the API specification (e.g.,
  type: string, format: uuid or type: integer, format: int64). This prevents the API schema from being cluttered with unnecessary complex object definitions for simple identifiers and provides a clear, usable contract for API consumers.

## **Native Approach I: Embeddable Records as Typed IDs**

### **Implementation with @Embeddable and @EmbeddedId**

A popular native approach for creating strongly-typed identifiers, particularly with modern Java, involves using a java.lang.Record annotated with @jakarta.persistence.Embeddable. This record, which typically wraps a single primitive value, is then used as the primary key of an entity by annotating the corresponding field with @jakarta.persistence.EmbeddedId.2
For example, a UserId can be defined with the concise record syntax:

```java
@Embeddable
public record UserId(Long value) implements Serializable {
}
```

This UserId record is then used within an entity as follows:

```java
@Entity
public class User {
    @EmbeddedId
    private UserId id;

    //... other fields, constructors, getters
}
```

This pattern successfully achieves compile-time type safety and benefits from the immutability and automatic equals/hashCode implementation provided by records.

### **Hibernate Version Compatibility**

The viability of this approach is heavily dependent on the Hibernate version.

* **Hibernate 6.2 and newer:** Support for using Java Records as embeddables is built-in and works without additional configuration. Hibernate 6.2 is intelligent enough to use the record's canonical constructor for instantiation when fetching data from the database.11
* **Hibernate 6.0 and 6.1:** These versions do not have native support for record instantiation. To make this pattern work, a developer must implement and register a custom org.hibernate.metamodel.spi.EmbeddableInstantiator for *each* typed ID record. This instantiator manually reads the values from the result set and calls the record's constructor, adding a significant amount of boilerplate and configuration overhead for every new ID type.11

### **Critical Challenge: ID Generation (@GeneratedValue)**

The most significant and often prohibitive limitation of the @EmbeddedId approach for single-field typed IDs is its poor and unreliable support for database-generated values. The JPA specification does not clearly define the behavior of @GeneratedValue when applied to a field *within* an @Embeddable class that serves as an @EmbeddedId.7
This ambiguity stems from an architectural mismatch. The @EmbeddedId annotation is primarily designed to group multiple columns that form a composite primary key into a single object. Conversely, the @GeneratedValue annotation is designed to operate on a single @Id column that represents a simple primary key. By using @EmbeddedId for a single-field wrapper, developers are co-opting a feature intended for composite keys. This creates a complex, indirect flow for the persistence provider: it must first trigger the database generation, retrieve the primitive value, instantiate the embeddable record with this value, and finally set the embeddable object on the entity. This convoluted lifecycle is not what @GeneratedValue was designed for, leading to numerous reports of IdentifierGenerationException and other failures across different JPA providers and database dialects.8 Consequently, this approach is fundamentally unsuitable for any entity that relies on database-side ID generation strategies like
IDENTITY or SEQUENCE.

### **Impact on Querying, Serialization, and API Schema**

Beyond the critical issue of ID generation, the @EmbeddedId pattern introduces friction in several other key areas of application development.

* **Querying (JPQL):** Because the ID is an embedded object, accessing its underlying value in JPQL or HQL requires property dereferencing. A query to find a user by their ID becomes SELECT u FROM User u WHERE u.id.value \= :userIdValue.14 This syntax is more verbose and error-prone than a direct comparison. More importantly, it leaks the implementation detail of the ID record (the fact that the value is stored in a field named
  value) into the data access layer, creating a brittle coupling that violates the principle of encapsulation.
* **Serialization (Jackson):** By default, Jackson serializes objects based on their structure. An entity using an @EmbeddedId record will be serialized into a nested JSON object, such as {"id": {"value": 123}}.18 This is often undesirable for API clients, who expect a flattened, primitive ID like
  {"id": 123}. To achieve this flattened representation, a custom JsonSerializer must be written and registered for each typed ID, reintroducing the boilerplate that the "simple" native approach was intended to avoid.
* **OpenAPI Schema (SpringDoc):** The default behavior of API documentation tools like SpringDoc is to generate a schema that mirrors the Java object structure. For an @EmbeddedId record, this results in a complex schema definition for the ID (e.g., a UserId object with a value property) instead of a simple primitive type.20 This complicates the API contract and can be confusing for consumers. Correcting this requires manual schema annotations or a custom
  ModelConverter, adding another layer of configuration and maintenance.

## **Native Approach II: JPA AttributeConverter**

### **Core Concept and Implementation**

The Java Persistence API provides the jakarta.persistence.AttributeConverter interface as a standard mechanism for mapping a custom Java type to a single, basic database column type.3 This is a powerful feature for mapping value objects like
Money or Color to DECIMAL or VARCHAR columns, respectively.
An AttributeConverter for a typed ID would implement two methods:

1. convertToDatabaseColumn(UserId attribute): Takes the UserId object and returns the underlying Long value to be stored in the database.
2. convertToEntityAttribute(Long dbData): Takes the Long value from the database and constructs a new UserId object.

A basic implementation would look like this:

```java
@Converter(autoApply = true)
public class UserIdConverter implements AttributeConverter<UserId, Long> {
    @Override
    public Long convertToDatabaseColumn(UserId userId) {
        return userId == null? null : userId.value();
    }

    @Override
    public UserId convertToEntityAttribute(Long dbData) {
        return dbData == null? null : new UserId(dbData);
    }
}
```

When autoApply is set to true, the JPA provider will automatically use this converter for any entity attribute of type UserId.

### **The Critical Limitation: Incompatibility with @Id**

Despite its apparent suitability, the AttributeConverter approach has a fundamental limitation that makes it non-viable for primary keys in a standard, portable application: the JPA specification explicitly forbids the use of @Convert on attributes annotated with @Id.24
This restriction is not an oversight but a deliberate design choice aimed at ensuring the stability and predictability of the persistence context. The identity of an entity is the cornerstone of how the EntityManager manages state, tracks changes, and handles caching. The lifecycle for processing an @Id attribute is highly optimized and tightly controlled. Introducing a custom, user-defined conversion step into this critical path could introduce ambiguity and unpredictable behavior, for example, regarding how the persistence context should handle an object that exists in both its converted (primitive) and unconverted (typed ID) state. To avoid this complexity, the specification mandates that primary keys must be of a basic, directly mappable type. Any attempt to apply AttributeConverter directly to an @Id field is a violation of this principle and will fail in a spec-compliant JPA provider.

### **Evaluating Workarounds**

Given the direct prohibition, developers have devised workarounds to achieve a similar outcome. However, these workarounds invariably reintroduce the complexities and drawbacks of other native approaches.

* **Workaround 1: Nesting @Convert within @EmbeddedId:** The most common technique is to circumvent the @Id restriction by not using @Id at all. Instead, a single-field @Embeddable class is created. The @Convert annotation is then applied to the field *inside* this embeddable class. The entity then uses this embeddable as its primary key via @EmbeddedId.26 While this technically works and bypasses the spec restriction, it is a convoluted solution that immediately inherits all the significant drawbacks of the
  @EmbeddedId approach discussed in the previous section: unreliable @GeneratedValue support, verbose JPQL queries, nested JSON serialization, and complex OpenAPI schemas. It solves the AttributeConverter restriction only to fall into the trap of the @EmbeddedId pattern's limitations.
* **Workaround 2: Using @IdClass:** A less common but possible workaround involves @IdClass. The entity defines a primitive field annotated with @Id, but the separate @IdClass contains a field of the same name annotated with @Convert.25 This clever technique might resolve the issues related to querying and serialization, as the entity itself exposes a primitive ID. However, it introduces the cognitive overhead of the
  @IdClass pattern and remains a non-standard circumvention of the specification's clear intent. It is a more complex solution that trades one set of problems for another.

In summary, while AttributeConverter is an excellent tool for non-identifier value objects, its application to primary keys is either forbidden or requires workarounds that are more complex and problematic than the problem they intend to solve.

## **Comprehensive Feature Comparison**

The following table synthesizes the detailed analysis of each strategy, providing a direct, at-a-glance comparison across the key evaluation criteria. This matrix serves as a decision-making tool, highlighting the distinct trade-offs inherent in each approach.

| Feature/Concern | framefork/typed-ids (Custom UserType) | @EmbeddedId with Record | AttributeConverter (with @EmbeddedId Workaround) |
| :---- | :---- | :---- | :---- |
| **Type Safety** | **Excellent.** Enforced at compile time by the Java type system. | **Excellent.** Enforced at compile time by the Java type system. | **Excellent.** Enforced at compile time by the Java type system. |
| **Boilerplate Code** | **Minimal.** Requires inheriting from a base class and adding static factory methods. | **Minimal.** The Java Record syntax is extremely concise for data carriers. | **Moderate.** Requires the definition of the ID record plus a separate converter class for each ID type. |
| **@GeneratedValue Support** | **Excellent.** Full, robust support for AUTO, IDENTITY, and SEQUENCE via custom Hibernate generators.1 | **Poor/Unreliable.** Not defined by the JPA specification for this use case; known to be problematic and fail.8 | **Poor/Unreliable.** Inherits the fundamental limitations of the @EmbeddedId workaround.25 |
| **Querying Ergonomics (JPQL)** | **Excellent.** Fully transparent. Queries are written as if against a primitive: WHERE e.id \= :id. | **Poor.** Verbose and leaks implementation details: WHERE e.id.value \= :idValue.14 | **Poor.** Inherits the verbose query syntax from the @EmbeddedId workaround. |
| **JPA Portability** | **Low.** Relies on the Hibernate-specific UserType interface. This is a vendor lock-in, albeit to a powerful feature. | **Moderate.** Relies on Hibernate 6.2+ for native record support without a custom instantiator.11 | **Low.** The workaround itself is a non-standard pattern that circumvents the JPA specification. |
| **Jackson Serialization** | **Excellent.** Serializes to a flattened primitive value out-of-the-box via an auto-discoverable module.1 | **Poor.** Serializes to a nested object ({"id":{"value":123}}) by default, requiring a custom serializer to flatten. | **Poor.** Serializes to a nested object by default due to the structure of the @EmbeddedId workaround. |
| **OpenAPI Schema (SpringDoc)** | **Excellent.** Generates a correct, primitive schema (string, integer) via a provided, auto-discoverable ModelConverter.1 | **Poor.** Generates a complex object schema by default, complicating the API contract.20 | **Poor.** Generates a complex object schema by default due to the workaround. |
| **Overall Complexity** | **Low.** The library abstracts away all underlying complexity, providing a simple and consistent developer experience. | **Moderate.** The initial definition is simple, but it creates complex and problematic side effects in downstream systems. | **High.** Requires understanding multiple JPA concepts, their limitations, and non-obvious workarounds to function. |

## **In-depth Analysis and Recommendations**

### **Synthesizing the Findings**

The comparative analysis reveals a clear distinction between the purpose-built library and the adapted native approaches. While all strategies successfully achieve compile-time type safety, their impact on the broader development lifecycle differs dramatically.
The native approaches, using @EmbeddedId or AttributeConverter workarounds, suffer from what can be described as a "leaky abstraction." The implementation detail of how the typed ID is persisted—the fact that it is a wrapper object containing a value field—is not fully encapsulated at the persistence layer. This detail "leaks" into higher-level concerns, forcing developers to write verbose JPQL queries (u.id.value), create custom serializers to produce clean JSON, and configure model converters to generate sane API schemas. This ripple effect of complexity undermines the initial goal of simplifying development through type safety.
In contrast, the framefork/typed-ids library provides a "full-stack" solution. By leveraging the Hibernate-specific UserType interface, it creates a much stronger, non-leaky abstraction. Hibernate is taught how to treat the typed ID *as if* it were a primitive for the purposes of querying, serialization hints, and metadata generation. The library doesn't just solve the persistence problem; it anticipates and addresses the entire developer experience, from entity definition and ID generation to API documentation. This holistic approach is what sets it apart from the native alternatives, which only solve a fraction of the problem and leave the developer to manually address the consequences.

### **Context-Specific Recommendations**

The optimal strategy depends on project constraints and priorities. The following recommendations are provided to guide architectural decisions in different contexts.

* For Green-Field Projects Prioritizing Developer Experience and Robustness:
  The framefork/typed-ids library is the unequivocally superior choice. The minor cost of introducing a focused, third-party dependency is overwhelmingly justified by the substantial benefits it provides: minimal boilerplate, seamless and reliable @GeneratedValue support, clean and ergonomic querying, and out-of-the-box integration with essential ecosystem tools like Jackson and SpringDoc. This approach allows the development team to adopt the typed ID pattern without friction, maximizing productivity and code quality.
* For Projects with a Strict "No New Dependencies" Policy:
  If adding a third-party library is not permissible, the @EmbeddedId with Java Record approach is the most viable native solution, but it should only be considered under a specific set of conditions:
  1. The project is standardized on **Hibernate 6.2 or newer** to avoid the need for custom EmbeddableInstantiator boilerplate.
  2. The entities using this pattern **do not require database-generated identifiers** (@GeneratedValue). This is a critical, non-negotiable constraint.
  3. The development team is willing to accept the long-term trade-offs, including verbose JPQL queries, and is prepared to invest the effort in writing and maintaining **custom Jackson serializers and OpenAPI model converters** to ensure a clean and usable API contract.
* When to Avoid AttributeConverter for IDs:
  The AttributeConverter pattern, while an excellent tool for mapping non-identifier value objects, should be actively avoided for primary keys. The JPA specification's prohibition against its use on @Id fields means that any implementation requires a workaround. These workarounds inevitably lead back to the @EmbeddedId pattern, making the AttributeConverter approach a more complex and less direct path to a solution that already has significant inherent flaws. It offers no advantages over the direct @EmbeddedId approach and introduces additional conceptual overhead.

## **Conclusion**

This report has conducted a thorough investigation into various strategies for implementing strongly-typed entity identifiers in a Hibernate ORM environment. The analysis demonstrates that while native JPA and Hibernate features like @Embeddable with Java Records can be adapted to provide type safety, they introduce significant friction and limitations, particularly concerning database-side ID generation, query ergonomics, and integration with serialization and API documentation frameworks. These native approaches solve the immediate problem of type safety but create a cascade of downstream complexities that must be manually addressed by the development team.
In contrast, the framefork/typed-ids library presents a complete and mature solution. By leveraging Hibernate's powerful but non-standard extension points, such as UserType and custom ID generators, it provides a seamless and developer-friendly experience that covers the entire lifecycle of a typed identifier. It successfully abstracts away the underlying complexity, allowing developers to benefit from strong type safety without compromising on essential features or clean API design.
Therefore, for teams seeking to adopt the strongly-typed ID pattern to build more robust and maintainable applications, the framefork/typed-ids library is the recommended path. It represents a well-designed, holistic solution that has anticipated and solved the full spectrum of challenges associated with this powerful domain modeling practice.

#### **Works cited**

1. https://github.com/framefork/typed-ids/ codebase
2. Hibernate: Composite Primary Keys with @Embeddable and @IdClass \- Medium, accessed August 24, 2025, [https://medium.com/jpa-java-persistence-api-guide/hibernate-composite-primary-keys-with-embeddable-and-idclass-73ad3fcbc7cc](https://medium.com/jpa-java-persistence-api-guide/hibernate-composite-primary-keys-with-embeddable-and-idclass-73ad3fcbc7cc)
3. Implementing an AttributeConverter With JPA \- Lorenzo Miscoli, accessed August 24, 2025, [https://lorenzomiscoli.com/implementing-an-attributeconverter-with-jpa/](https://lorenzomiscoli.com/implementing-an-attributeconverter-with-jpa/)
4. IdClass (hibernate-jpa-2.1-api 1.0.0.Final API), accessed August 24, 2025, [https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/IdClass.html](https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/IdClass.html)
5. UserType (Hibernate Javadocs) \- Red Hat on GitHub, accessed August 24, 2025, [https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/usertype/UserType.html](https://docs.jboss.org/hibernate/orm/current/javadocs/org/hibernate/usertype/UserType.html)
6. How to implement a custom basic type using Hibernate UserType, accessed August 24, 2025, [https://vladmihalcea.com/how-to-implement-a-custom-basic-type-using-hibernate-usertype/](https://vladmihalcea.com/how-to-implement-a-custom-basic-type-using-hibernate-usertype/)
7. An Overview of Identifiers in Hibernate/JPA | Baeldung, accessed August 24, 2025, [https://www.baeldung.com/hibernate-identifiers](https://www.baeldung.com/hibernate-identifiers)
8. java \- Entity with @EmbeddedId , @GeneratedValue and ..., accessed August 24, 2025, [https://stackoverflow.com/questions/49465335/entity-with-embeddedid-generatedvalue-and-manytoone](https://stackoverflow.com/questions/49465335/entity-with-embeddedid-generatedvalue-and-manytoone)
9. EmbeddedId and GeneratedValue| JBoss.org Content Archive (Read Only), accessed August 24, 2025, [https://developer.jboss.org/thread/107207](https://developer.jboss.org/thread/107207)
10. Jpa @Embedded and @Embeddable | Baeldung, accessed August 24, 2025, [https://www.baeldung.com/jpa-embedded-embeddable](https://www.baeldung.com/jpa-embedded-embeddable)
11. Java Records as Embeddables with Hibernate 6 \- Thorben Janssen, accessed August 24, 2025, [https://thorben-janssen.com/java-records-embeddables-hibernate/](https://thorben-janssen.com/java-records-embeddables-hibernate/)
12. Hibernate 6 EmbeddableInstantiator \- Instantiate embeddables your way \- Thorben Janssen, accessed August 24, 2025, [https://thorben-janssen.com/hibernate-embeddableinstantiator/](https://thorben-janssen.com/hibernate-embeddableinstantiator/)
13. View topic \- @EmbeddedId that contains an Identity column on SQL Server, accessed August 24, 2025, [https://forum.hibernate.org/viewtopic.php?p=2436748](https://forum.hibernate.org/viewtopic.php?p=2436748)
14. How to write JPQL SELECT with embedded id? \- java \- Stack Overflow, accessed August 24, 2025, [https://stackoverflow.com/questions/4676904/how-to-write-jpql-select-with-embedded-id](https://stackoverflow.com/questions/4676904/how-to-write-jpql-select-with-embedded-id)
15. How do I query based on fields of EmbeddedId using JPA Criteria? \- Stack Overflow, accessed August 24, 2025, [https://stackoverflow.com/questions/60472066/how-do-i-query-based-on-fields-of-embeddedid-using-jpa-criteria](https://stackoverflow.com/questions/60472066/how-do-i-query-based-on-fields-of-embeddedid-using-jpa-criteria)
16. Composite key handling, using @EmbeddedId annotation in Spring boot java \- Medium, accessed August 24, 2025, [https://medium.com/@bhagyajayashani/composite-key-handling-using-embeddedid-annotation-in-spring-boot-java-67c29da9d119](https://medium.com/@bhagyajayashani/composite-key-handling-using-embeddedid-annotation-in-spring-boot-java-67c29da9d119)
17. Mapping a Composite Key With JPA and Hibernate \- Lorenzo Miscoli, accessed August 24, 2025, [https://lorenzomiscoli.com/mapping-a-composite-key-with-jpa-and-hibernate/](https://lorenzomiscoli.com/mapping-a-composite-key-with-jpa-and-hibernate/)
18. Practical Java 16 \- Using Jackson to serialize Records \- DEV Community, accessed August 24, 2025, [https://dev.to/brunooliveira/practical-java-16-using-jackson-to-serialize-records-4og4](https://dev.to/brunooliveira/practical-java-16-using-jackson-to-serialize-records-4og4)
19. Efficient JSON serialization with Jackson and Java \- Oracle Blogs, accessed August 24, 2025, [https://blogs.oracle.com/javamagazine/post/java-json-serialization-jackson](https://blogs.oracle.com/javamagazine/post/java-json-serialization-jackson)
20. Support for Java Record · Issue \#1041 · springdoc/springdoc-openapi, accessed August 24, 2025, [https://github.com/springdoc/springdoc-openapi/issues/1041](https://github.com/springdoc/springdoc-openapi/issues/1041)
21. SpringDoc Incorrectly Generates Schema When Using Record Named "Item" · Issue \#2907, accessed August 24, 2025, [https://github.com/springdoc/springdoc-openapi/issues/2907](https://github.com/springdoc/springdoc-openapi/issues/2907)
22. JPA Attribute Converters | Baeldung, accessed August 24, 2025, [https://www.baeldung.com/jpa-attribute-converters](https://www.baeldung.com/jpa-attribute-converters)
23. JPA AttributeConverter \- A Beginner's Guide \- Vlad Mihalcea, accessed August 24, 2025, [https://vladmihalcea.com/jpa-attributeconverter/](https://vladmihalcea.com/jpa-attributeconverter/)
24. How to implement an AttributeConverter to support custom types \- Thorben Janssen, accessed August 24, 2025, [https://thorben-janssen.com/jpa-attribute-converter/](https://thorben-janssen.com/jpa-attribute-converter/)
25. Convert on @Id field \- hibernate \- Stack Overflow, accessed August 24, 2025, [https://stackoverflow.com/questions/44069361/convert-on-id-field](https://stackoverflow.com/questions/44069361/convert-on-id-field)
26. How to create attribute converter for @id \- java \- Stack Overflow, accessed August 24, 2025, [https://stackoverflow.com/questions/42929194/how-to-create-attribute-converter-for-id](https://stackoverflow.com/questions/42929194/how-to-create-attribute-converter-for-id)
