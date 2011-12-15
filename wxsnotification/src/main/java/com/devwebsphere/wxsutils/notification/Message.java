package com.devwebsphere.wxsutils.notification;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Message implements Externalizable {
	protected Message(int pid) {
		partitionId = pid;
	}

	protected Message() {

	}

	public int partitionId;

	public void writeExternal(ObjectOutput out) throws IOException {
		out.write(partitionId);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		partitionId = in.read();
	}
}
