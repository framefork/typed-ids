package org.framefork.typedIds.common.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.model.domain.DomainType;
import org.hibernate.query.BindableType;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.IncomparableComparator;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlanExposer;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.spi.TypeConfigurationAware;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@ApiStatus.Internal
public abstract class ImmutableType<T, JavaTypeType extends JavaType<T>> implements
    UserType<T>,
    Type,
    EnhancedUserType<T>,
    BindableType<T>,
    SqmExpressible<T>,
    DomainType<T>,
    MutabilityPlanExposer<T>,
    DynamicParameterizedType,
    TypeConfigurationAware
{

    private final JavaTypeType javaType;
    @Nullable
    private JdbcType jdbcType;

    @Nullable
    private TypeConfiguration typeConfiguration;

    protected ImmutableType(
        final JavaTypeType javaType,
        @Nullable final JdbcType jdbcType
    )
    {
        this.javaType = Objects.requireNonNull(javaType, "javaType must not be null");
        this.jdbcType = jdbcType;
    }

    @Override
    public void setTypeConfiguration(final TypeConfiguration typeConfiguration)
    {
        this.typeConfiguration = typeConfiguration;
        getJdbcType(null); // initialize
    }

    @Nullable
    @Override
    public TypeConfiguration getTypeConfiguration()
    {
        return typeConfiguration;
    }

    @Override
    public JavaTypeType getExpressibleJavaType()
    {
        return javaType;
    }

    @Override
    public Class<T> getBindableJavaType()
    {
        return returnedClass();
    }

    @Override
    public JdbcType getJdbcType(@Nullable final TypeConfiguration typeConfiguration)
    {
        if (jdbcType == null && getTypeConfiguration() != null) {
            jdbcType = getTypeConfiguration().getJdbcTypeRegistry().getDescriptor(getSqlType());
        }

        return Objects.requireNonNull(jdbcType, "jdbcType must not be null");
    }

    @Override
    public Class<T> returnedClass()
    {
        return javaType.getJavaTypeClass();
    }

    @Override
    public Class<T> getReturnedClass()
    {
        return returnedClass();
    }

    @Override
    public String getName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public void setParameterValues(final Properties parameters)
    {
        if (javaType instanceof ParameterizedType javaTypeParametrized) {
            javaTypeParametrized.setParameterValues(parameters);
        }
    }

    @Nullable
    protected T get(final ResultSet rs, final int position, final SharedSessionContractImplementor session, final Object owner) throws SQLException
    {
        return getJdbcType(null).getExtractor(getExpressibleJavaType()).extract(rs, position, session);
    }

    protected void set(final PreparedStatement st, @Nullable final T value, final int index, final SharedSessionContractImplementor session) throws SQLException
    {
        getJdbcType(null).getBinder(getExpressibleJavaType()).bind(st, value, index, session);
    }

    @Nullable
    @Override
    public T nullSafeGet(final ResultSet rs, final int position, final SharedSessionContractImplementor session, final Object owner) throws SQLException
    {
        return get(rs, position, session, owner);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException
    {
        set(st, returnedClass().cast(value), index, session);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final boolean[] settable, final SharedSessionContractImplementor session) throws SQLException
    {
        set(st, returnedClass().cast(value), index, session);
    }

    @Override
    public boolean equals(final Object x, final Object y)
    {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final Object x)
    {
        return x.hashCode();
    }

    @Override
    public boolean isMutable()
    {
        return false;
    }

    @Override
    public MutabilityPlan<T> getExposedMutabilityPlan()
    {
        return ImmutableMutabilityPlan.instance();
    }

    @Override
    public boolean isAssociationType()
    {
        return false;
    }

    @Override
    public boolean isCollectionType()
    {
        return false;
    }

    @Override
    public boolean isEntityType()
    {
        return false;
    }

    @Override
    public boolean isAnyType()
    {
        return false;
    }

    @Override
    public boolean isComponentType()
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getColumnSpan(final Mapping mapping)
    {
        return 1;
    }

    @Override
    public boolean isSame(final Object x, final Object y)
    {
        return equals(x, y);
    }

    @Override
    public boolean isEqual(final Object x, final Object y)
    {
        return equals(x, y);
    }

    @Override
    public boolean isEqual(final Object x, final Object y, final SessionFactoryImplementor factory)
    {
        return equals(x, y);
    }

    @Override
    public int getHashCode(final Object x)
    {
        return hashCode(x);
    }

    @Override
    public int getHashCode(final Object x, final SessionFactoryImplementor factory)
    {
        return hashCode(x);
    }

    @Override
    public int compare(final Object x, final Object y, final SessionFactoryImplementor sessionFactoryImplementor)
    {
        return compare(x, y);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(final Object x, final Object y)
    {
        if (x instanceof Comparable<?> xComparable && y instanceof Comparable<?> yComparable) {
            return ((Comparator<Object>) getComparator()).compare(xComparable, yComparable);
        }

        return IncomparableComparator.INSTANCE.compare(x, y);
    }

    @SuppressWarnings("unchecked")
    public Comparator<T> getComparator()
    {
        @Nullable Comparator<T> comparator = javaType.getComparator();
        return comparator != null ? comparator : (Comparator<T>) IncomparableComparator.INSTANCE;
    }

    @Override
    public final boolean isDirty(final Object old, final Object current, final SharedSessionContractImplementor session)
    {
        return isDirty(old, current);
    }

    @Override
    public final boolean isDirty(final Object old, final Object current, final boolean[] checkable, final SharedSessionContractImplementor session)
    {
        return checkable[0] && isDirty(old, current);
    }

    protected final boolean isDirty(final Object old, final Object current)
    {
        return !isSame(old, current);
    }

    @Override
    public boolean isModified(final Object dbState, final Object currentState, final boolean[] checkable, final SharedSessionContractImplementor session)
    {
        return isDirty(dbState, currentState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deepCopy(final Object value)
    {
        return (T) value;
    }

    @Override
    public Object deepCopy(final Object value, final SessionFactoryImplementor factory)
    {
        return deepCopy(value);
    }

    @Override
    public Serializable disassemble(final Object o)
    {
        return (Serializable) o;
    }

    @Override
    public Serializable disassemble(final Object value, final SharedSessionContractImplementor session, final Object owner)
    {
        return disassemble(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T assemble(final Serializable cached, final Object owner)
    {
        return (T) cached;
    }

    @Override
    public Object assemble(final Serializable cached, final SharedSessionContractImplementor session, final Object owner)
    {
        return assemble(cached, session);
    }

    @Override
    public void beforeAssemble(final Serializable cached, final SharedSessionContractImplementor session)
    {

    }

    @SuppressWarnings("unchecked")
    @Override
    public T replace(final Object o, final Object target, final Object owner)
    {
        return (T) o;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object replace(final Object original, final Object target, final SharedSessionContractImplementor session, final Object owner, final Map copyCache)
    {
        return replace(original, target, owner);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object replace(final Object original, final Object target, final SharedSessionContractImplementor session, final Object owner, final Map copyCache, final ForeignKeyDirection foreignKeyDirection)
    {
        return replace(original, target, owner);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean[] toColumnNullness(final Object value, final Mapping mapping)
    {
        return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
    }

    @Override
    public long getDefaultSqlLength(final Dialect dialect, final JdbcType jdbcType)
    {
        return javaType.getDefaultSqlLength(dialect, jdbcType);
    }

    @Override
    public int getDefaultSqlPrecision(final Dialect dialect, final JdbcType jdbcType)
    {
        return javaType.getDefaultSqlPrecision(dialect, jdbcType);
    }

    @Override
    public int getDefaultSqlScale(final Dialect dialect, final JdbcType jdbcType)
    {
        return javaType.getDefaultSqlScale(dialect, jdbcType);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int[] getSqlTypeCodes(final Mapping mapping)
    {
        return new int[]{getSqlType()};
    }

    @Override
    public T fromStringValue(final CharSequence sequence)
    {
        return javaType.fromString(sequence);
    }

    @Override
    public String toLoggableString(@Nullable final Object value, final SessionFactoryImplementor factory)
    {
        return String.valueOf(value);
    }

    @Nullable
    @Override
    public String toSqlLiteral(@Nullable final T o)
    {
        return (o != null) ? String.format(Locale.ROOT, "'%s'", o) : null;
    }

    @Nullable
    @Override
    public String toString(@Nullable final T o)
    {
        return (o != null) ? o.toString() : null;
    }

    @Override
    public String toString()
    {
        return "%s backed by %s".formatted(javaType, getJdbcType(null));
    }

}
