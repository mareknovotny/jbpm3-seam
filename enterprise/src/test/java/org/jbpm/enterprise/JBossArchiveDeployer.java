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

import java.io.IOException;
import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * An archive deployer that deals with test deployment/undeployment, etc.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class JBossArchiveDeployer implements ArchiveDeployer {

  private MBeanServerConnection server;

  private static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

  public JBossArchiveDeployer(MBeanServerConnection server) {
    this.server = server;
  }

  public void deploy(URL url) throws DeploymentException {
    invokeMainDeployer("deploy", url);
  }

  public void undeploy(URL url) throws DeploymentException {
    invokeMainDeployer("undeploy", url);
  }

  private void invokeMainDeployer(String methodName, URL url) throws DeploymentException {
    try {
      server.invoke(new ObjectName(MAIN_DEPLOYER), methodName, new Object[] { url },
        new String[] { "java.net.URL" });
    }
    catch (JMException e) {
      throw new DeploymentException(methodName + " failed", e);
    }
    catch (IOException e) {
      throw new DeploymentException(methodName + " failed", e);
    }
  }
}
