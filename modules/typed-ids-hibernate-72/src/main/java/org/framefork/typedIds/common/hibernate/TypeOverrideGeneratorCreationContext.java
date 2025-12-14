package org.framefork.typedIds.common.hibernate;

import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Value;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TypeOverrideGeneratorCreationContext implements GeneratorCreationContext
{

    private final GeneratorCreationContext delegate;
    private final Type typeOverride;

    public TypeOverrideGeneratorCreationContext(
        final GeneratorCreationContext delegate,
        final Type typeOverride
    )
    {
        this.delegate = delegate;
        this.typeOverride = typeOverride;
    }

    @Override
    public SqlStringGenerationContext getSqlStringGenerationContext()
    {
        return delegate.getSqlStringGenerationContext();
    }

    @Override
    public Type getType()
    {
        return typeOverride;
    }

    @Override
    public Property getProperty()
    {
        return delegate.getProperty();
    }

    @Override
    public Value getValue()
    {
        return delegate.getValue();
    }

    @Override
    public RootClass getRootClass()
    {
        return delegate.getRootClass();
    }

    @Override
    public PersistentClass getPersistentClass()
    {
        return delegate.getPersistentClass();
    }

    @Override
    public String getDefaultSchema()
    {
        return delegate.getDefaultSchema();
    }

    @Override
    public String getDefaultCatalog()
    {
        return delegate.getDefaultCatalog();
    }

    @Override
    public ServiceRegistry getServiceRegistry()
    {
        return delegate.getServiceRegistry();
    }

    @Override
    public Database getDatabase()
    {
        return delegate.getDatabase();
    }

}
