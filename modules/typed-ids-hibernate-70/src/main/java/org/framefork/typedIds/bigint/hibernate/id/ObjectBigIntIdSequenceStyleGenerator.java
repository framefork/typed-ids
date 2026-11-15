package org.framefork.typedIds.bigint.hibernate.id;

import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.framefork.typedIds.common.hibernate.TypeOverrideGeneratorCreationContext;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.DatabaseStructure;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.spi.JavaTypeBasicAdaptor;
import org.hibernate.type.internal.ImmutableNamedBasicTypeImpl;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Properties;

/**
 * Generates {@link org.framefork.typedIds.bigint.ObjectBigIntId} ids from a sequence (or the table Hibernate falls back to without sequence support).
 * <p>
 * The sequence machinery only produces integral values, so the actual work is done by a plain {@link SequenceStyleGenerator} configured against
 * a surrogate {@code Long} type, and this class wraps every generated value in the id type. {@link ObjectBigIntIdGeneratorCreator} hands it the delegate
 * that Hibernate's own generator creator configured, so the sequence name, increment, initial value and optimizer of {@code @SequenceGenerator} or
 * {@code @GenericGenerator} apply. When Hibernate instantiates this class itself, {@link #configure} configures the delegate from the given parameters.
 * <p>
 * It extends {@link SequenceStyleGenerator} so that Hibernate keeps treating it as one, but {@code super} is never configured: the delegate is a plain
 * {@link SequenceStyleGenerator}, so overriding its protected hooks ({@code determineSequenceName}, {@code buildDatabaseStructure}, ...) in a subclass
 * of this class has no effect.
 */
public class ObjectBigIntIdSequenceStyleGenerator extends SequenceStyleGenerator
{

    @Nullable
    private ObjectBigIntIdType objectBigIntIdType;

    @Nullable
    private SequenceStyleGenerator delegate;

    public ObjectBigIntIdSequenceStyleGenerator()
    {
    }

    /**
     * Wraps a generator that Hibernate already configured against {@link #surrogateTypeContext(GeneratorCreationContext)}.
     */
    public ObjectBigIntIdSequenceStyleGenerator(final SequenceStyleGenerator delegate, final ObjectBigIntIdType objectBigIntIdType)
    {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.objectBigIntIdType = Objects.requireNonNull(objectBigIntIdType, "objectBigIntIdType must not be null");
    }

    /**
     * Returns the creation context with the id's {@link ObjectBigIntIdType} replaced by a surrogate {@code Long} type with the same JDBC type,
     * which a plain {@link SequenceStyleGenerator} can be configured against.
     */
    public static GeneratorCreationContext surrogateTypeContext(final GeneratorCreationContext creationContext)
    {
        var objectBigIntIdType = toObjectBigIntIdType(creationContext.getType());
        var surrogateType = new ImmutableNamedBasicTypeImpl<>(
            new JavaTypeBasicAdaptor<>(Long.class),
            objectBigIntIdType.getJdbcType(null),
            "bigint"
        );
        return new TypeOverrideGeneratorCreationContext(creationContext, surrogateType);
    }

    public static ObjectBigIntIdType toObjectBigIntIdType(final Type type)
    {
        if (type instanceof CustomType<?> customType && customType.getUserType() instanceof ObjectBigIntIdType objectBigIntIdType) {
            return objectBigIntIdType;
        }

        throw new HibernateException("The given type is expected to be a CustomType wrapper over a %s, but was '%s' instead".formatted(ObjectBigIntIdType.class.getSimpleName(), type));
    }

    @Override
    public void configure(final GeneratorCreationContext creationContext, final Properties parameters)
    {
        this.objectBigIntIdType = toObjectBigIntIdType(creationContext.getType());

        var newDelegate = new SequenceStyleGenerator();
        newDelegate.configure(surrogateTypeContext(creationContext), parameters);
        this.delegate = newDelegate;
    }

    @Override
    public Object generate(final SharedSessionContractImplementor session, final Object object)
    {
        var idType = Objects.requireNonNull(objectBigIntIdType, "objectBigIntIdType must not be null");
        return idType.wrapJdbcValue(delegate().generate(session, object));
    }

    @Override
    public void registerExportables(final Database database)
    {
        delegate().registerExportables(database);
    }

    @Override
    public void initialize(final SqlStringGenerationContext context)
    {
        delegate().initialize(context);
    }

    @Override
    public DatabaseStructure getDatabaseStructure()
    {
        return delegate().getDatabaseStructure();
    }

    @Override
    public Optimizer getOptimizer()
    {
        return delegate().getOptimizer();
    }

    @Override
    public Type getIdentifierType()
    {
        return delegate().getIdentifierType();
    }

    @Override
    public boolean supportsBulkInsertionIdentifierGeneration()
    {
        return delegate().supportsBulkInsertionIdentifierGeneration();
    }

    @Override
    public String determineBulkInsertionIdentifierGenerationSelectFragment(final SqlStringGenerationContext context)
    {
        return delegate().determineBulkInsertionIdentifierGenerationSelectFragment(context);
    }

    private SequenceStyleGenerator delegate()
    {
        return Objects.requireNonNull(delegate, "the generator must be configured before use");
    }

}
