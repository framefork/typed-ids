package org.framefork.typedIds.index;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Builds a Gradle project that has Lombok and the typed-ids index processor on its processor path, changes its sources,
 * and checks that Gradle compiles the change incrementally and that the index stays correct.
 * <p>
 * Gradle compiles incrementally only when every processor on the path is registered as incremental. It then deletes the index files
 * before each compilation and hands the processor every class it indexed before, so the index must come out complete.
 */
final class IndexProcessorGradleIncrementalCompilationTest
{

    private static final String UUID_INDEX = "build/classes/java/main/META-INF/services/org.framefork.typedIds.uuid.ObjectUuid";
    private static final String BIGINT_INDEX = "build/classes/java/main/META-INF/services/org.framefork.typedIds.bigint.ObjectBigIntId";

    private static final String FULL_UUID_INDEX = """
        com.example.AnotherId
        com.example.Holder$NestedId
        com.example.Person$Id
        com.example.UserId
        """;

    @TempDir
    Path projectDir;

    @BeforeEach
    public void buildTheProjectOnce() throws IOException
    {
        write("settings.gradle", "rootProject.name = 'fixture'\n");
        write("build.gradle", """
            plugins {
                id 'java'
            }

            dependencies {
                compileOnly files(%1$s)
                annotationProcessor files(%1$s)
                annotationProcessor files(%2$s)
                implementation files(%3$s)
            }
            """.formatted(
            groovyFileList("fixture.lombok"),
            groovyFileList("fixture.processor"),
            groovyFileList("fixture.typedIds")
        ));

        write("src/main/java/com/example/UserId.java", uuidId("UserId"));
        write("src/main/java/com/example/AnotherId.java", uuidId("AnotherId"));
        write("src/main/java/com/example/OrderId.java", """
            package com.example;

            public final class OrderId extends org.framefork.typedIds.bigint.ObjectBigIntId<OrderId>
            {
                private OrderId(final long inner) { super(inner); }
            }
            """);
        write("src/main/java/com/example/Holder.java", """
            package com.example;

            public final class Holder
            {
                public static final class NestedId extends org.framefork.typedIds.uuid.ObjectUuid<NestedId>
                {
                    private NestedId(final java.util.UUID inner) { super(inner); }
                }

                public static String describe() { return "holder"; }
            }
            """);
        write("src/main/java/com/example/Person.java", """
            package com.example;

            public record Person(String name)
            {
                public static final class Id extends org.framefork.typedIds.uuid.ObjectUuid<Id>
                {
                    private Id(final java.util.UUID inner) { super(inner); }
                }
            }
            """);
        write("src/main/java/com/example/Customer.java", customer("customer"));

        build();

        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX);
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void editOfAClassThatIsNotAnId() throws IOException
    {
        write("src/main/java/com/example/Customer.java", customer("changed"));

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX);
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void editOfAnIdClass() throws IOException
    {
        write("src/main/java/com/example/UserId.java", """
            package com.example;

            public final class UserId extends org.framefork.typedIds.uuid.ObjectUuid<UserId>
            {
                private UserId(final java.util.UUID inner) { super(inner); }

                public static UserId random() { return randomUUID(UserId::new); }
            }
            """);

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX);
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void addedIdClass() throws IOException
    {
        write("src/main/java/com/example/AddedId.java", uuidId("AddedId"));

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo("com.example.AddedId\n" + FULL_UUID_INDEX);
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void deletedIdClass() throws IOException
    {
        Files.delete(projectDir.resolve("src/main/java/com/example/AnotherId.java"));

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX.replace("com.example.AnotherId\n", ""));
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void idClassThatStopsExtendingTheBaseClass() throws IOException
    {
        write("src/main/java/com/example/UserId.java", """
            package com.example;

            public final class UserId
            {
            }
            """);

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX.replace("com.example.UserId\n", ""));
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    @Test
    public void nestedIdRemovedFromAnOuterClassThatStays() throws IOException
    {
        write("src/main/java/com/example/Holder.java", """
            package com.example;

            public final class Holder
            {
                public static String describe() { return "holder"; }
            }
            """);

        assertIncremental(build());
        assertThat(read(UUID_INDEX)).isEqualTo(FULL_UUID_INDEX.replace("com.example.Holder$NestedId\n", ""));
        assertThat(read(BIGINT_INDEX)).isEqualTo("com.example.OrderId\n");
    }

    private static void assertIncremental(final BuildResult result)
    {
        assertThat(result.getOutput())
            .contains("Incremental compilation of ")
            .doesNotContain("Full recompilation is required");
    }

    private BuildResult build()
    {
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withTestKitDir(new File(requiredProperty("fixture.testKitDir")))
            .withArguments("compileJava", "--info", "--stacktrace")
            .build();
    }

    private static String customer(final String name)
    {
        return """
            package com.example;

            @lombok.Getter
            @lombok.RequiredArgsConstructor
            public final class Customer
            {
                private final String name;
                private final UserId userId;

                @Override
                public String toString() { return "%s"; }
            }
            """.formatted(name);
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

    private static String groovyFileList(final String property)
    {
        return Arrays.stream(requiredProperty(property).split(File.pathSeparator))
            .map(path -> "'" + path.replace("\\", "\\\\").replace("'", "\\'") + "'")
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String requiredProperty(final String name)
    {
        return Objects.requireNonNull(System.getProperty(name), () -> "system property " + name + " is set by the Gradle test task");
    }

    private void write(final String path, final String content) throws IOException
    {
        Path file = projectDir.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    private String read(final String path) throws IOException
    {
        return Files.readString(projectDir.resolve(path), StandardCharsets.UTF_8);
    }

}
