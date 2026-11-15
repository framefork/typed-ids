package org.framefork.typedIds.bigint.hibernate.id;

import org.hibernate.generator.Generator;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.mapping.GeneratorCreator;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Replaces the sequence generator of an {@link org.framefork.typedIds.bigint.ObjectBigIntId} id with an {@link ObjectBigIntIdSequenceStyleGenerator}.
 * <p>
 * Hibernate's creator configures the generator from {@code @SequenceGenerator}, {@code @GenericGenerator} or the defaults, but against the id's
 * custom type, which the sequence machinery cannot produce values for. So when the original creator yields a {@link SequenceStyleGenerator},
 * this asks the same creator once more, with the type replaced by a surrogate {@code Long}, and wraps the result. The second generator therefore gets
 * exactly the sequence name, increment, initial value and optimizer Hibernate derived for the first one. Every other generator is returned as is.
 */
public class ObjectBigIntIdGeneratorCreator implements GeneratorCreator
{

    private final GeneratorCreator originalCreator;

    public ObjectBigIntIdGeneratorCreator(final GeneratorCreator originalCreator)
    {
        this.originalCreator = Objects.requireNonNull(originalCreator, "originalCreator must not be null");
    }

    @Override
    public Generator createGenerator(final GeneratorCreationContext context)
    {
        var originalGenerator = originalCreator.createGenerator(context);
        var originalSequenceGenerator = sequenceStyleGenerator(originalGenerator);
        if (originalSequenceGenerator instanceof ObjectBigIntIdSequenceStyleGenerator) {
            // the mapping names this generator itself, so Hibernate instantiated it and configured it with the mapping's parameters already
            return originalGenerator;
        }
        if (originalSequenceGenerator == null) {
            // For any other generator type, return the original
            // Note: the IdentityGenerator seems to be capable of handling custom types now
            return originalGenerator;
        }

        var objectBigIntIdType = ObjectBigIntIdSequenceStyleGenerator.toObjectBigIntIdType(context.getType());
        var surrogateGenerator = originalCreator.createGenerator(ObjectBigIntIdSequenceStyleGenerator.surrogateTypeContext(context));
        var delegate = sequenceStyleGenerator(surrogateGenerator);
        if (delegate == null) {
            throw new IllegalStateException("Expected the generator creator to create a %s again, but it created %s".formatted(SequenceStyleGenerator.class.getName(), surrogateGenerator));
        }

        return new ObjectBigIntIdSequenceStyleGenerator(delegate, objectBigIntIdType);
    }

    @Nullable
    private static SequenceStyleGenerator sequenceStyleGenerator(final Generator generator)
    {
        return generator instanceof SequenceStyleGenerator sequenceStyleGenerator ? sequenceStyleGenerator : null;
    }

    @Override
    public boolean isAssigned()
    {
        return originalCreator.isAssigned();
    }

}
