//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.jmx.loader;

import java.util.List;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.jpa.JPALoader;
import com.ibm.websphere.objectgrid.plugins.LoaderException;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.OptimisticCollisionException;

/**
 * This is a wrapper for JPALoader to add the WXSUtils LoaderMBean support.
 *
 */
public class JMXJPALoader extends JPALoader 
{
	@Override
	public void batchUpdate(TxID tx, LogSequence ls)
			throws LoaderException, OptimisticCollisionException 
	{
		long start = System.nanoTime();
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(tx.getSession().getObjectGrid().getName(), getEntityClassName());
		super.batchUpdate(tx, ls);
		mbean.getBatchSizeMetrics().logTime(ls.size());
		mbean.getBatchUpdateMetrics().logTime(System.nanoTime() - start);
		mbean.getBatchSizeMetrics().logTime(ls.size());
		mbean.recordOperationRows(ls);
	}

	@Override
	public List get(TxID tx, List keys, boolean b) throws LoaderException 
	{
		long start = System.nanoTime();
		LoaderMBeanImpl mbean = WXSUtils.getLoaderMBeanManager().getBean(tx.getSession().getObjectGrid().getName(), getEntityClassName());
		List rc = super.get(tx, keys, b);
		mbean.getGetMetrics().logTime(System.nanoTime() - start);
		mbean.getGetSizeMetrics().logTime(keys.size());
		return rc;
	}
}
