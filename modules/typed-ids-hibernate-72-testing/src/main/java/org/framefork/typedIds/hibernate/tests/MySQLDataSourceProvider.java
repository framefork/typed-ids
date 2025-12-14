package org.framefork.typedIds.hibernate.tests;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.hibernate.dialect.Database;
import org.hibernate.dialect.MySQLDialect;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

import javax.sql.DataSource;
import java.util.Map;

public final class MySQLDataSourceProvider extends AbstractContainerDataSourceProvider
{

    public static final MySQLDataSourceProvider V8 = new MySQLDataSourceProvider("8.4");

    private final String version;

    private MySQLDataSourceProvider(final String version)
    {
        this.version = version;
    }

    @Override
    public Database database()
    {
        return Database.MYSQL;
    }

    @Override
    public String hibernateDialect()
    {
        return MySQLDialect.class.getName();
    }

    @Override
    protected DataSource newDataSource()
    {
        var dataSource = new MysqlDataSource();
        dataSource.setURL(url());
        dataSource.setUser(username());
        dataSource.setPassword(password());
        return dataSource;
    }

    @Override
    public String username()
    {
        return "mysql";
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
        var container = new MySQLContainer("mysql:" + version);
        container.withTmpFs(Map.of("/var/lib/mysql", "rw"));
        return container;
    }

}
