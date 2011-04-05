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
package com.devwebsphere.wxsutils.multijob.ogql;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is returned as the result from the container of processing another chunk
 * of data. It contains the actual results.
 * @author bnewport
 *
 */
public class GridQueryChunk implements Serializable {
	private static final long serialVersionUID = 8258139641862106947L;

	ArrayList<Serializable> result;
}
