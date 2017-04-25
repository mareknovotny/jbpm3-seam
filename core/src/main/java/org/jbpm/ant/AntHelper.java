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
package org.jbpm.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbpm.JbpmConfiguration;
import org.jbpm.db.hibernate.JbpmHibernateConfiguration;
import org.jbpm.util.ClassLoaderUtil;

/**
 * common strategy for jbpm ant tasks to obtain configuration instances.
 */
public class AntHelper {

  private static final Map<Object, JbpmHibernateConfiguration> configurations = new HashMap<>();
  private static final Map<String, JbpmConfiguration> jbpmConfigurations = new HashMap<>();

  private AntHelper() {
    // prevent instantiation
  }

  public static JbpmHibernateConfiguration getConfiguration(String hibernateCfg, String hibernateProperties) {
    Object key = getKey(hibernateCfg, hibernateProperties);
    JbpmHibernateConfiguration jbpmHibernateConfiguration = configurations.get(key);
    if (jbpmHibernateConfiguration == null) {
      jbpmHibernateConfiguration = new JbpmHibernateConfiguration();
      jbpmHibernateConfiguration.getConfigurationProxy().configure(hibernateCfg);

      if (hibernateProperties != null) {
        Properties properties = ClassLoaderUtil.getProperties(hibernateProperties);
        jbpmHibernateConfiguration.getConfigurationProxy().addProperties(properties);
      }
      configurations.put(key, jbpmHibernateConfiguration);
    }
    return jbpmHibernateConfiguration;
  }

  public static JbpmConfiguration getJbpmConfiguration(String jbpmCfg) {
    JbpmConfiguration jbpmConfiguration = jbpmConfigurations.get(jbpmCfg);
    if (jbpmConfiguration == null) {
      if (jbpmCfg == null) {
        jbpmConfiguration = JbpmConfiguration.getInstance();
      }
      else {
        jbpmConfiguration = JbpmConfiguration.getInstance(jbpmCfg);
      }
      jbpmConfigurations.put(jbpmCfg, jbpmConfiguration);
    }
    return jbpmConfiguration;
  }

  static List<String> getKey(String cfg, String properties) {
    List<String> key = new ArrayList<>();
    key.add(cfg);
    key.add(properties);
    return key;
  }
}
