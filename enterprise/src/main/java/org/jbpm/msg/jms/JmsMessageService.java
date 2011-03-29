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
package org.jbpm.msg.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jbpm.JbpmException;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;
import org.jbpm.msg.MessageService;
import org.jbpm.svc.Services;

public class JmsMessageService implements MessageService {

  private final JmsMessageServiceFactory serviceFactory;

  private final Connection connection;
  private final Session session;

  private static final long serialVersionUID = 2L;
  private static final String GROUP_ID_PROP = "JMSXGroupID";
  private static final String GROUP_PREFIX = "jBPMPID";

  /** @deprecated use {@link #JmsMessageService(JmsMessageServiceFactory)} instead */
  public JmsMessageService(Connection connection, Destination destination, boolean commitEnabled)
    throws JMSException {
    this.connection = connection;
    session = createSession(connection);
    serviceFactory = (JmsMessageServiceFactory) Services.getCurrentService(Services.SERVICENAME_MESSAGE);
  }

  public JmsMessageService(JmsMessageServiceFactory serviceFactory) throws JMSException {
    connection = serviceFactory.getConnectionFactory().createConnection();
    session = createSession(connection);
    this.serviceFactory = serviceFactory;
  }

  /**
   * EJB 2.1 section 17.3.5 Because the container manages the transactional enlistment of JMS
   * sessions on behalf of a bean, the parameters of the
   * {@link Connection#createSession(boolean, int) createSession} method are ignored. It is
   * recommended that the Bean Provider specify that a session is transacted, but provide
   * <code>0</code> for the value of the acknowledgment mode.
   * <p>
   * Nonetheless, in <a href="http://publib.boulder.ibm.com/infocenter/adiehelp/v5r1m1/topic/com.ibm.wasee.doc/info/ee/ae/tmj_ep.html"
   * >WebSphere</a>, if the transacted flag is set to true outside of a transaction, the
   * application should use {@link Session#commit() commit} or {@link Session#rollback()
   * rollback} to control the completion of the work.
   * </p>
   * <p>
   * Therefore, the safest course of action is to create a session with the parameters
   * <code>false</code> and {@link Session#AUTO_ACKNOWLEDGE}.
   */
  private static Session createSession(Connection connection) throws JMSException {
    return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  public void send(Job job) {
    getJobSession().saveJob(job);

    try {
      sendMessage(job);
    }
    catch (JMSException e) {
      throw new JbpmException("could not send jms message", e);
    }
  }

  private JobSession getJobSession() {
    return serviceFactory.getJbpmConfiguration().getCurrentJbpmContext().getJobSession();
  }

  private void sendMessage(Job job) throws JMSException {
    Message message = getSession().createMessage();
    modifyMessage(message, job);

    MessageProducer messageProducer = getMessageProducer();
    try {
      messageProducer.send(message);
    }
    finally {
      messageProducer.close();
    }
  }

  /**
   * Hook to modify the message, e.g. adding extra properties to the header required by the own
   * application. One possible use case is to rescue the actor id over the "JMS" intermezzo of
   * asynchronous continuations.
   */
  protected void modifyMessage(Message message, Job job) throws JMSException {
    message.setLongProperty("jobId", job.getId());

    if (job.isExclusive()) {
      message.setStringProperty(GROUP_ID_PROP, GROUP_PREFIX + job.getProcessInstance().getId());
    }
  }

  public void close() {
    // there is no need to close the sessions and producers of a closed connection
    try {
      connection.close();
    }
    catch (JMSException e) {
      throw new JbpmException("could not close jms connection", e);
    }
  }

  public Session getSession() {
    return session;
  }

  protected MessageProducer getMessageProducer() throws JMSException {
    return getSession().createProducer(serviceFactory.getDestination());
  }
}
