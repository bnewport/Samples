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
package com.devwebsphere.wxsutils.filter.path;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.filter.ValuePath;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This fetches the named attribute using the appropriately named getter method on the class. The attribute name MUST be
 * capitalized. If the field doesn't exist then null is returned
 * 
 * @author bnewport
 * 
 */
public class PojoPropertyPath implements ValuePath, Externalizable {
	static Logger logger = Logger.getLogger(PojoPropertyPath.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 5874273314940302023L;
	String propertyName;

	public PojoPropertyPath() {
	}

	public PojoPropertyPath(String propertyName) {
		try {
			this.propertyName = propertyName;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object get(Object fo) {
		try {
			Method getMethod = fo.getClass().getMethod("get" + propertyName, (Class<?>[]) null);
			return getMethod.invoke(fo);
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public String toString() {
		return ".get" + propertyName + "()";
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		propertyName = in.readUTF();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(propertyName);
	}
}
