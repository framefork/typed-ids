package org.framefork.typedIds.index.processor;

import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Reads and writes the index files in the class output.
 * <p>
 * The format is what the runtime readers of every released version understand: binary class names, one per line, each followed by a newline,
 * no comments, UTF-8.
 */
final class SubclassIndexFiles
{

    private static final String INDEX_PREFIX = "META-INF/services/";

    private final Filer filer;

    SubclassIndexFiles(final Filer filer)
    {
        this.filer = filer;
    }

    static String resourceName(final String baseClassCanonicalName)
    {
        return INDEX_PREFIX + baseClassCanonicalName;
    }

    /**
     * Returns null when the class output holds no such index file yet.
     */
    @Nullable ExistingIndexFile read(final String resourceName)
    {
        FileObject resource;
        try {
            resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }

        try (InputStream input = resource.openInputStream()) {
            return new ExistingIndexFile(resource, readEntries(input));

        } catch (NoSuchFileException e) {
            return null;

        } catch (FileNotFoundException e) {
            // IntelliJ's own builder reports files that an earlier build session wrote as missing, with their path as the message.
            @Nullable String reportedPath = e.getMessage();
            if (reportedPath != null && Files.isRegularFile(Path.of(reportedPath))) {
                try (InputStream input = Files.newInputStream(Path.of(reportedPath))) {
                    return new ExistingIndexFile(resource, readEntries(input));
                } catch (IOException | RuntimeException ignored) {
                    return null;
                }
            }

            return null;

        } catch (IOException e) {
            return null;
        }
    }

    void write(final String resourceName, final Collection<String> entries, @Nullable final ExistingIndexFile existing) throws IOException
    {
        FileObject file;
        try {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        } catch (FilerException e) {
            // Eclipse refuses to create a resource that was read in the same compilation, but lets the read file object be written.
            if (existing == null) {
                throw e;
            }
            file = existing.file();
        }

        try (Writer writer = new OutputStreamWriter(file.openOutputStream(), StandardCharsets.UTF_8)) {
            for (String entry : entries) {
                writer.write(entry);
                writer.write('\n');
            }
        }
    }

    private static List<String> readEntries(final InputStream input) throws IOException
    {
        try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
        }
    }

    record ExistingIndexFile(FileObject file, List<String> entries)
    {

    }

}
