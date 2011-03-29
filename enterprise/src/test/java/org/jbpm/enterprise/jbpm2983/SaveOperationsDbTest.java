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
package org.jbpm.enterprise.jbpm2983;

import javax.transaction.UserTransaction;

import junit.framework.Test;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.command.Command;
import org.jbpm.command.CommandService;
import org.jbpm.enterprise.AbstractEnterpriseTestCase;
import org.jbpm.enterprise.IntegrationTestSetup;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.jta.JtaDbPersistenceServiceFactory;
import org.jbpm.svc.Services;

/**
 * Check if transaction is active before performing save operations.
 * 
 * @see <a href="https://jira.jboss.org/browse/JBPM-2983">JBPM-2983</a>
 * @author Alejandro Guizar
 */
public class SaveOperationsDbTest extends AbstractEnterpriseTestCase {

  public static Test suite() {
    return new IntegrationTestSetup(SaveOperationsDbTest.class, "enterprise-test.war");
  }

  protected CommandService createCommandService() throws Exception {
    return new CommandService() {
      public Object execute(Command command) {
        JbpmContext jbpmContext = JbpmConfiguration.getInstance().createJbpmContext();
        UserTransaction tx = null;
        try {
          JtaDbPersistenceServiceFactory persistenceServiceFactory =
            (JtaDbPersistenceServiceFactory) jbpmContext.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
          tx = persistenceServiceFactory.getUserTransaction();
          tx.begin();

          Object result = command.execute(jbpmContext);
          tx.commit();
          return result;
        }
        catch (Exception e) {
          if (tx != null) {
            try {
              tx.rollback();
            }
            catch (Exception re) {
              // ignore
            }
          }
          throw e instanceof RuntimeException ? (RuntimeException) e : new JbpmException(e);
        }
        finally {
          jbpmContext.close();
        }
      }
    };
  }

  public void testSaveOperations() {
    deployProcessDefinition("<?xml version='1.0'?>"
      + "<process-definition name='jbpm2983'>"
      + "  <start-state name='start'>"
      + "    <transition to='midway' />"
      + "  </start-state>"
      + "  <state name='midway'>"
      + "    <transition to='end'>"
      + "      <script>"
      + "      throw new UnsupportedOperationException(\"you did not see me coming\");"
      + "      </script>"
      + "    </transition>"
      + "  </state>"
      + "  <end-state name='end' />"
      + "</process-definition>");

    ProcessInstance processInstance = startProcessInstance("jbpm2983");
    try {
      signalToken(processInstance.getRootToken().getId());
      fail("expected exception");
    }
    catch (DelegationException e) {
      assert e.getCause() instanceof UnsupportedOperationException : e.getCause();
    }

    long processInstanceId = processInstance.getId();
    assertTrue("expected process instance " + processInstanceId + " to have ended",
      !hasProcessInstanceEnded(processInstanceId));
  }
}
