package org.framefork.typedIds;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

final class TypedIdsClassIndexProcessorTest
{

    private static final String UUID_INDEX = "org.framefork.typedIds.uuid.ObjectUuid";
    private static final String BIGINT_INDEX = "org.framefork.typedIds.bigint.ObjectBigIntId";

    @TempDir
    Path workDir;

    @Test
    public void indexesTopLevelNestedRecordNestedAndIndirectSubclassesButNotAbstractOnes()
    {
        var result = ProcessorTestCompiler.withTypedIds(workDir).compile(Map.of(
            "com/example/TopLevelId.java", uuidId("TopLevelId"),
            "com/example/Holder.java", """
                package com.example;

                public final class Holder
                {
                    public static final class NestedId extends org.framefork.typedIds.bigint.ObjectBigIntId<NestedId>
                    {
                        private NestedId(final long inner) { super(inner); }
                    }

                    public static final class Middle
                    {
                        public static final class DeepId extends org.framefork.typedIds.uuid.ObjectUuid<DeepId>
                        {
                            private DeepId(final java.util.UUID inner) { super(inner); }
                        }
                    }
                }
                """,
            "com/example/Person.java", """
                package com.example;

                public record Person(String name)
                {
                    public static final class Id extends org.framefork.typedIds.uuid.ObjectUuid<Id>
                    {
                        private Id(final java.util.UUID inner) { super(inner); }
                    }
                }
                """,
            "com/example/AbstractTenantId.java", """
                package com.example;

                public abstract class AbstractTenantId<SelfType extends AbstractTenantId<SelfType>> extends org.framefork.typedIds.uuid.ObjectUuid<SelfType>
                {
                    protected AbstractTenantId(final java.util.UUID inner) { super(inner); }
                }
                """,
            "com/example/TenantUserId.java", """
                package com.example;

                public final class TenantUserId extends AbstractTenantId<TenantUserId>
                {
                    private TenantUserId(final java.util.UUID inner) { super(inner); }
                }
                """,
            "com/example/NotAnId.java", """
                package com.example;

                public final class NotAnId
                {
                    @Override
                    public String toString() { return "not an id"; }
                }
                """
        )).assertSuccess();

        assertThat(result.index(UUID_INDEX)).hasValue("""
            com.example.Holder$Middle$DeepId
            com.example.Person$Id
            com.example.TenantUserId
            com.example.TopLevelId
            """);
        assertThat(result.index(BIGINT_INDEX)).hasValue("""
            com.example.Holder$NestedId
            """);
    }

    @Test
    public void writesNoIndexWhenThereAreNoIdClasses()
    {
        var result = ProcessorTestCompiler.withTypedIds(workDir).compile(Map.of(
            "com/example/NotAnId.java", """
                package com.example;

                public final class NotAnId
                {
                }
                """
        )).assertSuccess();

        assertThat(result.index(UUID_INDEX)).isEmpty();
        assertThat(result.index(BIGINT_INDEX)).isEmpty();
    }

    @Test
    public void partialCompileMergesWithTheExistingIndexAndPrunesStaleEntries() throws IOException
    {
        var compiler = ProcessorTestCompiler.withTypedIds(workDir);

        var fullBuild = compiler.compile(Map.of(
            "com/example/KeptId.java", uuidId("KeptId"),
            "com/example/BecomesAbstractId.java", uuidId("BecomesAbstractId"),
            "com/example/DeletedId.java", uuidId("DeletedId"),
            "com/example/Holder.java", """
                package com.example;

                public final class Holder
                {
                    public static final class NestedId extends org.framefork.typedIds.uuid.ObjectUuid<NestedId>
                    {
                        private NestedId(final java.util.UUID inner) { super(inner); }
                    }
                }
                """
        )).assertSuccess();

        assertThat(fullBuild.index(UUID_INDEX)).hasValue("""
            com.example.BecomesAbstractId
            com.example.DeletedId
            com.example.Holder$NestedId
            com.example.KeptId
            """);

        // A build tool deletes the class files of a deleted source, and recompiles only the changed and added sources.
        Files.delete(compiler.outputDir().resolve("com/example/DeletedId.class"));

        var partialBuild = compiler.compile(Map.of(
            "com/example/AddedId.java", uuidId("AddedId"),
            "com/example/BecomesAbstractId.java", """
                package com.example;

                public abstract class BecomesAbstractId extends org.framefork.typedIds.uuid.ObjectUuid<BecomesAbstractId>
                {
                    protected BecomesAbstractId(final java.util.UUID inner) { super(inner); }
                }
                """,
            "com/example/Holder.java", """
                package com.example;

                public final class Holder
                {
                    public static final class NestedId
                    {
                    }
                }
                """
        )).assertSuccess();

        assertThat(partialBuild.index(UUID_INDEX)).hasValue("""
            com.example.AddedId
            com.example.KeptId
            """);
    }

    @Test
    public void partialCompileMovesAClassThatChangedItsBaseToTheOtherIndex()
    {
        var compiler = ProcessorTestCompiler.withTypedIds(workDir);

        var fullBuild = compiler.compile(Map.of(
            "com/example/KeptId.java", uuidId("KeptId"),
            "com/example/MovedId.java", uuidId("MovedId"),
            "com/example/OrderId.java", bigIntId("OrderId")
        )).assertSuccess();

        assertThat(fullBuild.index(UUID_INDEX)).hasValue("""
            com.example.KeptId
            com.example.MovedId
            """);
        assertThat(fullBuild.index(BIGINT_INDEX)).hasValue("""
            com.example.OrderId
            """);

        // The partial compile has no ObjectUuid subclass, only the class that moved away from it.
        var partialBuild = compiler.compile(Map.of(
            "com/example/MovedId.java", bigIntId("MovedId")
        )).assertSuccess();

        assertThat(partialBuild.index(UUID_INDEX)).hasValue("""
            com.example.KeptId
            """);
        assertThat(partialBuild.index(BIGINT_INDEX)).hasValue("""
            com.example.MovedId
            com.example.OrderId
            """);
    }

    @Test
    public void failsWithTheRequiredVersionWhenTypedIdsIsTooOldToHaveTheMarker()
    {
        var result = ProcessorTestCompiler.withoutTypedIds(workDir).compile(Map.of(
            // typed-ids before the marker annotation
            "org/framefork/typedIds/uuid/ObjectUuid.java", """
                package org.framefork.typedIds.uuid;

                public abstract class ObjectUuid<SelfType extends ObjectUuid<SelfType>>
                {
                    protected ObjectUuid(final java.util.UUID inner) { }
                }
                """,
            "com/example/UserId.java", uuidId("UserId")
        ));

        assertThat(result.success()).isFalse();
        assertThat(result.errors())
            .containsPattern("typed-ids-index-java-classes-processor \\S+ needs org\\.framefork:typed-ids \\S+ on the compile classpath")
            .contains("but found org.framefork.typedIds.uuid.ObjectUuid without the org.framefork.typedIds.common.index.IndexTypedIdSubclasses annotation");
    }

    @Test
    public void doesNothingWithoutTypedIdsOnTheClasspath()
    {
        var result = ProcessorTestCompiler.withoutTypedIds(workDir).compile(Map.of(
            "com/example/NotAnId.java", """
                package com.example;

                public final class NotAnId
                {
                }
                """
        )).assertSuccess();

        assertThat(result.index(UUID_INDEX)).isEmpty();
        assertThat(result.index(BIGINT_INDEX)).isEmpty();
    }

    @Test
    public void isRegisteredForGradleAsAggregatingIncrementalProcessor() throws IOException
    {
        try (InputStream input = TypedIdsClassIndexProcessor.class.getResourceAsStream("/META-INF/gradle/incremental.annotation.processors")) {
            var content = new String(Objects.requireNonNull(input, "registration is missing").readAllBytes(), StandardCharsets.UTF_8);

            assertThat(content).isEqualTo(TypedIdsClassIndexProcessor.class.getName() + ",aggregating\n");
        }
    }

    private static String uuidId(final String name)
    {
        return """
            package com.example;

            public final class %1$s extends org.framefork.typedIds.uuid.ObjectUuid<%1$s>
            {
                private %1$s(final java.util.UUID inner) { super(inner); }
            }
            """.formatted(name);
    }

    private static String bigIntId(final String name)
    {
        return """
            package com.example;

            public final class %1$s extends org.framefork.typedIds.bigint.ObjectBigIntId<%1$s>
            {
                private %1$s(final long inner) { super(inner); }
            }
            """.formatted(name);
    }

}
