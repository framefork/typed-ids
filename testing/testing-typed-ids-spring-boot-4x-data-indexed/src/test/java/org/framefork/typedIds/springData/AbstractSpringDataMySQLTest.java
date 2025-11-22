package org.framefork.typedIds.springData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest(properties = {
    "spring.main.banner-mode=off",
    "spring.application.name=mysql-test",
    "spring.liquibase.enabled=false",
    "spring.datasource.hikari.minimum-idle=0",
    "spring.jpa.hibernate.ddl-auto=create-drop",
})
@AutoConfigureTestEntityManager
@ContextConfiguration(classes = {
    SpringDataTestConfiguration.class,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractSpringDataMySQLTest
{

    @Autowired
    private TestEntityManager testEntityManager;

    /**
     * Force-check DB constraints before transaction is rolled back
     */
    @AfterEach
    void flushAfterEach()
    {
        flush();
    }

    protected void flush()
    {
        testEntityManager.flush();
    }

    protected void flushAndClear()
    {
        testEntityManager.flush();
        testEntityManager.clear();
    }

}
