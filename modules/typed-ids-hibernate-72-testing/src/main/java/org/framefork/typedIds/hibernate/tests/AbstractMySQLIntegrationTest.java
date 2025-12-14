package org.framefork.typedIds.hibernate.tests;

import io.hypersistence.utils.test.AbstractHibernateTest;
import io.hypersistence.utils.test.providers.DataSourceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractMySQLIntegrationTest extends AbstractHibernateTest
{

    @BeforeEach
    @Override
    public void init()
    {
        super.init();
    }

    @AfterEach
    @Override
    public void destroy()
    {
        super.destroy();
    }

    @Override
    protected DataSourceProvider dataSourceProvider()
    {
        return MySQLDataSourceProvider.V8;
    }

}
