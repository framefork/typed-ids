package org.framefork.typedIds.uuid.hibernate;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.TypedIdsRegistry;
import org.framefork.typedIds.uuid.hibernate.jdbc.BinaryUuidJdbcType;
import org.framefork.typedIds.uuid.hibernate.jdbc.NativeUuidJdbcType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;

@SuppressWarnings("rawtypes")
@AutoService(TypeContributor.class)
public class ObjectUuidTypesContributor implements TypeContributor
{

    /**
     * <a href="https://mariadb.com/kb/en/uuid-data-type/">https://mariadb.com/kb/en/uuid-data-type/</a>
     */
    private static final DatabaseVersion MARIADB_NATIVE_UUID_SINCE = DatabaseVersion.make(10, 7);

    @Override
    public void contribute(
        final TypeContributions typeContributions,
        final ServiceRegistry serviceRegistry
    )
    {
        remapUuidType(typeContributions, serviceRegistry);
        contributeIndexedTypes(typeContributions);
    }

    private void remapUuidType(final TypeContributions typeContributions, final ServiceRegistry serviceRegistry)
    {
        JdbcServices jdbcServices = serviceRegistry.requireService(JdbcServices.class);
        Dialect dialect = jdbcServices.getDialect();

        DdlTypeRegistry ddlTypeRegistry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();
        JdbcTypeRegistry jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        if (!jdbcTypeRegistry.hasRegisteredDescriptor(SqlTypes.UUID)) {
            if (dialect instanceof MariaDBDialect && dialect.getVersion().isSameOrAfter(ObjectUuidTypesContributor.MARIADB_NATIVE_UUID_SINCE)) {
                typeContributions.contributeJdbcType(NativeUuidJdbcType.INSTANCE);

            } else if (dialect instanceof MySQLDialect) {
                typeContributions.contributeJdbcType(BinaryUuidJdbcType.INSTANCE);
                ddlTypeRegistry.addDescriptorIfAbsent(new DdlTypeImpl(SqlTypes.UUID, "binary(16)", dialect));

            } else {
                typeContributions.contributeJdbcType(NativeUuidJdbcType.INSTANCE);
            }
        }
    }

    private void contributeIndexedTypes(final TypeContributions typeContributions)
    {
        TypeConfiguration typeConfiguration = typeContributions.getTypeConfiguration();
        JdbcType uuidJdbcType = typeConfiguration.getJdbcTypeRegistry().getDescriptor(SqlTypes.UUID);

        var idTypes = TypedIdsRegistry.getObjectUuidClasses();
        for (var idType : idTypes) {
            var objectUuidType = new ObjectUuidType(idType, uuidJdbcType);

            typeContributions.contributeType(objectUuidType);
        }
    }

}
