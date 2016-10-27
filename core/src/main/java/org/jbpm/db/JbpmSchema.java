/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.mapping.Table;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.extract.internal.DatabaseInformationImpl;
import org.hibernate.tool.schema.extract.spi.DatabaseInformation;
import org.hibernate.tool.schema.extract.spi.TableInformation;
import org.jbpm.JbpmException;
import org.jbpm.db.hibernate.JbpmHibernateConfiguration;
import org.jbpm.logging.db.JDBCExceptionReporter;

/**
 * utilities for the jBPM database schema.
 *
 * {@link "http://stackoverflow.com/questions/32178041/where-did-configuration-generateschemacreationscript-go-in-hibernate-5"}
 * {@link "http://stackoverflow.com/questions/24132167/how-to-export-database-schema-using-hibernate-schemaexport-with-beanvalidation-c"}
 */
@SuppressWarnings( "nls" )
public class JbpmSchema {

  private final JbpmHibernateConfiguration jbpmHibernateConfiguration;

  private String delimiter;
  private final List<Exception> exceptions = new ArrayList<>();

  private MetadataSources metadataSources;
  private MetadataImplementor metadataImplementor;
  private SessionFactory sessionFactory;
  private Session session;
  private SessionImplementor sessionImplementor;

  private static final String[] TABLE_TYPES = { "TABLE" };

  public JbpmSchema(JbpmHibernateConfiguration jbpmHibernateConfiguration) {
      this.jbpmHibernateConfiguration = jbpmHibernateConfiguration;
      this.delimiter = ";";
  }

  private synchronized void configure() {
    if (sessionFactory == null) {
      this.sessionFactory = jbpmHibernateConfiguration.buildSessionFactory();
      this.metadataSources = jbpmHibernateConfiguration.getMetadataSources();
      this.metadataImplementor = jbpmHibernateConfiguration.getMetadataImplementor();
      this.session = sessionFactory.openSession();
      this.sessionImplementor = (SessionImplementor)session;
    }
  }

  Dialect getDialect() {
    return Dialect.getDialect(jbpmHibernateConfiguration.getConfigurationProxy().getProperties());
  }

  private String getDefaultCatalog() {
    return jbpmHibernateConfiguration.getConfigurationProxy().getProperty(Environment.DEFAULT_CATALOG);
  }

  private String getDefaultSchema() {
    return jbpmHibernateConfiguration.getConfigurationProxy().getProperty(Environment.DEFAULT_SCHEMA);
  }

  private boolean getShowSql() {
    return "true".equalsIgnoreCase(jbpmHibernateConfiguration.getConfigurationProxy().getProperty(Environment.SHOW_SQL));
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }

  public void dropSchema() {
      dropSchema(true, null);
  }

  public void dropSchema(boolean exportToDb, String exportToFile) {

    configure();

    SchemaExport schemaExport = new SchemaExport(metadataImplementor);
    schemaExport.setOutputFile(exportToFile);
    schemaExport.setDelimiter(delimiter);
    schemaExport.drop(true, exportToDb);

    @SuppressWarnings( "unchecked" )
    List<Exception> schemaExceptions = schemaExport.getExceptions();

    if (schemaExceptions != null && schemaExceptions.size() > 0) {
      for ( Exception e : schemaExceptions ) {
          exceptions.add( e );
          JDBCExceptionReporter.logExceptions(e, "failed to drop schema");
      }
    }
  }

  public void createSchema() {
      createSchema( true, null );
  }

  public void createSchema(boolean exportToDb, String exportToFile) {

    configure();

    SchemaExport schemaExport = new SchemaExport(metadataImplementor);
    schemaExport.setOutputFile(exportToFile);
    schemaExport.setDelimiter(delimiter);
    schemaExport.create(true, exportToDb);

    @SuppressWarnings( "unchecked" )
    List<Exception> schemaExceptions = schemaExport.getExceptions();

    if (schemaExceptions != null && schemaExceptions.size() > 0) {
      for ( Exception e : schemaExceptions ) {
        exceptions.add( e );
        JDBCExceptionReporter.logExceptions(e, "failed to create schema");
      }
    }
  }

  public void cleanSchema() {
      cleanSchema( true, null );
  }

  public void cleanSchema(boolean exportToDb, String exportToFile) {

    configure();

    SchemaExport schemaExportForDrop = new SchemaExport(metadataImplementor);
    schemaExportForDrop.setOutputFile(exportToFile);
    schemaExportForDrop.setDelimiter(delimiter);
    schemaExportForDrop.execute( true, exportToDb, false, false );

    @SuppressWarnings( "unchecked" )
    List<Exception> schemaExceptions = schemaExportForDrop.getExceptions();

    if (schemaExceptions != null && schemaExceptions.size() > 0) {
      for ( Exception e : schemaExceptions ) {
        exceptions.add( e );
        JDBCExceptionReporter.logExceptions(e, "failed to clean schema");
      }
    }
  }

  public void updateSchema() {
      updateSchema( true, null );
  }

  public void updateSchema(boolean exportToDb, String exportToFile) {

    configure();

    SchemaUpdate schemaUpdate = new SchemaUpdate(metadataImplementor);
    schemaUpdate.setOutputFile(exportToFile);
    schemaUpdate.setDelimiter(delimiter);
    schemaUpdate.execute(true, exportToDb);

    @SuppressWarnings( "unchecked" )
    List<Exception> schemaExceptions = schemaUpdate.getExceptions();

    if (schemaExceptions != null && schemaExceptions.size() > 0) {
      for ( Exception e : schemaExceptions ) {
          exceptions.add( e );
          JDBCExceptionReporter.logExceptions(e, "failed to update schema");
      }
    }

  }

  public void saveSqlScripts(String dir, String prefix) {
    File path = new File(dir);
    if (!path.isDirectory()) {
      throw new JbpmException(path + " is not a directory");
    }

    dropSchema(true, new File(path, prefix + ".drop.sql").getAbsolutePath());
    createSchema(true, new File(path, prefix + ".create.sql").getAbsolutePath());

    for ( Exception e : exceptions ) {
        JDBCExceptionReporter.logExceptions(e, "failed to generate scripts");
    }
  }

  public Set<String> getJbpmTables() {

    configure();

    Set<String> jbpmTables = new HashSet<>();

    Map<String, ClassMetadata>  map = sessionFactory.getAllClassMetadata();
    for(String entityName : map.keySet()){
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        String tableName = ((AbstractEntityPersister)sessionFactoryImpl.getEntityPersister(entityName)).getTableName();
        jbpmTables.add(tableName);
    }

    return jbpmTables;
  }

  public Map<String, Long> getRowsPerTable() {

    configure();

    final Map<String, Long> rowsPerTable = new HashMap<>();

    session.doWork( new Work(){

      @Override
      public void execute( Connection connection ) {
        try {

          JbpmSchema.prepareConnection( connection );

          for (String tableName :getJbpmTables()) {
            String sql = "SELECT COUNT(*) FROM " + tableName;

            try ( Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql); ) {

              if (!resultSet.next()) continue;

              long count = resultSet.getLong(1);
              if (resultSet.wasNull()) continue;

              rowsPerTable.put(tableName, new Long(count));
            }
          }
        } catch (SQLException e) {
          exceptions.add(e);
          JDBCExceptionReporter.logExceptions(e, "could not count records");

          rowsPerTable.clear();
        }
      }
    });

    return rowsPerTable;
  }

  public Set<String> getExistingTables() {

      configure();

      final Set<String> existingTables = new HashSet<>();

      session.doWork( new Work(){

        @Override
        public void execute( Connection connection ) {
          try {

            JbpmSchema.prepareConnection( connection );

            DatabaseMetaData metaData = connection.getMetaData();
            boolean storesLowerCaseIdentifiers = metaData.storesLowerCaseIdentifiers();

            try ( ResultSet resultSet = metaData.getTables(
                getDefaultCatalog(), getDefaultSchema(), null, TABLE_TYPES) ) {

              while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (storesLowerCaseIdentifiers) tableName = tableName.toUpperCase();
                existingTables.add(tableName);
              }
            }
          } catch (SQLException e) {
            exceptions.add(e);
            JDBCExceptionReporter.logExceptions(e, "could not count records");

            existingTables.clear();
          }
        }

      });

      return existingTables;

  }

  public boolean tableExists(final String tableName) {

    configure();

    final Boolean found = session.doReturningWork( new ReturningWork<Boolean>() {

      @Override
      public Boolean execute( Connection connection ) throws SQLException {

        JbpmSchema.prepareConnection( connection );

        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet resultSet = metaData.getTables(getDefaultCatalog(), getDefaultSchema(), tableName, TABLE_TYPES) ){
            return resultSet.next();
        }
        catch (SQLException e) {
          exceptions.add(e);
          JDBCExceptionReporter.logExceptions(e, "could not determine whether table exists");

          return false;
        }
      }
    } );

    return found;
  }

  public void updateTable(String tableName) {

    configure();

    final Table table = findTableMapping(tableName);
    final boolean showSql = getShowSql();

    final TableInformation tableInformation = getDatabaseInformation().getTableInformation( table.getQualifiedTableName() );

    session.doWork( new Work(){

      @Override
      public void execute( Connection connection ) {
        try {

          JbpmSchema.prepareConnection( connection );

          try ( Statement statement = connection.createStatement();) {
            if ( tableInformation == null) {
              String sql = table.sqlCreateString(getDialect(), metadataImplementor, getDefaultCatalog(), getDefaultSchema());
              JbpmSchema.execute(sql, statement, showSql);
            }
            else {
              Iterator scripts = table.sqlAlterStrings( getDialect(), metadataImplementor, tableInformation, getDefaultCatalog(), getDefaultSchema() );
              for (Iterator script = scripts; script.hasNext();) {
                  String sql = (String) script.next();
                  JbpmSchema.execute(sql, statement, showSql);
              }
            }
          }
        } catch (SQLException e) {
          exceptions.add(e);
          JDBCExceptionReporter.logExceptions(e, "failed to update table");

        }
      }
    });
  }

  private DatabaseInformation getDatabaseInformation() {

    final ServiceRegistry serviceRegistry = metadataImplementor.getMetadataBuildingOptions().getServiceRegistry();

//    final ConfigurationService cfgService = serviceRegistry.getService( ConfigurationService.class );
//    final SchemaManagementTool schemaManagementTool = serviceRegistry.getService( SchemaManagementTool.class );
//    final SchemaMigrator schemaMigrator = schemaManagementTool.getSchemaMigrator( cfgService.getSettings() );
    final JdbcServices jdbcServices = serviceRegistry.getService( JdbcServices.class );
    final JdbcConnectionAccess jdbcConnectionAccess = jdbcServices.getBootstrapJdbcConnectionAccess();

    final DatabaseInformation databaseInformation;
    try {
      databaseInformation = new DatabaseInformationImpl(
        serviceRegistry,
        serviceRegistry.getService( JdbcEnvironment.class ),
        jdbcConnectionAccess,
        metadataImplementor.getDatabase().getDefaultNamespace().getPhysicalName().getCatalog(),
        metadataImplementor.getDatabase().getDefaultNamespace().getPhysicalName().getSchema()
      );
    }
    catch (SQLException e) {
      throw jdbcServices.getSqlExceptionHelper().convert(
        e,
        "Error creating DatabaseInformation for schema migration"
      );
    }
    return databaseInformation;
  }

  private Table findTableMapping(String tableName) {

    configure();

    Collection<Table> tableMappings = metadataImplementor.collectTableMappings();
    for ( Table table : tableMappings ) {
        if (tableName.equals(table.getName())) return table;
    }

    throw new JbpmException("no mapping found for table: " + tableName);
  }

  public void createTable(String tableName) {

    configure();

    final Table table = findTableMapping(tableName);
    final boolean showSql = getShowSql();

    session.doWork( new Work(){

      @Override
      public void execute( Connection connection ) {
        try {

          JbpmSchema.prepareConnection( connection );

          try ( Statement statement = connection.createStatement();) {
              String sql = table.sqlCreateString(getDialect(), metadataImplementor, getDefaultCatalog(), getDefaultSchema());
              JbpmSchema.execute(sql, statement, showSql);
          }
        } catch (SQLException e) {
          exceptions.add(e);
          JDBCExceptionReporter.logExceptions(e, "failed to create table");

        }
      }
    });

  }

//  private static List<Exception> execute(String[] script, Statement statement, boolean showSql) {
//    List<Exception> exceptions = new LinkedList<>();
//
//    for (int i = 0; i < script.length; i++) {
//      String sql = script[i];
//      Exception exception = execute(sql, statement, showSql);
//      if (exception != null) {
//        exceptions.add( exception );
//      }
//    }
//    return exceptions;
//  }

  private static Exception execute(String sql, Statement statement, boolean showSql) {
    try {
      if (showSql) System.out.println(sql);
      statement.executeUpdate(sql);

      SQLWarning warning = statement.getWarnings();
      if (warning != null) {
        JDBCExceptionReporter.logWarnings(warning);
        statement.clearWarnings();
      }
    } catch (SQLException e) {
      JDBCExceptionReporter.logExceptions(e, "failed to execute update");
      return e;
    }
    return null;
  }

  private static void prepareConnection( Connection connection ) throws SQLException {
    if ( connection.getAutoCommit() == false ) {
      connection.commit();
      connection.setAutoCommit( true );
    }
  }

  public static void main(String[] args) {
    if (args.length > 0) {
      String action = args[0];

      if ("create".equalsIgnoreCase(action)) {
        getJbpmSchema(args, 1).createSchema();
      }
      else if ("drop".equalsIgnoreCase(action)) {
        getJbpmSchema(args, 1).dropSchema();
      }
      else if ("clean".equalsIgnoreCase(action)) {
        getJbpmSchema(args, 1).cleanSchema();
      }
      else if ("update".equalsIgnoreCase(action)) {
        getJbpmSchema(args, 1).updateSchema();
      }
      else if ("scripts".equalsIgnoreCase(action) && args.length > 2) {
        getJbpmSchema(args, 3).saveSqlScripts(args[1], args[2]);
      }
      else
        syntax();
    }
    else
      syntax();
  }

  private static void syntax() {
    System.err.println("Syntax:");
    System.err.println("JbpmSchema create [<hibernate.cfg.xml> [<hibernate.properties>]]");
    System.err.println("JbpmSchema drop [<hibernate.cfg.xml> [<hibernate.properties>]]");
    System.err.println("JbpmSchema clean [<hibernate.cfg.xml> [<hibernate.properties>]]");
    System.err.println("JbpmSchema update [<hibernate.cfg.xml> [<hibernate.properties>]]");
    System.err
      .println("JbpmSchema scripts <dir> <prefix> [<hibernate.cfg.xml> [<hibernate.properties>]]");
    System.exit(1);
  }

  private static JbpmSchema getJbpmSchema(String[] args, int index) {
    JbpmHibernateConfiguration jbpmHibernateConfiguration = new JbpmHibernateConfiguration();
    Configuration configuration = jbpmHibernateConfiguration.getConfigurationProxy();

    if (index < args.length) {
      // read configuration xml file
      String hibernateCfgXml = args[index];
      configuration.configure(new File(hibernateCfgXml));

      // read extra properties
      if (index + 1 < args.length) {
        String hibernateProperties = args[index + 1];
        try {
          InputStream fileSource = new FileInputStream(hibernateProperties);
          Properties properties = new Properties();
          properties.load(fileSource);
          fileSource.close();
          configuration.addProperties(properties);
        }
        catch (IOException e) {
          throw new JbpmException("failed to load hibernate properties", e);
        }
      }
    }
    else {
      // read configuration from default resource
      configuration.configure();
    }

    return new JbpmSchema(jbpmHibernateConfiguration);
  }



  public JbpmHibernateConfiguration getJbpmHibernateConfiguration() {
    return jbpmHibernateConfiguration;
  }


  public String getDelimiter() {
    return delimiter;
  }


  public MetadataSources getMetadataSources() {
    configure();
    return metadataSources;
  }


  public MetadataImplementor getMetadataImplementor() {
    configure();
    return metadataImplementor;
  }


  public SessionFactory getSessionFactory() {
    configure();
    return sessionFactory;
  }

  public Session getSession()
  {
    return session;
  }

  public SessionImplementor getSessionImplementor() {
    configure();
    return sessionImplementor;
  }

}
