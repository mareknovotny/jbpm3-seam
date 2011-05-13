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
package org.jbpm.db;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Job;
import org.jbpm.job.Timer;
import org.jbpm.persistence.JbpmPersistenceException;

public class JobSession {

  private final Session session;

  public JobSession(Session session) {
    this.session = session;
  }

  public Job getFirstAcquirableJob(String lockOwner) {
    try {
      Query query;
      if (lockOwner == null) {
        query = session.getNamedQuery("JobSession.getFirstUnownedAcquirableJob");
      }
      else {
        query = session.getNamedQuery("JobSession.getFirstAcquirableJob")
          .setString("lockOwner", lockOwner);
      }
      return (Job) query.setTimestamp("now", new Date()).setMaxResults(1).uniqueResult();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not get first acquirable job", e);
    }
  }

  public List findExclusiveJobs(String lockOwner, ProcessInstance processInstance) {
    try {
      return session.getNamedQuery("JobSession.findExclusiveJobs")
        .setString("lockOwner", lockOwner)
        .setTimestamp("now", new Date())
        .setParameter("processInstance", processInstance)
        .list();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not find exclusive jobs owned by '" + lockOwner
        + "' for " + processInstance, e);
    }
  }

  public List findJobsByToken(Token token) {
    try {
      return session.getNamedQuery("JobSession.findJobsByToken")
        .setParameter("token", token)
        .list();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not find jobs for " + token, e);
    }
  }

  public Job getFirstDueJob(String lockOwner, Collection monitoredJobs) {
    try {
      Query query;
      if (lockOwner == null) {
        query = session.getNamedQuery("JobSession.getFirstUnownedDueJob");
      }
      else if (monitoredJobs == null || monitoredJobs.isEmpty()) {
        query = session.getNamedQuery("JobSession.getFirstDueJob")
          .setString("lockOwner", lockOwner);
      }
      else {
        query = session.getNamedQuery("JobSession.getFirstDueJobExcludingMonitoredJobs")
          .setString("lockOwner", lockOwner)
          .setParameterList("monitoredJobIds", monitoredJobs);
      }
      return (Job) query.setMaxResults(1).uniqueResult();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not get first due job owned by '" + lockOwner
        + "' ignoring jobs " + monitoredJobs, e);
    }
  }

  public void saveJob(Job job) {
    try {
      session.save(job);
      if (job instanceof Timer) {
        Timer timer = (Timer) job;
        Action action = timer.getAction();
        // if action is transient, save it
        if (action != null && action.getId() == 0L) session.save(action);
      }
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not save " + job, e);
    }
  }

  public void deleteJob(Job job) {
    try {
      session.delete(job);
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not delete " + job, e);
    }
  }

  public Job loadJob(long jobId) {
    try {
      return (Job) session.load(Job.class, new Long(jobId));
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not load job " + jobId, e);
    }
  }

  /**
   * Reattach job originally loaded in a previous session.
   * 
   * @param job a detached job
   * @see <a href="http://tinyurl.com/kjss69">Detached objects and automatic versioning</a>
   */
  public void reattachJob(Job job) {
    try {
      session.lock(job, LockMode.NONE);
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not reattach " + job, e);
    }
  }

  public Timer loadTimer(long timerId) {
    try {
      return (Timer) session.load(Timer.class, new Long(timerId));
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not load timer " + timerId, e);
    }
  }

  public Job getJob(long jobId) {
    try {
      return (Job) session.get(Job.class, new Long(jobId));
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not get job " + jobId, e);
    }
  }

  public void suspendJobs(Token token) {
    try {
      session.getNamedQuery("JobSession.suspendJobs")
        .setParameter("token", token)
        .executeUpdate();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not suspend jobs for " + token, e);
    }
  }

  public void resumeJobs(Token token) {
    try {
      session.getNamedQuery("JobSession.resumeJobs")
        .setParameter("token", token)
        .executeUpdate();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not resume jobs for " + token, e);
    }
  }

  public void deleteTimersByName(String name, Token token) {
    try {
      // delete unowned timers
      session.getNamedQuery("JobSession.deleteTimersByName")
        .setString("name", name)
        .setParameter("token", token)
        .executeUpdate();

      // prevent further repetitions
      List timers = session.getNamedQuery("JobSession.findRepeatingTimersByName")
        .setString("name", name)
        .setParameter("token", token)
        .list();
      preventFurtherRepetitions(timers);
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not delete timers by name '" + name + "' for "
        + token, e);
    }
  }

  public int countDeletableJobsForProcessInstance(ProcessInstance processInstance) {
    Number jobCount = (Number) session.getNamedQuery("JobSession.countDeletableJobsForProcessInstance")
      .setParameter("processInstance", processInstance)
      .uniqueResult();
    return jobCount.intValue();
  }

  public void deleteJobsForProcessInstance(ProcessInstance processInstance) {
    try {
      // delete unowned node-execute-jobs and timers
      session.getNamedQuery("JobSession.deleteJobsForProcessInstance")
        .setParameter("processInstance", processInstance)
        .executeUpdate();

      // prevent further repetitions
      List timers = session.getNamedQuery("JobSession.findRepeatingTimersForProcessInstance")
        .setParameter("processInstance", processInstance)
        .list();
      preventFurtherRepetitions(timers);
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not delete jobs for " + processInstance, e);
    }
  }

  private static void preventFurtherRepetitions(List timers) {
    if (!timers.isEmpty()) {
      for (Iterator i = timers.iterator(); i.hasNext();) {
        Timer timer = (Timer) i.next();
        timer.setRepeat(null);
      }
    }
  }

  public List findJobsWithOverdueLockTime(Date threshold) {
    try {
      return session.getNamedQuery("JobSession.findJobsWithOverdueLockTime")
        .setTimestamp("threshold", threshold)
        .list();
    }
    catch (HibernateException e) {
      throw new JbpmPersistenceException("could not find jobs with lock time over " + threshold,
        e);
    }
  }

  public List loadJobs(long[] jobIds) {
    int jobCount = jobIds.length;
    Long[] jobs = new Long[jobCount];
    for (int i = 0; i < jobCount; i++) {
      jobs[i] = new Long(jobIds[i]);
    }
    return session.createCriteria(Job.class).add(Restrictions.in("id", jobs)).list();
  }

  public void releaseLockedJobs(final String lockOwner) {
    try {
      session.getNamedQuery("JobSession.releaseLockedJobs")
        .setString("lockOwner", lockOwner)
        .executeUpdate();
    } catch (HibernateException e) {
      throw new JbpmPersistenceException("could not release locked jobs by owner '" + lockOwner + "'", e);
    }
  }
}
