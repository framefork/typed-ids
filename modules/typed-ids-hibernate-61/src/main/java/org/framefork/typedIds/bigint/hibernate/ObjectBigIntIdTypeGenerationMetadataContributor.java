package org.framefork.typedIds.bigint.hibernate;

import com.google.auto.service.AutoService;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdIdentityGenerator;
import org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdSequenceStyleGenerator;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.cfg.SecondPass;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.jboss.jandex.IndexView;

import java.util.Map;

@AutoService(MetadataContributor.class)
public class ObjectBigIntIdTypeGenerationMetadataContributor implements MetadataContributor
{

    @Override
    public void contribute(final InFlightMetadataCollector metadata, final IndexView jandexIndex)
    {
        // TODO: handle mapped superclass?

        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            var identifier = entityBinding.getIdentifier();

            if (identifier instanceof BasicValue basicValueIdentifier) {
                if (basicValueIdentifier.getResolution() != null) {
                    remapIdentifierGeneratorStrategy(identifier);
                } else {
                    metadata.addSecondPass(new RemapIdentifierGeneratorStrategySecondPass(identifier));
                }

            } else {
                metadata.addSecondPass(new RemapIdentifierGeneratorStrategySecondPass(identifier));
            }
        }
    }

    private void remapIdentifierGeneratorStrategy(final KeyValue identifier)
    {
        if (!(identifier instanceof SimpleValue simpleValueIdentifier)) {
            return;
        }

        var identifierClass = simpleValueIdentifier.getType().getReturnedClass();
        if (!ObjectBigIntId.class.isAssignableFrom(identifierClass)) {
            return;
        }

        var newIdentifierGeneratorStrategy = switch (simpleValueIdentifier.getIdentifierGeneratorStrategy()) {
            case "org.hibernate.id.enhanced.SequenceStyleGenerator" -> ObjectBigIntIdSequenceStyleGenerator.class.getName();
            case "org.hibernate.id.IdentityGenerator" -> ObjectBigIntIdIdentityGenerator.class.getName();
            case "identity" -> ObjectBigIntIdIdentityGenerator.class.getName();
            default -> simpleValueIdentifier.getIdentifierGeneratorStrategy();
        };

        simpleValueIdentifier.setIdentifierGeneratorStrategy(newIdentifierGeneratorStrategy);
    }

    private final class RemapIdentifierGeneratorStrategySecondPass implements SecondPass
    {

        private final KeyValue identifier;

        public RemapIdentifierGeneratorStrategySecondPass(final KeyValue identifier)
        {
            this.identifier = identifier;
        }

        @Override
        public void doSecondPass(final Map<String, PersistentClass> persistentClasses)
        {
            remapIdentifierGeneratorStrategy(identifier);
        }

    }

}
