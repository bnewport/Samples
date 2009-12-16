package utils;

import java.util.Collection;
import java.util.List;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;

public class ChainedShardEvents implements ObjectGridEventListener, ObjectGridEventGroup.ShardEvents
{
	public List<ObjectGridEventGroup.ShardEvents> getListeners() {
		return listeners;
	}

	public void setListeners(List<ObjectGridEventGroup.ShardEvents> listeners) {
		this.listeners = listeners;
	}

	List<ObjectGridEventGroup.ShardEvents> listeners;
	
	public void shardActivated(ObjectGrid arg0) 
	{
		for(ObjectGridEventGroup.ShardEvents item : listeners)
		{
			item.shardActivated(arg0);
		}
	}

	public void shardDeactivate(ObjectGrid arg0) 
	{
		for(ObjectGridEventGroup.ShardEvents item : listeners)
		{
			item.shardDeactivate(arg0);
		}
	}


	public void destroy() {
	}

	public void initialize(Session arg0) {
	}

	public void transactionBegin(String arg0, boolean arg1) {
	}

	public void transactionEnd(String arg0, boolean arg1, boolean arg2,
			Collection arg3) {
	}

}
