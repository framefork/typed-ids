package org.framefork.typedIds.bigint.hibernate.id;

import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.framefork.typedIds.common.ReflectionHacks;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.jdbc.mutation.JdbcValueBindings;
import org.hibernate.engine.jdbc.mutation.group.PreparedStatementDetails;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.insert.Binder;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.BasicEntityIdentifierMapping;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.model.ast.builder.TableInsertBuilder;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.spi.JavaTypeBasicAdaptor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.internal.ImmutableNamedBasicTypeImpl;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.Objects;
import java.util.Properties;

/**
 * This class handles making Hibernate think it's generating a primitive long type instead of a custom type,
 * and once Hibernate internals do their job, it then handles wrapping the generated long value in an ObjectBigIntId.
 */
public class ObjectBigIntIdIdentityGenerator extends IdentityGenerator
{

    @Nullable
    private ObjectBigIntIdType objectBigIntIdType;
    @Nullable
    private ImmutableNamedBasicTypeImpl<Long> primitiveType;

    @Override
    public void configure(final Type type, final Properties parameters, final ServiceRegistry serviceRegistry)
    {
        this.objectBigIntIdType = toObjectBigIntIdType(type);
        this.primitiveType = new ImmutableNamedBasicTypeImpl<>(
            new JavaTypeBasicAdaptor<Long>(Long.class),
            toJdbcType(objectBigIntIdType),
            "bigint"
        );
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

    @Override
    public InsertGeneratedIdentifierDelegate getGeneratedIdentifierDelegate(final PostInsertIdentityPersister persister)
    {
        var persisterProxy = proxyPersister(persister);

        var originalDelegate = super.getGeneratedIdentifierDelegate(persisterProxy);

        return new InsertGeneratedIdentifierDelegateWrapper(originalDelegate);
    }

    private PostInsertIdentityPersister proxyPersister(final PostInsertIdentityPersister persister)
    {
        return (PostInsertIdentityPersister) Proxy.newProxyInstance(
            persister.getClass().getClassLoader(),
            ReflectionHacks.getAllInterfaces(persister),
            new InvocationHandler()
            {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if ("getIdentifierType".equals(method.getName()) && (args == null || args.length == 0)) {
                        // Override the getIdentifierType method
                        // Return a primitive long type instead of the custom type
                        return Objects.requireNonNull(primitiveType, "primitiveType must not be null");
                    }

                    // For all other methods, delegate to the original persister
                    return method.invoke(persister, args);
                }
            }
        );
    }

    @SuppressWarnings("deprecation")
    private final class InsertGeneratedIdentifierDelegateWrapper implements InsertGeneratedIdentifierDelegate
    {

        private final InsertGeneratedIdentifierDelegate delegate;

        public InsertGeneratedIdentifierDelegateWrapper(final InsertGeneratedIdentifierDelegate delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public Object performInsert(final String insertSQL, final SharedSessionContractImplementor session, final Binder binder)
        {
            var idType = Objects.requireNonNull(objectBigIntIdType, "objectBigIntIdType must not be null");

            return idType.wrapJdbcValue(delegate.performInsert(insertSQL, session, binder));
        }

        @Override
        public Object performInsert(final PreparedStatementDetails insertStatementDetails, final JdbcValueBindings valueBindings, final Object entity, final SharedSessionContractImplementor session)
        {
            var idType = Objects.requireNonNull(objectBigIntIdType, "objectBigIntIdType must not be null");

            return idType.wrapJdbcValue(delegate.performInsert(insertStatementDetails, valueBindings, entity, session));
        }

        @Override
        public String prepareIdentifierGeneratingInsert(final String insertSQL)
        {
            return delegate.prepareIdentifierGeneratingInsert(insertSQL);
        }

        @Override
        public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert(final SqlStringGenerationContext context)
        {
            return delegate.prepareIdentifierGeneratingInsert(context);
        }

        @Override
        public TableInsertBuilder createTableInsertBuilder(final BasicEntityIdentifierMapping identifierMapping, final Expectation expectation, final SessionFactoryImplementor sessionFactory)
        {
            return delegate.createTableInsertBuilder(identifierMapping, expectation, sessionFactory);
        }

        @Override
        public PreparedStatement prepareStatement(final String insertSql, final SharedSessionContractImplementor session)
        {
            return delegate.prepareStatement(insertSql, session);
        }

    }

}
