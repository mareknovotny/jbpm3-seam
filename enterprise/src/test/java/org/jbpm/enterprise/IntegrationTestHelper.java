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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jbpm.JbpmException;
import org.jbpm.util.JndiUtil;

/**
 * An integration test helper that deals with test deployment/undeployment, etc.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class IntegrationTestHelper {

  private String testResourcesDir;
  private String testArchiveDir;
  private String integrationTarget;

  private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
  private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";

  private static MBeanServerConnection server;

  public String getTestResourcesDir() {
    if (testResourcesDir == null) {
      testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY,
        "target/test-classes");
    }
    return testResourcesDir;
  }

  /** Try to discover the File for the test resource */
  public File getResourceFile(String resource) throws FileNotFoundException {
    File file = new File(resource);
    if (file.exists()) return file;

    file = new File(getTestResourcesDir() + "/" + resource);
    if (file.exists()) return file;

    throw new FileNotFoundException("resource not found: " + resource);
  }

  public String getTestArchiveDir() {
    if (testArchiveDir == null) {
      testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY, "target/test-libs");
    }
    return testArchiveDir;
  }

  /** Try to discover the File for the deployment archive */
  public File getTestArchiveFile(String archive) throws FileNotFoundException {
    File file = new File(archive);
    if (file.exists()) return file;

    file = new File(getTestArchiveDir() + "/" + archive);
    if (file.exists()) return file;

    throw new FileNotFoundException("test archive not found: " + getTestArchiveDir() + "/"
      + archive);
  }

  public void deploy(String archive) throws IOException, DeploymentException {
    URL url = getTestArchiveFile(archive).toURI().toURL();
    deploy(url);
  }

  public void deploy(URL archive) throws DeploymentException {
    getDeployer().deploy(archive);
  }

  public void undeploy(String archive) throws IOException, DeploymentException {
    URL url = getTestArchiveFile(archive).toURI().toURL();
    undeploy(url);
  }

  public void undeploy(URL archive) throws DeploymentException {
    getDeployer().undeploy(archive);
  }

  public String getIntegrationTarget() {
    if (integrationTarget == null) {
      String jbossVersion;
      try {
        ObjectName oname = ObjectNameFactory.create("jboss.system:type=Server");
        jbossVersion = (String) getServer().getAttribute(oname, "Version");
      }
      catch (JMException e) {
        throw new JbpmException("failed to determine jboss version", e);
      }
      catch (IOException e) {
        throw new JbpmException("failed to determine jboss version", e);
      }

      if (jbossVersion.startsWith("5.1.0"))
        integrationTarget = "jboss510";
      else if (jbossVersion.startsWith("5.0.1"))
        integrationTarget = "jboss501";
      else if (jbossVersion.startsWith("4.2.3"))
        integrationTarget = "jboss423";
      else if (jbossVersion.startsWith("4.0.5"))
        integrationTarget = "jboss405";
      else
        throw new JbpmException("unsupported jboss version: " + jbossVersion);
    }
    return integrationTarget;
  }

  public MBeanServerConnection getServer() {
    if (server == null) {
      server = (MBeanServerConnection) JndiUtil.lookup("jmx/invoker/RMIAdaptor",
        MBeanServerConnection.class);
    }
    return server;
  }

  private ArchiveDeployer getDeployer() {
    return new JBossArchiveDeployer(getServer());
  }
}
