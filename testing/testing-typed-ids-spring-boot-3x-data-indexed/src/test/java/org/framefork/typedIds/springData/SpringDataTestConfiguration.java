package org.framefork.typedIds.springData;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "org.framefork.typedIds"
})
@EntityScan(basePackages = {
    "org.framefork.typedIds"
})
@EnableJpaRepositories(basePackages = {
    "org.framefork.typedIds"
})
@SuppressWarnings("PrivateConstructorForUtilityClass")
public class SpringDataTestConfiguration
{

    @ConditionalOnProperty(name = "spring.application.name", havingValue = "mysql-test")
    @Bean
    @ServiceConnection
    public static MySQLContainer<?> mySQLContainer()
    {
        return new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa")
            .waitingFor(new HostPortWaitStrategy());
    }

    @ConditionalOnProperty(name = "spring.application.name", havingValue = "postgres-test")
    @Bean
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer()
    {
        return new PostgreSQLContainer<>("postgres:16.4")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa")
            .withEnv("TZ", "UTC")
            .waitingFor(new HostPortWaitStrategy());
    }

}
