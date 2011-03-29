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
package org.jbpm.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Environment;

import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.command.CommandService;
import org.jbpm.command.DeleteProcessDefinitionCommand;
import org.jbpm.command.DeployProcessCommand;
import org.jbpm.command.GetProcessInstanceCommand;
import org.jbpm.command.SignalCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.ejb.LocalCommandServiceHome;
import org.jbpm.graph.def.EventCallback;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.db.DbPersistenceService;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.persistence.db.StaleObjectLogConfigurer;
import org.jbpm.svc.Services;

public abstract class AbstractEnterpriseTestCase extends ServletTestCase {

  protected CommandService commandService;

  private List processDefinitions = new ArrayList();
  private final Log log = LogFactory.getLog(getClass());

  protected AbstractEnterpriseTestCase() {
  }

  protected void setUp() throws Exception {
    commandService = createCommandService();
    log.info("### " + getName() + " started ###");
  }

  protected void tearDown() throws Exception {
    log.info("### " + getName() + " done ###");
    for (Iterator i = processDefinitions.iterator(); i.hasNext();) {
      ProcessDefinition processDefinition = (ProcessDefinition) i.next();
      deleteProcessDefinition(processDefinition.getId());
    }
    commandService = null;
    EventCallback.clear();
  }

  protected CommandService createCommandService() throws Exception {
    Object bean = new InitialContext().lookup("java:comp/env/ejb/CommandServiceBean");

    CommandService commandService;
    if (bean instanceof CommandService) {
      // compatibility with EJB3 homeless beans
      commandService = (CommandService) bean;
    }
    else {
      LocalCommandServiceHome home = (LocalCommandServiceHome) bean;
      commandService = home.create();
    }

    return new RetryCommandService(commandService);
  }

  protected ProcessDefinition deployProcessDefinition(String xml) {
    ProcessDefinition processDefinition = (ProcessDefinition) commandService.execute(new DeployProcessCommand(xml));
    processDefinitions.add(processDefinition);
    return processDefinition;
  }

  protected ProcessDefinition deployProcessDefinition(byte[] processArchive) {
    ProcessDefinition processDefinition = (ProcessDefinition) commandService.execute(new DeployProcessCommand(processArchive));
    processDefinitions.add(processDefinition);
    return processDefinition;
  }

  protected ProcessInstance startProcessInstance(String processName) {
    StartProcessInstanceCommand command = new StartProcessInstanceCommand();
    command.setProcessDefinitionName(processName);
    command.setVariables(Collections.singletonMap("eventCallback", new EventCallback()));
    return (ProcessInstance) commandService.execute(command);
  }

  protected void signalToken(long tokenId) {
    commandService.execute(new SignalCommand(tokenId, null));
  }

  protected boolean hasProcessInstanceEnded(final long processInstanceId) {
    ProcessInstance processInstance = (ProcessInstance) commandService.execute(new GetProcessInstanceCommand(processInstanceId));
    return processInstance.hasEnded();
  }

  protected Object getVariable(final long processInstanceId, final String variableName) {
    return commandService.execute(new Command() {
      private static final long serialVersionUID = 1L;

      public Object execute(JbpmContext jbpmContext) throws Exception {
        ProcessInstance processInstance = jbpmContext.loadProcessInstance(processInstanceId);
        return processInstance.getContextInstance().getVariable(variableName);
      }
    });
  }

  protected String getHibernateDialect() {
    return (String) commandService.execute(new Command() {
      private static final long serialVersionUID = 1L;

      public Object execute(JbpmContext jbpmContext) throws Exception {
        DbPersistenceServiceFactory factory = (DbPersistenceServiceFactory) jbpmContext.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
        return factory.getConfiguration().getProperty(Environment.DIALECT);
      }
    });
  }

  private void deleteProcessDefinition(long processDefinitionId) throws Exception {
    commandService.execute(new DeleteProcessDefinitionCommand(processDefinitionId));
  }

  private static final class RetryCommandService implements CommandService {

    private final CommandService delegate;

    RetryCommandService(CommandService delegate) {
      this.delegate = delegate;
    }

    public Object execute(Command command) {
      RuntimeException lockingException = null;

      for (int i = 0; i < 3; i++) {
        try {
          return delegate.execute(command);
        }
        catch (RuntimeException e) {
          if (!DbPersistenceService.isLockingException(e)) throw e;
          // if this is a locking exception, keep it quiet
          StaleObjectLogConfigurer.getStaleObjectExceptionsLog().error("failed to execute "
            + command);
          lockingException = e;
        }
      }

      throw lockingException;
    }
  }

}