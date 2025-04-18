package org.framefork.typedIds.bigint.hibernate;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdSequenceStyleGenerator;
import org.hibernate.boot.ResourceStreamLocator;
import org.hibernate.boot.spi.AdditionalMappingContributions;
import org.hibernate.boot.spi.AdditionalMappingContributor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;

@AutoService(AdditionalMappingContributor.class)
public class ObjectBigIntIdTypeGenerationMetadataContributor implements AdditionalMappingContributor
{

    @Override
    public String getContributorName()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public void contribute(
        final AdditionalMappingContributions contributions,
        final InFlightMetadataCollector metadata,
        final ResourceStreamLocator resourceStreamLocator,
        final MetadataBuildingContext buildingContext
    )
    {
        // TODO: handle mapped superclass?

        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            var identifier = entityBinding.getIdentifier();
            if (identifier instanceof SimpleValue simpleValueIdentifier) {
                var identifierClass = simpleValueIdentifier.getType().getReturnedClass();
                if (!ObjectBigIntId.class.isAssignableFrom(identifierClass)) {
                    continue;
                }

                remapIdentifierGeneratorStrategy(simpleValueIdentifier);
            }
        }
    }

    private void remapIdentifierGeneratorStrategy(final SimpleValue identifier)
    {
        var newIdentifierGeneratorStrategy = switch (identifier.getIdentifierGeneratorStrategy()) {
            case "org.hibernate.id.enhanced.SequenceStyleGenerator" -> ObjectBigIntIdSequenceStyleGenerator.class.getName();
            default -> identifier.getIdentifierGeneratorStrategy();
        };

        identifier.setIdentifierGeneratorStrategy(newIdentifierGeneratorStrategy);
    }

}
