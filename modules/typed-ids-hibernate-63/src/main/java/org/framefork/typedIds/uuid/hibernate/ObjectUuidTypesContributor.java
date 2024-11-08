package org.framefork.typedIds.uuid.hibernate;

import com.google.auto.service.AutoService;
import org.atteo.classindex.ClassIndex;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.hibernate.postgresql.PostgresUUIDJdbcType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AutoService(TypeContributor.class)
public class ObjectUuidTypesContributor implements TypeContributor
{

    private static final boolean CLASS_INDEX_PRESENT;

    static {
        boolean found = false;
        try {
            Class.forName("org.atteo.classindex.ClassIndex");
            found = true;
        } catch (ClassNotFoundException ignored) {
        }

        CLASS_INDEX_PRESENT = found;
    }

    @Override
    public void contribute(
        final TypeContributions typeContributions,
        final ServiceRegistry serviceRegistry
    )
    {
        if (!CLASS_INDEX_PRESENT) {
            return;
        }

        JdbcServices jdbcServices = serviceRegistry.requireService(JdbcServices.class);
        Dialect dialect = jdbcServices.getDialect();

        var idTypes = getIndexedSubclassesFor(ObjectUuid.class);
        for (var idType : idTypes) {
            var objectUuidType = createObjectUuidTypeFor(idType, dialect);

            typeContributions.contributeType(objectUuidType);

            @SuppressWarnings("unchecked")
            var objectUuidArrayType = (ObjectUuidArrayType<?>) ObjectUuidArrayType.create(idType, objectUuidType);
            typeContributions.contributeType(objectUuidArrayType);
            typeContributions.getTypeConfiguration().getBasicTypeRegistry().register(objectUuidArrayType, objectUuidArrayType.getRegistrationKeys());
        }
    }

    private ObjectUuidType createObjectUuidTypeFor(final Class<?> idType, final Dialect dialect)
    {
        if (dialect instanceof PostgreSQLDialect) {
            return new ObjectUuidType(idType, PostgresUUIDJdbcType.INSTANCE);

        } else {
            return new ObjectUuidType(idType);
        }
    }

    private <T> List<Class<? extends T>> getIndexedSubclassesFor(final Class<T> typeClass)
    {
        Iterable<Class<? extends T>> subclasses = ClassIndex.getSubclasses(typeClass);

        List<Class<? extends T>> result = new ArrayList<>();
        subclasses.forEach(result::add);
        result.sort(Comparator.comparing(Class::getName));

        return result;
    }

}
