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
package com.devwebsphere.wxs.asyncserviceimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;

public class ChainedShardEvents implements ObjectGridEventListener, ObjectGridEventGroup.ShardEvents {
	List<ObjectGridEventGroup.ShardEvents> listeners = new ArrayList<ObjectGridEventGroup.ShardEvents>();

	public ChainedShardEvents() {
		listeners.add(new AsyncServiceProcessor<Boolean>());
	}

	public List<ObjectGridEventGroup.ShardEvents> getListeners() {
		return listeners;
	}

	public void setListeners(List<ObjectGridEventGroup.ShardEvents> listeners) {
		this.listeners = listeners;
	}

	public void shardActivated(ObjectGrid arg0) {
		for (ObjectGridEventGroup.ShardEvents item : listeners) {
			item.shardActivated(arg0);
		}
	}

	public void shardDeactivate(ObjectGrid arg0) {
		for (ObjectGridEventGroup.ShardEvents item : listeners) {
			item.shardDeactivate(arg0);
		}
	}

	public void destroy() {
	}

	public void initialize(Session arg0) {
	}

	public void transactionBegin(String arg0, boolean arg1) {
	}

	public void transactionEnd(String arg0, boolean arg1, boolean arg2, Collection arg3) {
	}

}
