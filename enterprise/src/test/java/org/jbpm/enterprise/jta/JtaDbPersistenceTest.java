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
package org.jbpm.enterprise.jta;

import junit.framework.Test;

import org.hibernate.HibernateException;
import org.hibernate.TransactionException;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.command.CommandService;
import org.jbpm.command.impl.CommandServiceImpl;
import org.jbpm.enterprise.AbstractEnterpriseTestCase;
import org.jbpm.enterprise.IntegrationTestSetup;

public class JtaDbPersistenceTest extends AbstractEnterpriseTestCase {

  public static Test suite() {
    return new IntegrationTestSetup(JtaDbPersistenceTest.class, "enterprise-test.war");
  }

  protected CommandService createCommandService() throws Exception {
    return getName().indexOf("Container") != -1 ? super.createCommandService()
      : new CommandServiceImpl(JbpmConfiguration.getInstance());
  }

  public void testContainerTxSuccess() {
    executeProcess();
  }

  public void testContainerTxFailure() {
    executeProcess();
  }

  public void testContainerTxRollback() {
    executeProcess();
  }

  public void testContainerTxExceptionHandler() {
    executeExceptionProcess();
  }

  public void testUserTxSuccess() {
    executeProcess();
  }

  public void testUserTxFailure() {
    executeProcess();
  }

  public void testUserTxRollback() {
    executeProcess();
  }

  public void testUserTxExceptionHandler() {
    executeExceptionProcess();
  }

  private void executeProcess() {
    deployProcessDefinition("<process-definition name='jta'>"
      + "  <start-state name='start'>"
      + "    <transition to='midway' />"
      + "  </start-state>"
      + "  <state name='midway'>"
      + "    <transition to='end' />"
      + "  </state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    final long processInstanceId = startProcessInstance("jta").getId();
    final String testName = getName();
    try {
      commandService.execute(new Command() {
        private static final long serialVersionUID = 1L;

        public Object execute(JbpmContext jbpmContext) {
          jbpmContext.loadProcessInstance(processInstanceId).signal();
          if (testName.endsWith("Failure")) throw new HibernateException("simulated failure");
          if (testName.endsWith("Rollback")) jbpmContext.setRollbackOnly();
          return null;
        }
      });
    }
    catch (RuntimeException e) {
      assertSame(HibernateException.class, getUltimateCause(e).getClass());
    }
    assertEquals(testName.endsWith("Success"), hasProcessInstanceEnded(processInstanceId));
  }

  private void executeExceptionProcess() {
    deployProcessDefinition("<process-definition name='jbpm2918'>"
      + "  <exception-handler exception-class='" + TransactionException.class.getName() + "'>"
      + "    <action class='org.example.NoSuchAction' />"
      + "  </exception-handler>"
      + "  <start-state name='start'>"
      + "    <transition to='end'>"
      + "      <script>"
      + "        executionContext.jbpmContext.sessionFactory.transactionManager.setRollbackOnly();"
      + "        throw new org.hibernate.TransactionException(\"transaction marked for rollback\");"
      + "      </script>"
      + "    </transition>"
      + "  </start-state>"
      + "  <end-state name='end' />"
      + "</process-definition>");
    try {
      startProcessInstance("jbpm2918");
      fail("expected exception");
    }
    catch (RuntimeException e) {
      assertSame(TransactionException.class, getUltimateCause(e).getClass());
    }
  }

  private static Throwable getUltimateCause(Throwable exception) {
    Throwable cause = exception;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause;
  }
}
