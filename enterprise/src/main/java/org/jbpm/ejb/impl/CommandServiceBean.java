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
package org.jbpm.ejb.impl;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.command.Command;
import org.jbpm.command.CommandService;
import org.jbpm.msg.jms.JmsMessageServiceFactory;
import org.jbpm.persistence.jta.JtaDbPersistenceServiceFactory;

/**
 * Stateless session bean that executes {@linkplain Command commands} by calling their
 * {@link Command#execute(JbpmContext) execute} method on a separate {@link JbpmContext jBPM
 * context}.
 * 
 * <h3>Environment</h3>
 * 
 * <p>
 * The environment entries and resources available for customization are summarized in the table
 * below.
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>JbpmCfgResource</code></td>
 * <td>Environment Entry</td>
 * <td>The classpath resource from which to read the {@linkplain JbpmConfiguration jBPM
 * configuration}. Optional, defaults to <code>
 * jbpm.cfg.xml</code>.</td>
 * </tr>
 * <tr>
 * <td><code>ejb/TimerEntityBean</code></td>
 * <td>EJB Reference</td>
 * <td>Link to the local {@linkplain TimerEntityBean entity bean} that implements the scheduler
 * service. Required for processes that contain timers.</td>
 * </tr>
 * <tr>
 * <td><code>jdbc/JbpmDataSource</code></td>
 * <td>Resource Manager Reference</td>
 * <td>Logical name of the data source that provides JDBC connections to the
 * {@linkplain JtaDbPersistenceServiceFactory persistence service}. Must match the
 * <code>hibernate.connection.datasource</code> property in the Hibernate configuration file.</td>
 * </tr>
 * <tr>
 * <td><code>jms/JbpmConnectionFactory</code></td>
 * <td>Resource Manager Reference</td>
 * <td>Logical name of the factory that provides JMS connections to the
 * {@linkplain JmsMessageServiceFactory message service}. Required for processes that contain
 * asynchronous continuations.</td>
 * </tr>
 * <tr>
 * <td><code>jms/JobQueue</code></td>
 * <td>Message Destination Reference</td>
 * <td>The message service sends job messages to the queue referenced here. To ensure this is
 * the same queue from which the {@linkplain JobListenerBean job listener bean} receives
 * messages, the <code>message-destination-link
 * </code> points to a common logical destination, <code>JobQueue</code>.</td>
 * </tr>
 * </table>
 * 
 * @author Jim Rigsbee
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class CommandServiceBean implements SessionBean, CommandService {

  private static final long serialVersionUID = 1L;

  private JbpmConfiguration jbpmConfiguration;
  private SessionContext sessionContext;

  /**
   * Creates the {@link JbpmConfiguration} to be used by this command service. In case the
   * environment key <code>JbpmCfgResource</code> is specified, that value is interpreted as the
   * name of the configuration resource to load from the classpath. If that key is absent, the
   * default configuration file will be used (jbpm.cfg.xml).
   */
  public void ejbCreate() throws CreateException {
    String jbpmCfgResource;
    try {
      Context jndiContext = new InitialContext();
      jbpmCfgResource = (String) jndiContext.lookup("java:comp/env/JbpmCfgResource");
    }
    catch (NamingException e) {
      if (log.isDebugEnabled()) {
        log.debug("could not fetch configuration resource: " + e.getMessage());
      }
      // use default configuration resource
      jbpmCfgResource = null;
    }

    jbpmConfiguration = JbpmConfiguration.getInstance(jbpmCfgResource);
  }

  public Object execute(Command command) {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      if (log.isDebugEnabled()) log.debug("executing " + command);
      Object result = command.execute(jbpmContext);
      // check whether command requested a rollback
      if (jbpmContext.getServices().getTxService().isRollbackOnly()) {
        sessionContext.setRollbackOnly();
      }
      return result;
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new JbpmException("failed to execute " + command, e);
    }
    finally {
      jbpmContext.close();
    }
  }

  public void setSessionContext(SessionContext sessionContext) {
    this.sessionContext = sessionContext;
  }

  public void ejbRemove() {
    sessionContext = null;
    jbpmConfiguration = null;
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  private static final Log log = LogFactory.getLog(CommandServiceBean.class);
}
