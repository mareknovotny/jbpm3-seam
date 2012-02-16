package org.jbpm.ant;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

public class ShutDownHsqldb extends Task {

  private String config = "hibernate.cfg.xml";
  private String properties;

  public void execute() throws BuildException {
    Configuration configuration = AntHelper.getConfiguration(config, properties);
    SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) configuration.buildSessionFactory();
    ConnectionProvider connectionProvider = sessionFactory.getConnectionProvider();
    Connection connection = null;
    try {
      connection = connectionProvider.getConnection();
      Statement statement = connection.createStatement();

      log("shutting down database");
      statement.executeUpdate("SHUTDOWN");

      connectionProvider.closeConnection(connection);
    }
    catch (SQLException e) 
    {
      throw new BuildException("could not shut down database", e);
    }
    finally 
    {
      try 
      {
		connectionProvider.closeConnection(connection);
      } 
      catch (SQLException e) { }
    }
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }

}
