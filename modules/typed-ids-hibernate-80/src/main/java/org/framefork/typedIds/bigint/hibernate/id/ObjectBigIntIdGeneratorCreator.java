package org.framefork.typedIds.bigint.hibernate.id;

import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.boot.model.internal.GeneratorAnnotationHelper;
import org.hibernate.generator.Generator;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.GenericGeneratorGeneration;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.mapping.GeneratorCreator;

import java.util.Objects;

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

        if (isSequenceStyleGenerator(originalGenerator)) {
            return prepareForUse(new ObjectBigIntIdSequenceStyleGenerator(), context);

        } else {
            // For any other generator type, return the original
            // Note: the IdentityGenerator seems to be capable of  handling custom types now
            return originalGenerator;
        }
    }

    /**
     * Since Hibernate 8.0, {@code @GenericGenerator(type = SequenceStyleGenerator.class)} is an {@code @IdGeneratorType} annotation,
     * and its creator returns a {@link GenericGeneratorGeneration} that wraps the actual generator.
     */
    private static boolean isSequenceStyleGenerator(final Generator generator)
    {
        var actualGenerator = generator instanceof GenericGeneratorGeneration genericGeneration ? genericGeneration.getDelegate() : generator;
        return actualGenerator instanceof SequenceStyleGenerator;
    }

    private static <T extends Generator> T prepareForUse(final T generator, final GeneratorCreationContext context)
    {
        GeneratorAnnotationHelper.<IdGeneratorType>prepareForUse(generator, null, null, null, null, context);
        return generator;
    }

    @Override
    public boolean isAssigned()
    {
        return originalCreator.isAssigned();
    }

}
