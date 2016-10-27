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
package org.jbpm.identity.hibernate;

import java.sql.Connection;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jbpm.db.hibernate.JbpmHibernateConfiguration;
import org.jbpm.identity.Group;
import org.jbpm.identity.Membership;
import org.jbpm.identity.User;

public class IdentitySessionFactory {

  protected JbpmHibernateConfiguration jbpmHibernateConfiguration;
  protected SessionFactory sessionFactory;

  public IdentitySessionFactory() {
    this(createConfiguration());
  }

  public IdentitySessionFactory(JbpmHibernateConfiguration jbpmHibernateConfiguration) {
    this(jbpmHibernateConfiguration, jbpmHibernateConfiguration.getConfigurationProxy().buildSessionFactory());
  }

  public IdentitySessionFactory(JbpmHibernateConfiguration jbpmHibernateConfiguration, SessionFactory sessionFactory) {
    this.jbpmHibernateConfiguration = jbpmHibernateConfiguration;
    this.sessionFactory = sessionFactory;
  }

  public static JbpmHibernateConfiguration createConfiguration() {
    return createConfiguration(null);
  }

  public static JbpmHibernateConfiguration createConfiguration(String resource) {

    JbpmHibernateConfiguration jbpmHibernateConfiguration = new JbpmHibernateConfiguration();
    Configuration configuration = jbpmHibernateConfiguration.getConfigurationProxy();
    if (resource != null) {
      configuration.configure(resource);
    }
    else {
      configuration.configure();
    }

    return jbpmHibernateConfiguration;
  }

  public IdentitySession openIdentitySession() {
    return new IdentitySession(sessionFactory.openSession());
  }

  public void evictCachedIdentities() {
    sessionFactory.getCache().evictEntityRegion(User.class);
    sessionFactory.getCache().evictEntityRegion(Membership.class);
    sessionFactory.getCache().evictEntityRegion(Group.class);
  }

  public JbpmHibernateConfiguration getJbpmHibernateConfiguration() {
    return jbpmHibernateConfiguration;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
