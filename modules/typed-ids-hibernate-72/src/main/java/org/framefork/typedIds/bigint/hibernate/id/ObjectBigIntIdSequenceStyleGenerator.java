package org.framefork.typedIds.bigint.hibernate.id;

import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.framefork.typedIds.common.hibernate.TypeOverrideGeneratorCreationContext;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.spi.JavaTypeBasicAdaptor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.internal.ImmutableNamedBasicTypeImpl;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Properties;

/**
 * This class handles making Hibernate think it's generating a primitive long type instead of a custom type,
 * and once Hibernate internals do their job, it then handles wrapping the generated long value in an ObjectBigIntId.
 */
public class ObjectBigIntIdSequenceStyleGenerator extends SequenceStyleGenerator
{

    @Nullable
    private ObjectBigIntIdType objectBigIntIdType;

    @Override
    public Object generate(final SharedSessionContractImplementor session, final Object object)
    {
        var idType = Objects.requireNonNull(objectBigIntIdType, "objectBigIntIdType must not be null");
        return idType.wrapJdbcValue(super.generate(session, object));
    }

    @Override
    public void configure(final GeneratorCreationContext creationContext, final Properties parameters)
    {
        this.objectBigIntIdType = toObjectBigIntIdType(creationContext.getType());

        var primitiveType = new ImmutableNamedBasicTypeImpl<>(
            new JavaTypeBasicAdaptor<>(Long.class),
            toJdbcType(objectBigIntIdType),
            "bigint"
        );

        super.configure(new TypeOverrideGeneratorCreationContext(creationContext, primitiveType), parameters);
    }

    @Override
    public void initialize(final SqlStringGenerationContext context)
    {
        // Initialize the parent to set up the database structure
        super.initialize(context);
    }

    private ObjectBigIntIdType toObjectBigIntIdType(final Type type)
    {
        if (type instanceof CustomType<?> customType) {
            var userType = customType.getUserType();
            if (userType instanceof ObjectBigIntIdType objectBigIntIdType) {
                return objectBigIntIdType;
            }
        }

        throw new HibernateException("The given type is expected to be a CustomType wrapper over a %s, but was '%s' instead".formatted(ObjectBigIntIdType.class.getSimpleName(), type));
    }

    private JdbcType toJdbcType(final ObjectBigIntIdType objectBigIntIdType)
    {
        return objectBigIntIdType.getJdbcType();
    }

}
