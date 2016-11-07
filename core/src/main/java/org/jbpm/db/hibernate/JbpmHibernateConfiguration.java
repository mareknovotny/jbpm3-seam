package org.jbpm.db.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.SharedCacheMode;

import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AttributeConverterDefinition;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tuple.entity.EntityTuplizerFactory;
import org.hibernate.type.BasicType;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class JbpmHibernateConfiguration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Configuration configurationProxy;

    // ALSO
    private MetadataImplementor metadataImplementor;

    public JbpmHibernateConfiguration()
    {
        super();
        this.configurationProxy = createConfigurationProxy();
    }

    protected Configuration createConfigurationProxy() {

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(Configuration.class);
//        proxyFactory.setInterfaces(new Class[] { Serializable.class });

        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method m) {
                return (m.getName().equals("buildSessionFactory") && m.getParameterTypes().length == 1);
            }
        });

        MethodHandler methodHandler = new MethodHandler() {

            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {

                long start = System.currentTimeMillis();
                try {
//                    return proceed.invoke(self, args);
                    return buildSessionFactory();
                } catch (Exception e ) {
                    throw new RuntimeException( e );
                } finally {

                    long end = System.currentTimeMillis();
                    System.out.println("Execution time: " + (end - start)
                            + " ms, method: " + proceed);
                }

            }
        };

        try
        {
            // Default Constructor for Configuration - new Configuration()
            return (Configuration) proxyFactory.create(new Class[0], new Object[0], methodHandler);
        }
        catch ( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     *
     * @see Configuration#buildSessionFactory()
     * @see Configuration#buildSessionFactory(ServiceRegistry)
     *
     */
    public SessionFactory buildSessionFactory() throws HibernateException {

        Configuration configuration = configurationProxy;

        if (this.metadataImplementor == null) {

            StandardServiceRegistryBuilder standardServiceRegistryBuilder = getFieldValueFromParent( configuration, "standardServiceRegistryBuilder" );
            Properties properties = getFieldValueFromParent( configuration, "properties" );
            standardServiceRegistryBuilder.applySettings( properties );
            ServiceRegistry serviceRegistry = standardServiceRegistryBuilder.build();

            MetadataSources metadataSources = getFieldValueFromParent( configuration, "metadataSources" );
            ImplicitNamingStrategy implicitNamingStrategy = getFieldValueFromParent( configuration, "implicitNamingStrategy" );
            PhysicalNamingStrategy physicalNamingStrategy = getFieldValueFromParent( configuration, "physicalNamingStrategy" );
            SharedCacheMode sharedCacheMode = getFieldValueFromParent( configuration, "sharedCacheMode" );
            List<TypeContributor> typeContributorRegistrations = getFieldValueFromParent( configuration, "typeContributorRegistrations" );
            List<BasicType> basicTypes = getFieldValueFromParent( configuration, "basicTypes" );
//            Map<String, NamedSQLQueryDefinition> namedSqlQueries = getFieldValueFromParent( configuration, "namedSqlQueries" );
            Map<String, SQLFunction> sqlFunctions = getFieldValueFromParent( configuration, "sqlFunctions" );
            List<AuxiliaryDatabaseObject> auxiliaryDatabaseObjectList = getFieldValueFromParent( configuration, "auxiliaryDatabaseObjectList" );
            HashMap<Class,AttributeConverterDefinition> attributeConverterDefinitionsByClass = getFieldValueFromParent( configuration, "attributeConverterDefinitionsByClass" );

            final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder( (StandardServiceRegistry) serviceRegistry );
            if ( implicitNamingStrategy != null ) {
                metadataBuilder.applyImplicitNamingStrategy( implicitNamingStrategy );
            }
            if ( physicalNamingStrategy != null ) {
                metadataBuilder.applyPhysicalNamingStrategy( physicalNamingStrategy );
            }
            if ( sharedCacheMode != null ) {
                metadataBuilder.applySharedCacheMode( sharedCacheMode );
            }
            if ( !typeContributorRegistrations.isEmpty() ) {
                for ( TypeContributor typeContributor : typeContributorRegistrations ) {
                    metadataBuilder.applyTypes( typeContributor );
                }
            }
            if ( !basicTypes.isEmpty() ) {
                for ( BasicType basicType : basicTypes ) {
                    metadataBuilder.applyBasicType( basicType );
                }
            }
            if ( sqlFunctions != null ) {
                for ( Map.Entry<String, SQLFunction> entry : sqlFunctions.entrySet() ) {
                    metadataBuilder.applySqlFunction( entry.getKey(), entry.getValue() );
                }
            }
            if ( auxiliaryDatabaseObjectList != null ) {
                for ( AuxiliaryDatabaseObject auxiliaryDatabaseObject : auxiliaryDatabaseObjectList ) {
                    metadataBuilder.applyAuxiliaryDatabaseObject( auxiliaryDatabaseObject );
                }
            }
            if ( attributeConverterDefinitionsByClass != null ) {
                for ( AttributeConverterDefinition attributeConverterDefinition : attributeConverterDefinitionsByClass.values() ) {
                    metadataBuilder.applyAttributeConverter( attributeConverterDefinition );
                }
            }

            this.metadataImplementor = (MetadataImplementor)metadataBuilder.build();
        }

        Interceptor interceptor = getFieldValueFromParent( configuration, "interceptor" );
        SessionFactoryObserver sessionFactoryObserver = getFieldValueFromParent( configuration, "sessionFactoryObserver" );
        EntityNotFoundDelegate entityNotFoundDelegate = getFieldValueFromParent( configuration, "entityNotFoundDelegate" );
        EntityTuplizerFactory entityTuplizerFactory = getFieldValueFromParent( configuration, "entityTuplizerFactory" );

        final SessionFactoryBuilder sessionFactoryBuilder = metadataImplementor.getSessionFactoryBuilder();
        if ( interceptor != null && interceptor != EmptyInterceptor.INSTANCE ) {
            sessionFactoryBuilder.applyInterceptor( interceptor );
        }
        if ( sessionFactoryObserver != null ) {
            sessionFactoryBuilder.addSessionFactoryObservers( sessionFactoryObserver );
        }
        if ( entityNotFoundDelegate != null ) {
            sessionFactoryBuilder.applyEntityNotFoundDelegate( entityNotFoundDelegate );
        }
        if ( entityTuplizerFactory != null ) {
            sessionFactoryBuilder.applyEntityTuplizerFactory( entityTuplizerFactory );
        }

        return sessionFactoryBuilder.build();
    }

    public Configuration getConfigurationProxy()
    {
        return configurationProxy;
    }

    public BootstrapServiceRegistry getBootstrapServiceRegistry()
    {
        return getFieldValueFromParent( configurationProxy, "bootstrapServiceRegistry" );
    }

    public MetadataSources getMetadataSources()
    {
        return getFieldValueFromParent( configurationProxy, "metadataSources" );
    }

    public MetadataImplementor getMetadataImplementor()
    {
        return metadataImplementor;
    }

    private static <T> T getFieldValueFromParent(Object instance, String fieldName) {
        try
        {
            Field field = instance.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);

            @SuppressWarnings( "unchecked" )
            T fieldValue = (T)field.get(instance);
            return fieldValue;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

//    private static <T> T getFieldValue(Object instance, String fieldName) {
//        try
//        {
//            Field field = instance.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//
//            @SuppressWarnings( "unchecked" )
//            T fieldValue = (T)field.get(instance);
//            return fieldValue;
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//    }
}
