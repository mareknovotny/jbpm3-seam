package org.jbpm.ejb.impl;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.command.Command;

/**
 * Message-driven bean that listens for {@link Message messages} containing a reference to a
 * pending {@linkplain org.jbpm.job.Job job} for asynchronous continuations.
 * <p>
 * The message must have a <code>long</code> property called <code>jobId</code> which identifies
 * a job in the database. The message body, if any, is ignored.
 * </p>
 * <h3>Environment</h3>
 * <p>
 * This bean inherits its environment entries and resources available for customization from
 * {@link CommandListenerBean}.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class JobListenerBean extends CommandListenerBean {

  private static final long serialVersionUID = 1L;
  private static final Log log = LogFactory.getLog(JobListenerBean.class);

  protected Command extractCommand(Message message) throws JMSException {
    // checking for jobId property
    if (message.propertyExists("jobId")) {
      long jobId = message.getLongProperty("jobId");
      return new ExecuteJobCommand(jobId);
    }
    else {
      log.warn("property jobId not found");
    }
    return null;
  }
}
