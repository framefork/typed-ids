package org.framefork.typedIds.index.processor;

import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Indexes the concrete subclasses of every base class that declares a marker annotation.
 * <p>
 * The marker must be {@link java.lang.annotation.Inherited @Inherited} and have at least {@code CLASS} retention.
 * javac then reports every subclass as annotated, including nested and indirect ones, so the marker can be the only annotation this processor supports.
 * That is what lets Gradle run it as an aggregating incremental processor: a processor that supports {@code *} makes Gradle recompile everything.
 * <p>
 * Each annotated concrete class is filed under the nearest superclass that declares the marker directly.
 * The index of that base class is the resource {@code META-INF/services/<canonical name of the base class>}:
 * the binary names of its concrete subclasses ({@code Outer$Inner}), sorted, one per line, each followed by a newline, with no comments.
 * Abstract classes are left out, because a framework that instantiates the indexed classes cannot use them.
 * <p>
 * An index file already present in the class output is merged with the new entries.
 * An old entry is dropped when it no longer resolves, is abstract, or no longer belongs to the base class.
 * That keeps the index complete when a build recompiles only some of the sources, as IntelliJ's own builder or a non-incremental Maven build do.
 * The existing index of every known base class is checked this way, also when the compilation saw none of its subclasses, so a class that moved to another base leaves the old index.
 * javac runs the processor only when the compilation has a class that carries the marker, so a partial compile without any such class leaves the index files as they are.
 * Gradle deletes the outputs of an aggregating processor before each compilation and hands it all previously indexed classes again,
 * so the merge finds nothing to merge there.
 * <p>
 * This class uses nothing but the JDK and knows the marker only by name, so a library needs to name its marker and nothing else.
 */
public abstract class SubclassIndexProcessor extends AbstractProcessor
{

    private final String markerAnnotationName;

    private final Set<String> knownBaseClassNames;

    private final Map<String, Set<String>> indexedSubclassesByBase = new TreeMap<>();

    /**
     * @param knownBaseClassNames the canonical names of the classes that declare the marker, whose existing index files every run checks for stale entries
     */
    protected SubclassIndexProcessor(final String markerAnnotationName, final Collection<String> knownBaseClassNames)
    {
        this.markerAnnotationName = markerAnnotationName;
        this.knownBaseClassNames = Set.copyOf(knownBaseClassNames);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return Set.of(markerAnnotationName);
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
    {
        @Nullable TypeElement marker = processingEnv.getElementUtils().getTypeElement(markerAnnotationName);
        if (marker == null) {
            return false;
        }

        if (roundEnv.processingOver()) {
            writeIndexFiles(marker);
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(marker)) {
            if (!(element instanceof TypeElement type)) {
                continue;
            }

            @Nullable TypeElement base = findIndexBase(type, marker);
            if (base == null) {
                continue; // the class declares the marker itself and has no indexed superclass
            }

            // Every base seen gets its index rewritten, even when this compilation only made one of its subclasses abstract.
            Set<String> subclasses = indexedSubclassesByBase.computeIfAbsent(base.getQualifiedName().toString(), key -> new TreeSet<>());
            if (isIndexable(type)) {
                subclasses.add(processingEnv.getElementUtils().getBinaryName(type).toString());
            }
        }

        return false;
    }

    private void writeIndexFiles(final TypeElement marker)
    {
        var indexFiles = new SubclassIndexFiles(processingEnv.getFiler());

        // A base that this compilation saw no subclass of can still list a class that this compilation moved to another base.
        for (String baseName : knownBaseClassNames) {
            indexedSubclassesByBase.computeIfAbsent(baseName, key -> new TreeSet<>());
        }

        for (var entry : indexedSubclassesByBase.entrySet()) {
            String baseName = entry.getKey();
            String resourceName = SubclassIndexFiles.resourceName(baseName);

            try {
                var existing = indexFiles.read(resourceName);

                Set<String> subclasses = new TreeSet<>(entry.getValue());
                if (existing != null) {
                    for (String oldEntry : existing.entries()) {
                        if (!subclasses.contains(oldEntry) && stillBelongsToIndex(oldEntry, baseName, marker)) {
                            subclasses.add(oldEntry);
                        }
                    }
                }

                if (subclasses.isEmpty() && existing == null) {
                    continue;
                }

                indexFiles.write(resourceName, subclasses, existing);

            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot write the subclass index " + resourceName + ": " + e);
            }
        }
    }

    private boolean stillBelongsToIndex(final String binaryName, final String baseName, final TypeElement marker)
    {
        @Nullable TypeElement type = resolveBinaryName(binaryName);
        if (type == null || !isIndexable(type)) {
            return false;
        }

        @Nullable TypeElement base = findIndexBase(type, marker);
        return base != null && base.getQualifiedName().contentEquals(baseName);
    }

    /**
     * {@link Elements#getTypeElement} takes canonical names, the index holds binary names.
     * Replacing every {@code $} covers nested classes, trying the name unchanged covers a top-level class with a {@code $} in its name.
     */
    private @Nullable TypeElement resolveBinaryName(final String binaryName)
    {
        Elements elements = processingEnv.getElementUtils();
        for (String candidate : new String[]{binaryName.replace('$', '.'), binaryName}) {
            @Nullable TypeElement type = elements.getTypeElement(candidate);
            if (type != null && elements.getBinaryName(type).contentEquals(binaryName)) {
                return type;
            }
        }

        return null;
    }

    private @Nullable TypeElement findIndexBase(final TypeElement type, final TypeElement marker)
    {
        TypeMirror superclass = type.getSuperclass();
        while (superclass.getKind() == TypeKind.DECLARED) {
            var superElement = (TypeElement) ((DeclaredType) superclass).asElement();
            if (declaresMarker(superElement, marker)) {
                return superElement;
            }

            superclass = superElement.getSuperclass();
        }

        return null;
    }

    private boolean declaresMarker(final TypeElement type, final TypeElement marker)
    {
        for (AnnotationMirror annotation : type.getAnnotationMirrors()) {
            if (processingEnv.getTypeUtils().isSameType(annotation.getAnnotationType(), marker.asType())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isIndexable(final TypeElement type)
    {
        return type.getKind() == ElementKind.CLASS && !type.getModifiers().contains(Modifier.ABSTRACT);
    }

}
