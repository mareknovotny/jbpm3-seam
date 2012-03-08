package org.jbpm.persistence.db;

import javax.transaction.Synchronization;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.engine.transaction.spi.LocalStatus;

public class MockTransaction implements Transaction {

  boolean wasCommitted = false;
  boolean wasRolledBack = false;

  public void begin() throws HibernateException {
  }

  public void commit() throws HibernateException {
    wasCommitted = true;
  }

  public void rollback() throws HibernateException {
    wasRolledBack = true;
  }

  public boolean wasCommitted() throws HibernateException {
    return wasCommitted;
  }

  public boolean wasRolledBack() throws HibernateException {
    return wasRolledBack;
  }

  public boolean isActive() throws HibernateException {
    return (!wasCommitted) && (!wasRolledBack);
  }

  public void registerSynchronization(Synchronization synchronization) throws HibernateException {
  }

  public void setTimeout(int seconds) {
  }

	public boolean isInitiator() {
		return false;
	}

	public LocalStatus getLocalStatus() {
		if (isActive())
		{
			return LocalStatus.ACTIVE;
		}
		else if (!isActive())
		{
			return LocalStatus.NOT_ACTIVE;
		}
		else if (wasRolledBack())
		{
			return LocalStatus.ROLLED_BACK;
		}
		else 
		{
			return LocalStatus.COMMITTED;
		}
	}

	public boolean isParticipating() 
	{
		return false;
	}

	public int getTimeout() {
		return 0;
	}

}
