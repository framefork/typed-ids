package org.framefork.typedIds.hibernate.tests;

import org.hibernate.dialect.Database;
import org.hibernate.dialect.PostgreSQLDialect;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.Map;

public final class PostgreSQLDataSourceProvider extends AbstractContainerDataSourceProvider
{

    public static final PostgreSQLDataSourceProvider V16 = new PostgreSQLDataSourceProvider("16.4");

    private final String version;

    private PostgreSQLDataSourceProvider(final String version)
    {
        this.version = version;
    }

    @Override
    public Database database()
    {
        return Database.POSTGRESQL;
    }

    @Override
    public String hibernateDialect()
    {
        return PostgreSQLDialect.class.getName();
    }

    protected DataSource newDataSource()
    {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(url());
        dataSource.setUser(username());
        dataSource.setPassword(password());

        return dataSource;
    }

    @Override
    public String username()
    {
        return "postgres";
    }

    @Override
    public String password()
    {
        return "admin";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public JdbcDatabaseContainer newJdbcDatabaseContainer()
    {
        var container = new PostgreSQLContainer("postgres:" + version);
        container.withCommand("postgres", "-c", "fsync=off", "-c", "random_page_cost=1.0", "-c", "synchronous_commit=off", "-c", "full_page_writes=off");
        container.withEnv(Map.of("PGDATA", "/var/lib/postgresql/data"));
        container.withTmpFs(Map.of("/var/lib/postgresql/data", "rw"));
        return container;
    }

}
