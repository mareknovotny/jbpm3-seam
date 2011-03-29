package org.jbpm.scheduler.ejbtimer;

import org.jbpm.ejb.LocalTimerEntityHome;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;
import org.jbpm.util.JndiUtil;

/**
 * @author Tom Baeyens
 * @deprecated replaced by {@link EntitySchedulerServiceFactory}
 */
public class EjbSchedulerServiceFactory implements ServiceFactory {

  private static final long serialVersionUID = 1L;

  String timerServiceHomeJndiName = "java:comp/env/ejb/LocalTimerServiceBean";

  private LocalTimerServiceHome timerServiceHome;

  public synchronized LocalTimerServiceHome getTimerServiceHome() {
    if (timerServiceHome == null) {
      timerServiceHome = (LocalTimerServiceHome) JndiUtil.lookup(timerServiceHomeJndiName, LocalTimerEntityHome.class);
    }
    return timerServiceHome;
  }

  public Service openService() {
    return new EjbSchedulerService(getTimerServiceHome());
  }

  public void close() {
    timerServiceHome = null;
  }
}
