package org.framefork.typedIds.swagger;

import com.google.auto.service.AutoService;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import org.framefork.typedIds.TypedId;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

@AutoService(ModelConverter.class)
public class TypedIdsModelConverter implements ModelConverter
{

    public static final String IDS_AS_REFS_PROPERTY_NAME = "framefork.typed-ids.openapi.as-ref";

    /**
     * Allows all VO-IDs to be resolved as a reference to a scheme added to the components section.
     * <p/>
     * This is not a very good way to configure something, but it is consistent with {@link ModelResolver#enumsAsRef}
     */
    @SuppressWarnings("NonFinalStaticField")
    public static boolean idsAsRef = Objects.equals(System.getProperty(IDS_AS_REFS_PROPERTY_NAME, "true"), "true");

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    public Schema resolve(final AnnotatedType type, final ModelConverterContext context, final Iterator<ModelConverter> chain)
    {
        final Class<?> rawClass = TypedIdsSchemaUtils.rawClassOf(type.getType());
        if (rawClass != null && TypedId.class.isAssignableFrom(rawClass)) {
            var schema = TypedIdsSchemaUtils.createSchema(rawClass);

            if (idsAsRef) {
                var schemaRef = new Schema().$ref(Components.COMPONENTS_SCHEMAS_REF + schema.getName());
                context.defineModel(schema.getName(), schema, rawClass, null);
                return schemaRef;

            } else {
                return schema.name(null);
            }
        }

        return chain.hasNext() ? chain.next().resolve(type, context, chain) : null;
    }

}
