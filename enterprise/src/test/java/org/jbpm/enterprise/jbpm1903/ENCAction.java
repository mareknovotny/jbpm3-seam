package org.jbpm.enterprise.jbpm1903;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class ENCAction implements ActionHandler {

  private static final long serialVersionUID = 1L;

  public void execute(ExecutionContext executionContext) throws Exception {
    Context initialContext = new InitialContext();
    // log jndi environment
    LogFactory.getLog(ENCAction.class).info(initialContext.getEnvironment());
    try {
      Object queue = initialContext.lookup("java:comp/env/jms/JobQueue");
      executionContext.setVariable("queue", queue);
    }
    finally {
      initialContext.close();
    }
    executionContext.leaveNode();
  }

}
