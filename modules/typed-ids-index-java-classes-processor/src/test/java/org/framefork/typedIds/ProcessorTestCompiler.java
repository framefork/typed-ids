package org.framefork.typedIds;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs javac in-process with {@link TypedIdsClassIndexProcessor} on a set of sources, into an output directory that survives between compilations.
 * <p>
 * A second compilation into the same output directory, with that directory on the classpath, is what a partial compile does:
 * IntelliJ's own builder or Maven without incremental compilation recompile only some sources over the previous output.
 */
final class ProcessorTestCompiler
{

    private final Path workDir;
    private final Path outputDir;
    private final List<String> classpath;
    private int compilationCount = 0;

    private ProcessorTestCompiler(final Path workDir, final List<String> classpath)
    {
        this.workDir = workDir;
        this.outputDir = workDir.resolve("classes");
        this.classpath = classpath;
    }

    /**
     * Compiles against the test classpath, which has the current typed-ids.
     */
    static ProcessorTestCompiler withTypedIds(final Path workDir)
    {
        return new ProcessorTestCompiler(workDir, List.of(System.getProperty("java.class.path").split(File.pathSeparator)));
    }

    /**
     * Compiles against an empty classpath, so the sources have to bring every class they use.
     */
    static ProcessorTestCompiler withoutTypedIds(final Path workDir)
    {
        return new ProcessorTestCompiler(workDir, List.of());
    }

    Path outputDir()
    {
        return outputDir;
    }

    /**
     * @param sources relative source file path to its content
     */
    Result compile(final Map<String, String> sources)
    {
        try {
            compilationCount++;
            Path sourceDir = workDir.resolve("src" + compilationCount);
            List<Path> sourceFiles = new ArrayList<>();
            for (var source : sources.entrySet()) {
                Path file = sourceDir.resolve(source.getKey());
                Files.createDirectories(file.getParent());
                Files.writeString(file, source.getValue(), StandardCharsets.UTF_8);
                sourceFiles.add(file);
            }
            Files.createDirectories(outputDir);

            List<String> fullClasspath = new ArrayList<>(classpath);
            fullClasspath.add(outputDir.toString());

            List<String> options = new ArrayList<>(List.of(
                "-d", outputDir.toString(),
                "-classpath", String.join(File.pathSeparator, fullClasspath),
                "-encoding", "UTF-8"
            ));

            JavaCompiler compiler = Objects.requireNonNull(ToolProvider.getSystemJavaCompiler(), "the tests need a JDK");
            var diagnostics = new DiagnosticCollector<JavaFileObject>();
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                var task = compiler.getTask(null, fileManager, diagnostics, options, null, fileManager.getJavaFileObjectsFromPaths(sourceFiles));
                task.setProcessors(List.of(new TypedIdsClassIndexProcessor()));
                boolean success = task.call();
                return new Result(success, diagnostics.getDiagnostics(), outputDir);
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    record Result(boolean success, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path outputDir)
    {

        Result assertSuccess()
        {
            if (!success) {
                throw new AssertionError("Compilation failed:\n" + errors());
            }
            return this;
        }

        String errors()
        {
            return diagnostics.stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .map(diagnostic -> diagnostic.getMessage(null))
                .collect(Collectors.joining("\n"));
        }

        Optional<String> index(final String baseClassCanonicalName)
        {
            Path file = outputDir.resolve("META-INF/services/" + baseClassCanonicalName);
            if (!Files.exists(file)) {
                return Optional.empty();
            }

            try {
                return Optional.of(Files.readString(file, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

    }

}
