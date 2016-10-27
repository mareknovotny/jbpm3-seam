package org.jbpm.persistence.db;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.hibernate.Cache;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.internal.NamedQueryRepository;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.jbpm.db.hibernate.JbpmHibernateConfiguration;

public class MockSessionFactory implements SessionFactoryImplementor {

    private Settings settings;
    private boolean failOnFlush;
    private boolean failOnClose;
    private boolean isClosed;

    private static final long serialVersionUID = 1L;

    public MockSessionFactory() {
        JbpmHibernateConfiguration jbpmHibernateConfiguration = new JbpmHibernateConfiguration();

        Configuration configuration = jbpmHibernateConfiguration.getConfigurationProxy();
        SessionFactory sessionFactory = configuration.configure()
                .buildSessionFactory();
        this.settings = ((SessionFactoryImplementor) sessionFactory)
                .getSettings();
    }

    public void setFailOnFlush(boolean fail) {
        failOnFlush = fail;
    }

    public void setFailOnClose(boolean fail) {
        failOnClose = fail;
    }

    public Session openSession(Connection connection) {
        MockSession session = new MockSession(this, connection);
        session.setFailOnFlush(failOnFlush);
        session.setFailOnClose(failOnClose);
        return session;
    }

    @Override
    public Session openSession() throws HibernateException {
        MockSession session = new MockSession(this);
        session.setFailOnFlush(failOnFlush);
        session.setFailOnClose(failOnClose);
        return session;
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return null;
    }

    // //////////////////////////

    @Override
    public void close() throws HibernateException {
        isClosed = true;
    }

    @Override
    public ClassMetadata getClassMetadata(Class persistentClass)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map getAllClassMetadata() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map getAllCollectionMetadata() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statistics getStatistics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public StatelessSession openStatelessSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set getDefinedFilterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map getAllSecondLevelCacheRegions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionPersister getCollectionPersister(String role)
            throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set getCollectionRolesByEntityParticipant(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dialect getDialect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityNotFoundDelegate getEntityNotFoundDelegate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPersister getEntityPersister(String entityName)
            throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getImplementors(String className) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getImportedClassName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Interceptor getInterceptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedQueryDefinition getNamedQuery(String queryName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryCache getQueryCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryCache getQueryCache(String regionName)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryPlanCache getQueryPlanCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMappingDefinition getResultSetMapping(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getReturnAliases(String queryString)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.hibernate.type.Type[] getReturnTypes(String queryString)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLExceptionConverter getSQLExceptionConverter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Region getSecondLevelCacheRegion(String regionName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public SQLFunctionRegistry getSqlFunctionRegistry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StatisticsImplementor getStatisticsImplementor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UpdateTimestampsCache getUpdateTimestampsCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session openTemporarySession() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIdentifierPropertyName(String className)
            throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getIdentifierType(String className) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getReferencedPropertyType(String className, String propertyName)
            throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cache getCache() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeHelper getTypeHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeResolver getTypeResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JdbcServices getJdbcServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SqlExceptionHelper getSQLExceptionHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FetchProfile getFetchProfile(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceRegistryImplementor getServiceRegistry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addObserver(SessionFactoryObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionBuilderImplementor withOptions()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, EntityPersister> getEntityPersisters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, CollectionPersister> getCollectionPersisters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerNamedQueryDefinition( String name, NamedQueryDefinition definition )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerNamedSQLQueryDefinition( String name, NamedSQLQueryDefinition definition )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegionAccessStrategy getSecondLevelCacheRegionAccessStrategy( String regionName )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegionAccessStrategy getNaturalIdCacheRegionAccessStrategy( String regionName )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamedQueryRepository getNamedQueryRepository()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<EntityNameResolver> iterateEntityNameResolvers()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPersister locateEntityPersister( Class byClass )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPersister locateEntityPersister( String byName )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeserializationResolver getDeserializationResolver()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Region getNaturalIdCacheRegion(String arg0) {
        throw new UnsupportedOperationException();
    }
}