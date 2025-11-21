package org.framefork.typedIds.spring.convert.boot;

import org.framefork.typedIds.spring.convert.TestIds;
import org.framefork.typedIds.spring.convert.TestIds.BigIntId;
import org.framefork.typedIds.spring.convert.TestIds.UuidId;
import org.framefork.typedIds.spring.convert.TypedIdsGenericConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConverterNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

final class TypedIdsConvertersAutoConfigurationTest
{

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TypedIdsConvertersAutoConfiguration.class));

    @Test
    void registersTheConverter()
    {
        contextRunner.run(context -> assertThat(context).hasSingleBean(TypedIdsGenericConverter.class));
    }

    @Test
    void backsOffWhenTheApplicationDefinesItsOwn()
    {
        contextRunner
            .withUserConfiguration(OwnConverterConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(TypedIdsGenericConverter.class);
                assertThat(context).hasBean("ownTypedIdsGenericConverter");
            });
    }

    @Test
    void bindsTypedIdsInConfigurationProperties()
    {
        contextRunner
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues("test.uuid-id=" + TestIds.UUID_V7, "test.big-int-id=42")
            .run(context -> {
                var properties = context.getBean(TestProperties.class);
                assertThat(properties.getUuidId()).isEqualTo(UuidId.parse(TestIds.UUID_V7));
                assertThat(properties.getBigIntId()).isEqualTo(BigIntId.create(42L));
            });
    }

    @Test
    void configurationPropertiesDoNotBindWithoutTheAutoConfiguration()
    {
        new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues("test.uuid-id=" + TestIds.UUID_V7, "test.big-int-id=42")
            .run(context -> assertThat(context).getFailure().hasRootCauseInstanceOf(ConverterNotFoundException.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class OwnConverterConfiguration
    {

        @Bean
        TypedIdsGenericConverter ownTypedIdsGenericConverter()
        {
            return new TypedIdsGenericConverter();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(TestProperties.class)
    static class PropertiesConfiguration
    {

    }

    @ConfigurationProperties("test")
    static class TestProperties
    {

        private UuidId uuidId;
        private BigIntId bigIntId;

        public UuidId getUuidId()
        {
            return uuidId;
        }

        public void setUuidId(final UuidId uuidId)
        {
            this.uuidId = uuidId;
        }

        public BigIntId getBigIntId()
        {
            return bigIntId;
        }

        public void setBigIntId(final BigIntId bigIntId)
        {
            this.bigIntId = bigIntId;
        }

    }

}
