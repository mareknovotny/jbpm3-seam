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

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

/**
 * A test setup that deploys/undeploys archives
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class IntegrationTestSetup extends TestSetup {

  private final IntegrationTestHelper helper = new IntegrationTestHelper();
  private final String[] archives;

  public IntegrationTestSetup(Class testClass, String archiveList) {
    super(new TestSuite(testClass));
    archives = archiveList == null ? new String[0] : archiveList.split("[\\s,]+");
  }

  public IntegrationTestHelper getHelper() {
    return helper;
  }

  protected void setUp() throws Exception {
    super.setUp();

    for (int i = 0; i < archives.length; i++) {
      String archive = archives[i];
      try {
        helper.deploy(archive);
      }
      catch (DeploymentException ex) {
        helper.undeploy(archive);
        throw ex;
      }
    }
  }

  protected void tearDown() throws Exception {
    for (int i = 0; i < archives.length; i++) {
      String archive = archives[archives.length - i - 1];
      helper.undeploy(archive);
    }
    super.tearDown();
  }
}
