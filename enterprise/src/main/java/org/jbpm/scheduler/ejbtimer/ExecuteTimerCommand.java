package org.jbpm.scheduler.ejbtimer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.job.Timer;

public class ExecuteTimerCommand implements Command {

  private final long timerId;

  private static final long serialVersionUID = 1L;

  public ExecuteTimerCommand(long timerId) {
    this.timerId = timerId;
  }

  public Object execute(JbpmContext jbpmContext) throws Exception {
    Timer timer = acquireTimer(timerId, jbpmContext);
    if (timer != null) executeTimer(timer, jbpmContext);
    return timer;
  }

  private static Timer acquireTimer(long timerId, JbpmContext jbpmContext) {
    boolean debug = log.isDebugEnabled();
    if (debug) log.debug("acquiring timer: " + timerId);

    Timer timer = (Timer) jbpmContext.getSession().get(Timer.class, new Long(timerId));
    // timer could have been deleted manually
    // or by ending the process instance
    if (timer != null) {
      // register process instance for automatic save
      // see https://jira.jboss.org/jira/browse/JBPM-1015
      ProcessInstance processInstance = timer.getProcessInstance();
      jbpmContext.addAutoSaveProcessInstance(processInstance);

      // mark timer as locked to prevent it from being deleted
      timer.setLockOwner(Thread.currentThread().getName());
      if (debug) log.debug("acquired " + timer);
    }
    else if (debug) {
      log.debug("timer not found: " + timerId);
    }

    return timer;
  }

  private static void executeTimer(Timer timer, JbpmContext jbpmContext) throws Exception {
    if (log.isDebugEnabled()) log.debug("executing " + timer);
    if (timer.execute(jbpmContext)) {
      jbpmContext.getServices().getSchedulerService().deleteTimer(timer);
    }
  }

  private static final Log log = LogFactory.getLog(ExecuteTimerCommand.class);
}
