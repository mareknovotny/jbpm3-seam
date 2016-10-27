package org.jbpm.persistence.db;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Interceptor;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.resource.transaction.TransactionCoordinator;
import org.hibernate.stat.SessionStatistics;

@SuppressWarnings( {"rawtypes", "unchecked", "unused" } )
public class MockSession implements org.hibernate.Session, org.hibernate.engine.spi.SessionImplementor {

private static final long serialVersionUID = 1L;

  final Connection connection;
  final SessionFactory sessionFactory;

  MockTransaction transaction;
  boolean isFlushed;
  boolean isClosed;

  boolean failOnFlush;
  boolean failOnClose;

  public MockSession(SessionFactory sessionFactory) {
    this(sessionFactory, null);
  }

  public MockSession(SessionFactory sessionFactory, Connection connection) {
    this.connection = connection;
    this.sessionFactory = sessionFactory;
  }

  public void setFailOnFlush(boolean fail) {
    failOnFlush = fail;
  }

  public void setFailOnClose(boolean fail) {
    failOnClose = fail;
  }

  @Override
  public Transaction beginTransaction() throws HibernateException {
    transaction = new MockTransaction();
    return transaction;
  }

  @Override
  public Transaction getTransaction() {
    return transaction;
  }

  @Override
  public Connection connection() throws HibernateException {
    return connection;
  }

  @Override
  public void close() throws HibernateException {
    if (failOnClose)
      throw new HibernateException("simulated close exception");

    isClosed = true;
  }

  @Override
  public void flush() throws HibernateException {
    if (failOnFlush)
      throw new HibernateException("simulated flush exception");

    isFlushed = true;
  }

  @Override
  public boolean isOpen() {
    return ! isClosed;
  }

  @Override
  public void setFlushMode(FlushMode flushMode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FlushMode getFlushMode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCacheMode(CacheMode cacheMode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CacheMode getCacheMode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  @Override
  public void cancelQuery() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isConnected() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDirty() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Serializable getIdentifier(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object object) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void evict(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object load(Class theClass, Serializable id, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
 public Object load(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
 public Object load(Class theClass, Serializable id) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object load(String entityName, Serializable id) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void load(Object object, Serializable id) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replicate(Object object, ReplicationMode replicationMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replicate(String entityName, Object object, ReplicationMode replicationMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Serializable save(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Serializable save(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveOrUpdate(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveOrUpdate(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void update(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void update(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object merge(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object merge(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void persist(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void persist(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(String entityName, Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void lock(Object object, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void lock(String entityName, Object object, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refresh(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refresh(Object object, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public LockMode getCurrentLockMode(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Criteria createCriteria(Class persistentClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Criteria createCriteria(Class persistentClass, String alias) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Criteria createCriteria(String entityName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Criteria createCriteria(String entityName, String alias) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query createQuery(String queryString) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLQuery createSQLQuery(String queryString) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query createFilter(Object collection, String queryString) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query getNamedQuery(String queryName) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(Class clazz, Serializable id) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(Class clazz, Serializable id, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(String entityName, Serializable id) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(String entityName, Serializable id, LockMode lockMode) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getEntityName(Object object) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Filter enableFilter(String filterName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Filter getEnabledFilter(String filterName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void disableFilter(String filterName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SessionStatistics getStatistics() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setReadOnly(Object entity, boolean readOnly) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Connection disconnect() throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void reconnect(Connection connection) throws HibernateException {
    throw new UnsupportedOperationException();
  }
  @Override
  public String getTenantIdentifier() {
      throw new UnsupportedOperationException();
    }

    @Override
  public SharedSessionBuilder sessionWithOptions() {
      throw new UnsupportedOperationException();
    }

    @Override
  public boolean isDefaultReadOnly() {
      throw new UnsupportedOperationException();
    }

    @Override
  public void setDefaultReadOnly(boolean readOnly) {
      throw new UnsupportedOperationException();
  }

    @Override
  public Object load(Class theClass, Serializable id, LockOptions lockOptions)
            throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public Object load(String entityName, Serializable id,
            LockOptions lockOptions) throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public LockRequest buildLockRequest(LockOptions lockOptions) {
      throw new UnsupportedOperationException();
  }

    @Override
  public void refresh(String entityName, Object object)
            throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public void refresh(Object object, LockOptions lockOptions)
            throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public void refresh(String entityName, Object object,
            LockOptions lockOptions) throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public Object get(Class clazz, Serializable id, LockOptions lockOptions)
            throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public Object get(String entityName, Serializable id,
            LockOptions lockOptions) throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public boolean isReadOnly(Object entityOrProxy) {
      throw new UnsupportedOperationException();
  }

    @Override
  public void doWork(Work work) throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public <T> T doReturningWork(ReturningWork<T> work)
            throws HibernateException {
      throw new UnsupportedOperationException();
  }

    @Override
  public boolean isFetchProfileEnabled(String name)
            throws UnknownProfileException {
      throw new UnsupportedOperationException();
  }

    @Override
  public void enableFetchProfile(String name) throws UnknownProfileException {
      throw new UnsupportedOperationException();
  }

    @Override
  public void disableFetchProfile(String name) throws UnknownProfileException {
      throw new UnsupportedOperationException();
  }

    @Override
  public TypeHelper getTypeHelper() {
      throw new UnsupportedOperationException();
  }

    @Override
  public LobHelper getLobHelper() {
      throw new UnsupportedOperationException();
  }


  @Override
  public ProcedureCall getNamedProcedureCall(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcedureCall createStoredProcedureCall(String procedureName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcedureCall createStoredProcedureCall(String procedureName, Class ... resultClasses) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ProcedureCall createStoredProcedureCall(String procedureName, String ... resultSetMappings) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addEventListeners(SessionEventListener ... listeners) {
      throw new UnsupportedOperationException();
  }

  @Override
  public IdentifierLoadAccess byId(Class arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public IdentifierLoadAccess byId(String arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public NaturalIdLoadAccess byNaturalId(Class arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public NaturalIdLoadAccess byNaturalId(String arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public SimpleNaturalIdLoadAccess bySimpleNaturalId(String arg0) {
      throw new UnsupportedOperationException();
  }

  @Override
  public <T> T execute( Callback<T> callback ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JdbcConnectionAccess getJdbcConnectionAccess() {
    throw new UnsupportedOperationException();
  }

  @Override
  public EntityKey generateEntityKey( Serializable id, EntityPersister persister ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interceptor getInterceptor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAutoClear( boolean enabled ) {
      throw new UnsupportedOperationException();
  }

  @Override
  public void disableTransactionAutoJoin() {
      throw new UnsupportedOperationException();
  }

  @Override
  public boolean isTransactionInProgress() {
    return false;
  }

  @Override
  public void initializeCollection( PersistentCollection collection, boolean writing ) throws HibernateException {
      throw new UnsupportedOperationException();
  }

  @Override
  public Object internalLoad( String entityName, Serializable id, boolean eager, boolean nullable )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object immediateLoad( String entityName, Serializable id ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getTimestamp() {
    return 0;
  }

  @Override
  public SessionFactoryImplementor getFactory() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List list( String query, QueryParameters queryParameters ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator iterate( String query, QueryParameters queryParameters ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScrollableResults scroll( String query, QueryParameters queryParameters ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScrollableResults scroll( Criteria criteria, ScrollMode scrollMode ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List list( Criteria criteria ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List listFilter( Object collection, String filter, QueryParameters queryParameters )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator iterateFilter( Object collection, String filter, QueryParameters queryParameters )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public EntityPersister getEntityPersister( String entityName, Object object ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getEntityUsingInterceptor( EntityKey key ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Serializable getContextEntityIdentifier( Object object ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String bestGuessEntityName( Object object ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String guessEntityName( Object entity ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object instantiate( String entityName, Serializable id ) throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List listCustomQuery( CustomQuery customQuery, QueryParameters queryParameters )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScrollableResults scrollCustomQuery( CustomQuery customQuery, QueryParameters queryParameters )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List list( NativeSQLQuerySpecification spec, QueryParameters queryParameters )
    throws HibernateException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ScrollableResults scroll( NativeSQLQuerySpecification spec, QueryParameters queryParameters ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getDontFlushFromFind() {
    return 0;
  }

  @Override
  public PersistenceContext getPersistenceContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int executeUpdate( String query, QueryParameters queryParameters ) throws HibernateException {
    return 0;
  }

  @Override
  public int executeNativeUpdate( NativeSQLQuerySpecification specification, QueryParameters queryParameters )
    throws HibernateException {
    return 0;
  }

  @Override
  public Query getNamedSQLQuery( String name ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEventSource() {
    return false;
  }

  @Override
  public void afterScrollOperation() {
      throw new UnsupportedOperationException();
  }

  @Override
  public TransactionCoordinator getTransactionCoordinator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JdbcCoordinator getJdbcCoordinator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public boolean shouldAutoClose() {
    return false;
  }

  @Override
  public boolean isAutoCloseSessionEnabled() {
    return false;
  }

  @Override
  public LoadQueryInfluencers getLoadQueryInfluencers() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Query createQuery( NamedQueryDefinition namedQueryDefinition ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SQLQuery createSQLQuery( NamedSQLQueryDefinition namedQueryDefinition ) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SessionEventListenerManager getEventListenerManager() {
    throw new UnsupportedOperationException();
  }

}
