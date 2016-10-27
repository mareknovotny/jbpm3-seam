package org.jbpm.ant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Work;
import org.jbpm.db.hibernate.JbpmHibernateConfiguration;

public class ShutDownHsqldb extends Task {

  private String config = "hibernate.cfg.xml";
  private String properties;

  public void execute() throws BuildException {
    JbpmHibernateConfiguration jbpmHibernateConfiguration = AntHelper.getConfiguration(config, properties);
    SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) jbpmHibernateConfiguration.buildSessionFactory();

    try ( Session session = sessionFactory.getCurrentSession()) {

      session.doWork( new Work()
      {
        @Override
        public void execute( Connection connection ) throws SQLException {
          try (Statement statement = connection.createStatement()) {

              log( "shutting down database" );
              statement.executeUpdate( "SHUTDOWN" );
              statement.close();
          }
          catch ( SQLException e ) {
              throw new BuildException( "could not shut down database", e );
          }
          finally {
            try{
              if ( connection != null ) {
                connection.close();
              }
            }
            catch ( SQLException e ) {
                // ignore
            }
          }
        }
      } );
    }

  }

  public void setConfig(String config) {
    this.config = config;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }

}
