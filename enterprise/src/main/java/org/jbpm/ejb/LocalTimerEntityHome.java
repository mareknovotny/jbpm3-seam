package org.jbpm.ejb;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface LocalTimerEntityHome extends EJBLocalHome {

	public LocalTimerEntity create() throws CreateException;

	public LocalTimerEntity findByPrimaryKey(Long timerId) throws FinderException;

	public Collection findByNameAndTokenId(String name, Long tokenId)
			throws FinderException;

	public Collection findByProcessInstanceId(Long processInstanceId) throws FinderException;
}
