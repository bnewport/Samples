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
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.filter.ValuePath;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This fetches the named attributed from a POJO assuming
 * there is a public field on the class of that name. If the field
 * doesn't exist then null is returned
 * @author bnewport
 *
 */
public class PojoFieldPath implements ValuePath, Externalizable 
{
	static Logger logger = Logger.getLogger(PojoFieldPath.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6785462152045367579L;
	String propertyName;
	
	public PojoFieldPath()
	{
		
	}
	
	public PojoFieldPath(String propertyName)
	{
		try
		{
			this.propertyName = propertyName;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	public Object get(Object fo) {
		try
		{
			Field f = fo.getClass().getField(propertyName);
			return f.get(fo);
		}
		catch(NoSuchFieldException e)
		{
			return null;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	public String toString()
	{
		return "." + propertyName;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException 
	{
		propertyName = in.readUTF();
	}

	public void writeExternal(ObjectOutput out) throws IOException 
	{
		out.writeUTF(propertyName);
	}
}
