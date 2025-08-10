package org.framefork.typedIds.swagger;

import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
class TypedIdsSchemaUtilsTest
{

    @Test
    void testDefaultSchema()
    {
        var noSchemaActual = TypedIdsSchemaUtils.getDefaultSchema(NoSchema.class);
        assertThat(noSchemaActual)
            .extracting(Schema::getName)
            .isEqualTo("TypedIdsSchemaUtilsTest_NoSchema");

        var schemaNoNameActual = TypedIdsSchemaUtils.getDefaultSchema(SchemaNoName.class);
        assertThat(schemaNoNameActual)
            .extracting(Schema::getName)
            .isEqualTo("TypedIdsSchemaUtilsTest_SchemaNoName");

        var schemaNameSameAsClassSimpleNameActual = TypedIdsSchemaUtils.getDefaultSchema(SchemaNameSameAsClassSimpleName.class);
        assertThat(schemaNameSameAsClassSimpleNameActual)
            .extracting(Schema::getName)
            .isEqualTo("SchemaNameSameAsClassSimpleName");

        var schemaNameCustomActual = TypedIdsSchemaUtils.getDefaultSchema(SchemaNameCustom.class);
        assertThat(schemaNameCustomActual)
            .extracting(Schema::getName)
            .isEqualTo("CustomTypeName");
    }

    record NoSchema(String value)
    {

    }

    @io.swagger.v3.oas.annotations.media.Schema()
    record SchemaNoName(String value)
    {

    }

    @io.swagger.v3.oas.annotations.media.Schema(name = "SchemaNameSameAsClassSimpleName")
    record SchemaNameSameAsClassSimpleName(String value)
    {

    }

    @io.swagger.v3.oas.annotations.media.Schema(name = "CustomTypeName")
    record SchemaNameCustom(String value)
    {

    }

}
