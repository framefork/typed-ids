package org.framefork.typedIds.springData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest(properties = {
    "spring.main.banner-mode=off",
    "spring.application.name=postgres-test",
    "spring.liquibase.enabled=false",
    "spring.datasource.hikari.minimum-idle=0",
    "spring.jpa.hibernate.ddl-auto=create-drop",
})
@AutoConfigureTestEntityManager
@ContextConfiguration(classes = {
    SpringDataTestConfiguration.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractSpringDataPostgreSQLTest
{

    @Autowired
    private TestEntityManager testEntityManager;

    /**
     * Force-check DB constraints before transaction is rolled back
     */
    @AfterEach
    void flushAfterEach()
    {
        testEntityManager.flush();
    }

}
