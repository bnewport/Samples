package com.devwebsphere.wxsutils.continuousfilter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import com.devwebsphere.wxsutils.notification.Message;

public class CFMessage extends Message {
	public enum Operation {
		CLEAR, SET, ADD, REMOVE
	};

	Operation operation;
	Object[] keys;

	public CFMessage(int partitionId, Operation o, List<Object> keys) {
		super(partitionId);
		operation = o;
		if (keys != null && keys.size() > 0) {
			Object[] objs = keys.toArray(new Object[keys.size()]);
			this.keys = objs;
		}
	}

	public CFMessage() {

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(operation);
		out.writeObject(keys);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		operation = (Operation) in.readObject();
		keys = (Object[]) in.readObject();
	}

}
