package org.framefork.typedIds;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.index.processor.SubclassIndexProcessor;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Indexes the concrete subclasses of {@code ObjectUuid} and {@code ObjectBigIntId} into {@code META-INF/services/<base class canonical name>},
 * which {@code TypedIdsRegistry} reads at runtime.
 * <p>
 * It refers to typed-ids classes by name only: javac resolves them from the compile classpath of the code being compiled, not from the processor path.
 * <p>
 * The jar registers this class as an aggregating processor in {@code META-INF/gradle/incremental.annotation.processors}, under its exact name.
 */
@AutoService(Processor.class)
public class TypedIdsClassIndexProcessor extends SubclassIndexProcessor
{

    static final String MARKER_ANNOTATION = "org.framefork.typedIds.common.index.IndexTypedIdSubclasses";

    static final List<String> INDEXED_BASE_CLASSES = List.of(
        "org.framefork.typedIds.uuid.ObjectUuid",
        "org.framefork.typedIds.bigint.ObjectBigIntId"
    );

    private static final String VERSION_RESOURCE = "typed-ids-index-java-classes-processor.properties";

    public TypedIdsClassIndexProcessor()
    {
        super(MARKER_ANNOTATION, INDEXED_BASE_CLASSES);
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);

        var elements = processingEnv.getElementUtils();
        if (elements.getTypeElement(MARKER_ANNOTATION) != null) {
            return;
        }

        for (String baseClass : INDEXED_BASE_CLASSES) {
            if (elements.getTypeElement(baseClass) != null) {
                @Nullable String version = processorVersion();
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "typed-ids-index-java-classes-processor" + (version != null ? " " + version : "") + " needs org.framefork:typed-ids" + (version != null ? " " + version : "") + " on the compile classpath, "
                        + "but found " + baseClass + " without the " + MARKER_ANNOTATION + " annotation, which only newer typed-ids versions have. "
                        + "Use the same version of org.framefork:typed-ids and org.framefork:typed-ids-index-java-classes-processor."
                );
                return;
            }
        }
    }

    private static @Nullable String processorVersion()
    {
        try (@Nullable InputStream input = TypedIdsClassIndexProcessor.class.getResourceAsStream(VERSION_RESOURCE)) {
            if (input != null) {
                var properties = new Properties();
                properties.load(input);
                @Nullable String version = properties.getProperty("version");
                if (version != null && !version.isBlank()) {
                    return version;
                }
            }
        } catch (IOException ignored) {
            // the error then names no version
        }

        return null;
    }

}
