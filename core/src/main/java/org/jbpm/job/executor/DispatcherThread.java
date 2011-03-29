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
package org.jbpm.job.executor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jbpm.JbpmContext;
import org.jbpm.job.Job;

/**
 * Acquires jobs and then dispatches them to the job executor thread pool.
 * 
 * @author Alejandro Guizar
 */
class DispatcherThread extends Thread implements Deactivable {

  private final JobExecutor jobExecutor;
  private volatile boolean active = true;

  static final String DEFAULT_NAME = "Dispatcher";
  private static final Log log = LogFactory.getLog(DispatcherThread.class);

  DispatcherThread(JobExecutor jobExecutor) {
    this(DEFAULT_NAME, jobExecutor);
  }

  DispatcherThread(String name, JobExecutor jobExecutor) {
    super(jobExecutor.getThreadGroup(), name);
    this.jobExecutor = jobExecutor;
  }

  public void run() {
    int retryInterval = jobExecutor.getRetryInterval();
    while (active) {
      // acquire job; on exception, call returns null
      Job job = acquireJob();
      // submit job
      if (job != null) submitJob(job);

      // if still active, wait or sleep
      if (active) {
        try {
          if (job != null) {
            // reset the current retry interval
            retryInterval = jobExecutor.getRetryInterval();
            // wait for next due job
            long waitPeriod = getWaitPeriod(jobExecutor.getIdleInterval());
            if (waitPeriod > 0) {
              synchronized (jobExecutor) {
                jobExecutor.wait(waitPeriod);
              }
            }
          }
          else {
            // sleep instead of waiting on jobExecutor
            // to prevent message/scheduler service from waking up this thread
            sleep(retryInterval);
            // after an exception, double the current retry interval
            // to avoid continuous failures during anomalous conditions
            retryInterval *= 2;
            // enforce maximum idle interval
            int maxIdleInterval = jobExecutor.getMaxIdleInterval();
            if (retryInterval > maxIdleInterval || retryInterval < 0) {
              retryInterval = maxIdleInterval;
            }
          }
        }
        catch (InterruptedException e) {
          if (log.isDebugEnabled()) log.debug(getName() + " got interrupted");
        }
      }
    }
    log.info(getName() + " leaves cyberspace");
  }

  private Job acquireJob() {
    Job job = null;
    boolean debug = log.isDebugEnabled();
    // acquire job executor's monitor before creating context and allocating resources
    synchronized (jobExecutor) {
      JbpmContext jbpmContext = jobExecutor.getJbpmConfiguration().createJbpmContext();
      try {
        // look for available job
        Job firstJob = jbpmContext.getJobSession().getFirstAcquirableJob(null);
        // is there a job?
        if (firstJob != null) {
          // lock job
          firstJob.setLockOwner(getName());
          firstJob.setLockTime(new Date());
          // has job failed previously?
          if (firstJob.getException() != null) {
            // decrease retry count
            int retries = firstJob.getRetries() - 1;
            firstJob.setRetries(retries);
            if (debug) log.debug(firstJob + " has " + retries + " retries remaining");
          }
          // deliver result
          if (debug) log.debug("acquired " + firstJob);
          job = firstJob;
        }
        else if (debug) log.debug("no acquirable job found");
      }
      catch (RuntimeException e) {
        jbpmContext.setRollbackOnly();
        if (debug) log.debug("failed to acquire job", e);
      }
      catch (Error e) {
        jbpmContext.setRollbackOnly();
        throw e;
      }
      finally {
        try {
          jbpmContext.close();
        }
        catch (RuntimeException e) {
          job = null;
          if (debug) log.debug("failed to acquire job", e);
        }
      }
    }
    return job;
  }

  private void submitJob(Job job) {
    try {
      jobExecutor.getQueue().put(job);
    }
    catch (InterruptedException e) {
      unlockJob(job);
      if (log.isDebugEnabled()) log.debug(getName() + " got interrupted");
    }
  }

  private void unlockJob(Job job) {
    JbpmContext jbpmContext = jobExecutor.getJbpmConfiguration().createJbpmContext();
    try {
      // reattach job to persistence context
      jbpmContext.getJobSession().reattachJob(job);

      // unlock job so it can be dispatched again
      job.setLockOwner(null);
      job.setLockTime(null);
    }
    catch (RuntimeException e) {
      jbpmContext.setRollbackOnly();
      log.warn("failed to unlock " + job, e);
    }
    catch (Error e) {
      jbpmContext.setRollbackOnly();
      throw e;
    }
    finally {
      try {
        jbpmContext.close();
      }
      catch (RuntimeException e) {
        log.warn("failed to unlock " + job, e);
      }
    }
  }

  private long getWaitPeriod(int currentIdleInterval) {
    Date nextDueDate = getNextDueDate();
    if (nextDueDate != null) {
      long waitPeriod = nextDueDate.getTime() - System.currentTimeMillis();
      if (waitPeriod < currentIdleInterval) return waitPeriod;
    }
    return currentIdleInterval;
  }

  private Date getNextDueDate() {
    Date nextDueDate = null;
    JbpmContext jbpmContext = jobExecutor.getJbpmConfiguration().createJbpmContext();
    try {
      Job job = jbpmContext.getJobSession().getFirstDueJob(null, null);
      if (job != null) {
        nextDueDate = job.getDueDate();
      }
      else if (log.isDebugEnabled()) log.debug("no due job found");
    }
    catch (RuntimeException e) {
      jbpmContext.setRollbackOnly();
      if (log.isDebugEnabled()) log.debug("failed to determine next due date", e);
    }
    catch (Error e) {
      jbpmContext.setRollbackOnly();
      throw e;
    }
    finally {
      try {
        jbpmContext.close();
      }
      catch (RuntimeException e) {
        nextDueDate = null;
        if (log.isDebugEnabled()) log.debug("failed to determine next due date", e);
      }
    }
    return nextDueDate;
  }

  public void deactivate() {
    if (active) {
      active = false;
      interrupt();
    }
  }
}
