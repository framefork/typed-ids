package org.framefork.typedIds.hibernate.tests;

import io.hypersistence.utils.test.providers.DataSourceProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractContainerDataSourceProvider implements DataSourceProvider
{

    private final AtomicReference<JdbcDatabaseContainer<?>> container = new AtomicReference<>();

    public JdbcDatabaseContainer<?> getContainer()
    {
        if (container.get() == null) {
            synchronized (container) {
                if (container.get() == null) {
                    container.set(initContainer());
                }
            }
        }

        return container.get();
    }

    private JdbcDatabaseContainer<?> initContainer()
    {
        var newContainer = (JdbcDatabaseContainer<?>) newJdbcDatabaseContainer();

        if (supportsDatabaseName()) {
            newContainer.withDatabaseName(databaseName());
        }
        if (supportsCredentials()) {
            newContainer.withUsername(username()).withPassword(password());
        }

        newContainer.withReuse(true).start();

        return newContainer;
    }

    @Override
    public final DataSource dataSource()
    {
        getContainer(); // force init
        return newDataSource();
    }

    @Override
    public final String url()
    {
        return getContainer().getJdbcUrl();
    }

    public String databaseName()
    {
        return "framefork";
    }

    protected abstract DataSource newDataSource();

}
