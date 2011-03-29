package org.jbpm.scheduler.ejbtimer;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.ejb.LocalTimerEntity;
import org.jbpm.ejb.LocalTimerEntityHome;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.scheduler.SchedulerService;

public class EntitySchedulerService implements SchedulerService {

  private static final long serialVersionUID = 2L;

  private JbpmContext jbpmContext;
  private LocalTimerEntityHome timerEntityHome;

  public EntitySchedulerService(LocalTimerEntityHome timerEntityHome) {
    jbpmContext = JbpmContext.getCurrentJbpmContext();
    if (jbpmContext == null) throw new JbpmException("no active jbpm context");
    this.timerEntityHome = timerEntityHome;
  }

  public void createTimer(Timer timer) {
    if (log.isDebugEnabled()) log.debug("creating " + timer);
    jbpmContext.getJobSession().saveJob(timer);
    jbpmContext.getSession().flush();

    try {
      LocalTimerEntity timerEntity = timerEntityHome.findByPrimaryKey(new Long(timer.getId()));
      timerEntity.createTimer(timer);
    }
    catch (FinderException e) {
      log.error("failed to retrieve entity for " + timer, e);
    }
  }

  public void deleteTimer(Timer timer) {
    if (log.isDebugEnabled()) log.debug("deleting " + timer);
    try {
      LocalTimerEntity timerEntity = timerEntityHome.findByPrimaryKey(new Long(timer.getId()));
      timerEntity.cancelTimer(timer);
    }
    catch (FinderException e) {
      log.error("failed to retrieve entity for " + timer, e);
    }
    jbpmContext.getJobSession().deleteJob(timer);
  }

  public void deleteTimersByName(String timerName, Token token) {
    try {
      Collection timerEntities = timerEntityHome.findByNameAndTokenId(timerName, new Long(token.getId()));
      if (log.isDebugEnabled()) {
        log.debug("found " + timerEntities.size() + " timer entities by name '" + timerName
          + "' for " + token);
      }
      for (Iterator i = timerEntities.iterator(); i.hasNext();) {
        LocalTimerEntity timerEntity = (LocalTimerEntity) i.next();
        timerEntity.cancelTimersByName(timerName, token);
      }
    }
    catch (FinderException e) {
      log.error("failed to retrieve timer entities by name '" + timerName + "' for " + token, e);
    }
    jbpmContext.getJobSession().deleteTimersByName(timerName, token);
  }

  public void deleteTimersByProcessInstance(ProcessInstance processInstance) {
    try {
      Collection timerEntities = timerEntityHome.findByProcessInstanceId(new Long(processInstance.getId()));
      if (log.isDebugEnabled()) {
        log.debug("found " + timerEntities.size() + " timer entities for " + processInstance);
      }
      for (Iterator i = timerEntities.iterator(); i.hasNext();) {
        LocalTimerEntity timerEntity = (LocalTimerEntity) i.next();
        timerEntity.cancelTimersForProcessInstance(processInstance);
      }
    }
    catch (FinderException e) {
      log.error("failed to retrieve timer entities for " + processInstance, e);
    }
    jbpmContext.getJobSession().deleteJobsForProcessInstance(processInstance);
  }

  public void close() {
    // nothing to do here
  }

  private static final Log log = LogFactory.getLog(EntitySchedulerService.class);
}
