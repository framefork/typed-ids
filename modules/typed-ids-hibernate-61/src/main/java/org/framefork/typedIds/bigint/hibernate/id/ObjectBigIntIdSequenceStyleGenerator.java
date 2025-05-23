package org.framefork.typedIds.bigint.hibernate.id;

import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
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
    public void configure(final Type type, final Properties parameters, final ServiceRegistry serviceRegistry)
    {
        this.objectBigIntIdType = toObjectBigIntIdType(type);

        var primitiveType = new ImmutableNamedBasicTypeImpl<>(
            new JavaTypeBasicAdaptor<Long>(Long.class),
            toJdbcType(objectBigIntIdType),
            "bigint"
        );

        super.configure(primitiveType, parameters, serviceRegistry);
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
        return objectBigIntIdType.getJdbcType(null);
    }

}
