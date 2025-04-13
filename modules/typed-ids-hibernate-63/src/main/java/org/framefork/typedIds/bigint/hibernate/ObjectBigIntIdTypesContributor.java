package org.framefork.typedIds.bigint.hibernate;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.TypedIdsRegistry;
import org.framefork.typedIds.common.ReflectionHacks;
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

    private static final boolean CLASS_INDEX_PRESENT = ReflectionHacks.classExists("org.atteo.classindex.ClassIndex");

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
        if (!CLASS_INDEX_PRESENT) {
            return;
        }

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
