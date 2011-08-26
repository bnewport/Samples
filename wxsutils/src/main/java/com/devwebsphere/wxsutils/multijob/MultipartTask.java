package com.devwebsphere.wxsutils.multijob;

import java.io.Serializable;

import com.ibm.websphere.objectgrid.Session;

public interface MultipartTask<V extends Serializable,R>
{
	/**
	 * This is called with the previous task for a partition to create the next one.
	 * @param previousTask This is null for the first task in a partition
	 * @return This should return null when a partition is exhausted
	 */
	SinglePartTask<V, R> createTaskForPartition(SinglePartTask<V, R> previousTask);

	/**
	 * This is called on the client side to extract the actual value to return
	 * to the application from the value returned from process. This allows
	 * some post processing and detecting of conditions like no more data
	 * on the client side.
	 * @param rawRC The value returned from {@link #process(Session)}
	 * @return
	 */
	R extractResult(V rawRC);
}
