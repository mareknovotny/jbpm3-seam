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
package org.jbpm.scheduler.ejbtimer;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.TimerService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.ejb.LocalCommandService;
import org.jbpm.ejb.LocalCommandServiceHome;
import org.jbpm.ejb.impl.TimerEntityBean;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.util.JndiUtil;

/**
 * Session bean that interacts with the EJB timer service to schedule jBPM {@linkplain Timer
 * timers}.
 * 
 * @deprecated use {@link TimerEntityBean} instead
 */
public class TimerServiceBean implements SessionBean, TimedObject {

  private static final long serialVersionUID = 1L;

  private LocalCommandService commandService;
  private SessionContext sessionContext;

  public void ejbCreate() {
    try {
      LocalCommandServiceHome commandServiceHome = (LocalCommandServiceHome) JndiUtil
        .lookup("java:comp/env/ejb/LocalCommandServiceBean", LocalCommandServiceHome.class);
      commandService = commandServiceHome.create();
    }
    catch (CreateException e) {
      throw new EJBException("command service creation failed", e);
    }
  }

  public void createTimer(org.jbpm.job.Timer timer) {
    TimerService timerService = sessionContext.getTimerService();
    javax.ejb.Timer ejbTimer = timerService.createTimer(timer.getDueDate(), new TimerInfo(timer));
    log.debug("created " + ejbTimer);
  }

  public void cancelTimer(org.jbpm.job.Timer timer) {
    // TODO make the scanning of timers optional by only deleting the timers in the database
    // of course, the corresponding ejb timer notifications have to be ignored
    long timerId = timer.getId();
    Collection timers = sessionContext.getTimerService().getTimers();

    boolean debug = log.isDebugEnabled();
    if (debug) log.debug("examining " + timers.size() + " timers by id " + timerId);

    int count = 0;
    for (Iterator i = timers.iterator(); i.hasNext();) {
      javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
      if (ejbTimer.getInfo() instanceof TimerInfo) {
        TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
        if (timerId == timerInfo.getTimerId()) {
          ejbTimer.cancel();
          ++count;
        }
      }
    }
    if (debug) log.debug("canceled " + count + " timers by id " + timerId);
  }

  public void cancelTimersByName(String timerName, Token token) {
    // TODO make the scanning of timers optional by only deleting the timers in the database
    // of course, the corresponding ejb timer notifications have to be ignored
    Collection timers = sessionContext.getTimerService().getTimers();

    boolean debug = log.isDebugEnabled();
    if (debug) {
      log.debug("examining " + timers.size() + " timers by name '" + timerName + "' for "
        + token);
    }

    int count = 0;
    for (Iterator i = timers.iterator(); i.hasNext();) {
      javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
      if (ejbTimer.getInfo() instanceof TimerInfo) {
        TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
        if (timerInfo.matchesName(timerName, token)) {
          ejbTimer.cancel();
          ++count;
        }
      }
    }
    if (debug) {
      log.debug("canceled " + count + " timers by name '" + timerName + "' for " + token);
    }
  }

  public void cancelTimersForProcessInstance(ProcessInstance processInstance) {
    // TODO make the scanning of timers optional by only deleting the timers in the database
    // of course, the corresponding ejb timer notifications have to be ignored
    Collection timers = sessionContext.getTimerService().getTimers();
    boolean debug = log.isDebugEnabled();
    if (debug) log.debug("examining " + timers.size() + " timers for " + processInstance);

    int count = 0;
    for (Iterator i = timers.iterator(); i.hasNext();) {
      javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
      if (ejbTimer.getInfo() instanceof TimerInfo) {
        TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
        if (timerInfo.matchesProcessInstance(processInstance)) {
          ejbTimer.cancel();
          ++count;
        }
      }
    }
    if (debug) log.debug("canceled " + count + " timers for " + processInstance);
  }

  public void ejbTimeout(javax.ejb.Timer ejbTimer) {
    boolean debug = log.isDebugEnabled();
    if (debug) log.debug(ejbTimer + " fired");

    TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
    Timer timer = (Timer) commandService.execute(new ExecuteTimerCommand(timerInfo.getTimerId()));
    // if timer is repetitive
    if (timer != null && timer.getRepeat() != null) {
      // create a new timer
      if (debug) log.debug("scheduling timer for repeat at " + timer.getDueDate());
      createTimer(timer);
    }
  }

  public void setSessionContext(SessionContext sessionContext) throws EJBException,
    RemoteException {
    this.sessionContext = sessionContext;
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }

  public void ejbRemove() throws EJBException, RemoteException {
    commandService = null;
  }

  private static final Log log = LogFactory.getLog(TimerServiceBean.class);
}
