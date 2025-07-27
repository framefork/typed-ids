package org.framefork.typedIds.common.hibernate;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.query.sqm.SqmExpressible;
import org.hibernate.query.sqm.tree.domain.SqmDomainType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.MappingContext;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.WrapperOptions;
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

import static jakarta.persistence.metamodel.Type.PersistenceType.BASIC;

@ApiStatus.Internal
@SuppressWarnings("removal") // DynamicParameterizedType usage
public abstract class ImmutableType<T, JavaTypeType extends JavaType<T>> implements
    UserType<T>,
    Type,
    EnhancedUserType<T>,
    SqmExpressible<T>,
    SqmDomainType<T>,
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
    public Class<T> getJavaType()
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
    public String getTypeName()
    {
        return getName();
    }

    @Override
    public SqmDomainType<T> getSqmType()
    {
        return this;
    }

    @Override
    public PersistenceType getPersistenceType()
    {
        return BASIC;
    }

    @Override
    public void setParameterValues(final Properties parameters)
    {
        if (javaType instanceof ParameterizedType javaTypeParametrized) {
            javaTypeParametrized.setParameterValues(parameters);
        }
    }

    @Nullable
    protected T get(final ResultSet rs, final int position, final WrapperOptions options) throws SQLException
    {
        return getJdbcType(null).getExtractor(getExpressibleJavaType()).extract(rs, position, options);
    }

    protected void set(final PreparedStatement st, @Nullable final T value, final int index, final WrapperOptions options) throws SQLException
    {
        getJdbcType(null).getBinder(getExpressibleJavaType()).bind(st, value, index, options);
    }

    @Nullable
    @Override
    public T nullSafeGet(final ResultSet rs, final int position, final WrapperOptions options) throws SQLException
    {
        return get(rs, position, options);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, @Nullable final T value, final int index, final WrapperOptions options) throws SQLException
    {
        set(st, returnedClass().cast(value), index, options);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, @Nullable final Object value, final int index, final boolean[] settable, final SharedSessionContractImplementor session) throws SQLException
    {
        set(st, returnedClass().cast(value), index, session);
    }

    @SuppressWarnings("removal")
    @Override
    public void nullSafeSet(final PreparedStatement st, @Nullable final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException
    {
        set(st, returnedClass().cast(value), index, session);
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

    @Override
    public int getColumnSpan(final MappingContext mappingContext)
    {
        return 1;
    }

    @Override
    public boolean isSame(@Nullable final Object x, @Nullable final Object y)
    {
        return Objects.equals(x, y);
    }

    @Override
    public boolean isEqual(@Nullable final Object x, @Nullable final Object y)
    {
        return Objects.equals(x, y);
    }

    @Override
    public boolean isEqual(@Nullable final Object x, @Nullable final Object y, final SessionFactoryImplementor factory)
    {
        return Objects.equals(x, y);
    }

    @Override
    public int getHashCode(final Object x)
    {
        return Objects.hashCode(x);
    }

    @Override
    public int getHashCode(final Object x, final SessionFactoryImplementor factory)
    {
        return Objects.hashCode(x);
    }

    @Override
    public int compare(@Nullable final Object x, @Nullable final Object y, final SessionFactoryImplementor sessionFactoryImplementor)
    {
        return compare(x, y);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(@Nullable final Object x, @Nullable final Object y)
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
    public final boolean isDirty(@Nullable final Object old, @Nullable final Object current, final SharedSessionContractImplementor session)
    {
        return isDirty(old, current);
    }

    @Override
    public final boolean isDirty(@Nullable final Object old, @Nullable final Object current, final boolean[] checkable, final SharedSessionContractImplementor session)
    {
        return checkable[0] && isDirty(old, current);
    }

    protected final boolean isDirty(@Nullable final Object old, @Nullable final Object current)
    {
        return !isSame(old, current);
    }

    @Override
    public boolean isModified(@Nullable final Object dbState, @Nullable final Object currentState, final boolean[] checkable, final SharedSessionContractImplementor session)
    {
        return isDirty(dbState, currentState);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public T deepCopy(@Nullable final Object value)
    {
        return (T) value;
    }

    @Nullable
    @Override
    public Object deepCopy(@Nullable final Object value, final SessionFactoryImplementor factory)
    {
        return deepCopy(value);
    }

    @Nullable
    @Override
    public Serializable disassemble(@Nullable final Object o)
    {
        return (Serializable) o;
    }

    @Nullable
    @Override
    public Serializable disassemble(@Nullable final Object value, @Nullable final SharedSessionContractImplementor session, @Nullable final Object owner)
    {
        return disassemble(value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public T assemble(@Nullable final Serializable cached, final Object owner)
    {
        return (T) cached;
    }

    @Nullable
    @Override
    public Object assemble(@Nullable final Serializable cached, final SharedSessionContractImplementor session, final Object owner)
    {
        return assemble(cached, session);
    }

    @SuppressWarnings("removal")
    @Override
    public void beforeAssemble(final Serializable cached, final SharedSessionContractImplementor session)
    {

    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public T replace(@Nullable final Object o, @Nullable final Object target, @Nullable final Object owner)
    {
        return (T) o;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public Object replace(@Nullable final Object original, @Nullable final Object target, final SharedSessionContractImplementor session, @Nullable final Object owner, final Map copyCache)
    {
        return replace(original, target, owner);
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public Object replace(
        @Nullable final Object original,
        @Nullable final Object target,
        final SharedSessionContractImplementor session,
        @Nullable final Object owner,
        final Map copyCache,
        final ForeignKeyDirection foreignKeyDirection
    )
    {
        return replace(original, target, owner);
    }

    @Override
    public boolean[] toColumnNullness(@Nullable final Object value, final MappingContext mappingContext)
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

    @Override
    public int[] getSqlTypeCodes(final MappingContext mappingContext)
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
