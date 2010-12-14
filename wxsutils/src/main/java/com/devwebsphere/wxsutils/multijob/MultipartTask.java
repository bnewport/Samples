package com.devwebsphere.wxsutils.multijob;
//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//



public interface MultipartTask<V,R>
{
	/**
	 * This is called with the previous task for a partition to create the next one.
	 * @param previousTask This is null for the first task in a partition
	 * @return This should return null when a partition is exhausted
	 */
	SinglePartTask<V, R> createTaskForPartition(SinglePartTask<V, R> previousTask);
}
