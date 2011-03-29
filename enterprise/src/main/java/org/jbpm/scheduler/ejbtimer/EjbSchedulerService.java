package org.jbpm.scheduler.ejbtimer;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.scheduler.SchedulerService;

/**
 * @author Tom Baeyens
 * @deprecated replaced by {@link EntitySchedulerService}
 */
public class EjbSchedulerService implements SchedulerService {

  private static final long serialVersionUID = 2L;

  private JbpmContext jbpmContext;
  private LocalTimerService timerService;

  public EjbSchedulerService(LocalTimerServiceHome timerServiceHome) {
    jbpmContext = JbpmContext.getCurrentJbpmContext();
    if (jbpmContext == null) throw new JbpmException("no active jbpm context");

    try {
      timerService = timerServiceHome.create();
    }
    catch (CreateException e) {
      throw new JbpmException("failed to create local timer service", e);
    }
  }

  public void createTimer(Timer timer) {
    log.debug("creating " + timer);
    jbpmContext.getJobSession().saveJob(timer);
    jbpmContext.getSession().flush();
    timerService.createTimer(timer);
  }

  public void deleteTimer(Timer timer) {
    if (log.isDebugEnabled()) log.debug("deleting " + timer);
    timerService.cancelTimer(timer);
    jbpmContext.getJobSession().deleteJob(timer);
  }

  public void deleteTimersByName(String timerName, Token token) {
    if (log.isDebugEnabled()) {
      log.debug("deleting timers by name '" + timerName + "' for " + token);
    }
    timerService.cancelTimersByName(timerName, token);
    jbpmContext.getJobSession().deleteTimersByName(timerName, token);
  }

  public void deleteTimersByProcessInstance(ProcessInstance processInstance) {
    if (log.isDebugEnabled()) log.debug("deleting timers for " + processInstance);
    timerService.cancelTimersForProcessInstance(processInstance);
    jbpmContext.getJobSession().deleteJobsForProcessInstance(processInstance);
  }

  public void close() {
    try {
      timerService.remove();
    }
    catch (RemoveException e) {
      throw new JbpmException("ejb local timer service close problem", e);
    }
  }

  private static final Log log = LogFactory.getLog(EjbSchedulerService.class);
}
