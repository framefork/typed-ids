package org.framefork.typedIds.bigint.hibernate;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.TypedIdsRegistry;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

@SuppressWarnings("rawtypes")
@AutoService(TypeContributor.class)
public class ObjectBigIntIdTypesContributor implements TypeContributor
{

    @Override
    public void contribute(
        final TypeContributions typeContributions,
        final ServiceRegistry serviceRegistry
    )
    {
        contributeIndexedTypes(typeContributions);
    }

    private void contributeIndexedTypes(final TypeContributions typeContributions)
    {
        TypeConfiguration typeConfiguration = typeContributions.getTypeConfiguration();
        JdbcType bigintJdbcType = typeConfiguration.getJdbcTypeRegistry().getDescriptor(SqlTypes.BIGINT);

        var idTypes = TypedIdsRegistry.getObjectBigIntIdClasses();
        for (var idType : idTypes) {
            var objectBigIntIdType = new ObjectBigIntIdType(idType, bigintJdbcType);

            typeContributions.contributeType(objectBigIntIdType);

            // todo: array type
        }
    }

}
