package org.jbpm.persistence.db;

import javax.transaction.Synchronization;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

public class MockTransaction implements Transaction {

  boolean wasCommitted = false;
  boolean wasRolledBack = false;

  @Override
  public void begin() throws HibernateException {
      //
  }

  @Override
  public void commit() throws HibernateException {
    wasCommitted = true;
  }

  @Override
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

  @Override
  public void registerSynchronization(Synchronization synchronization) throws HibernateException {
      //
  }

  @Override
  public void setTimeout(int seconds) {
      throw new UnsupportedOperationException();
  }

  @Override
  public int getTimeout() {
      return 0;
  }

  @Override
  public TransactionStatus getStatus() {
    if (isActive()) {
        return TransactionStatus.ACTIVE;
    } else if (!isActive()) {
        return TransactionStatus.NOT_ACTIVE;
    } else if (wasRolledBack()) {
        return TransactionStatus.ROLLED_BACK;
    } else {
        return TransactionStatus.COMMITTED;
    }
  }

  @Override
  public void markRollbackOnly()
  {
      throw new UnsupportedOperationException();
  }

}
