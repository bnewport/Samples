//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2012
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.filter.path;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.devwebsphere.wxsutils.filter.ValuePath;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.plugins.io.ValueSerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContext;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContextAware;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.SerializedValue;

public class SerializedValuePath implements ValuePath, Externalizable, DataObjectContextAware {
	String propertyName;

	public SerializedValuePath() {
	}

	public SerializedValuePath(String propertyName) {
		this.propertyName = propertyName;
	}

	public Object get(Object fo) {
		SerializedValue sv = (SerializedValue) fo;
		DataObjectContext dCtx = sv.getContext();
		ValueSerializerPlugin vsp = dCtx.getSerializerAccessor().getMapSerializerPlugin().getValueSerializerPlugin();
		Object attrCtx = vsp.getAttributeContexts(propertyName);
		try {
			return vsp.inflateDataObjectAttributes(dCtx, sv.getInputStream(), attrCtx)[0];
		} catch (IOException ioe) {
			throw new ObjectGridRuntimeException(ioe);
		}
	}

	public String toString() {
		return "getAttribute(" + propertyName + ")";
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		propertyName = in.readUTF();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(propertyName);
	}

	@Override
	public void applyContext(DataObjectContext doc) {
	}

	@Override
	public DataObjectContext getContext() {
		return null;
	}
}
