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
package com.devwebsphere.wxsutils.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a helper class to track a set of classes and serialize/deserialize
 * them in a more efficient form than with write object.
 * @author bnewport
 *
 */
public class ClassSerializer
{
	static Logger logger = Logger.getLogger(ClassSerializer.class.getName());

	byte nextCode = 1;
	
	/**
	 * Goes from Class to byte
	 */
	Map<Class<? extends Externalizable>, Byte> class2IdMap = new HashMap<Class<? extends Externalizable>, Byte>();
	/**
	 * Goes from byte to Class
	 */
	Map<Byte, Class<? extends Externalizable>> id2ClassMap = new HashMap<Byte, Class<? extends Externalizable>>();

	/**
	 * Add another class to this serializer. Any classes added will be serialized in a more efficient
	 * format.
	 * @param list The list of classes to add
	 */
	public void storeClass(Class<? extends Externalizable>... list)
	{
		for(Class<? extends Externalizable> c : list)
		{
			class2IdMap.put(c, nextCode);
			id2ClassMap.put(nextCode, c);
			nextCode++;
		}
	}

	/**
	 * This writes the non-null object to the stream
	 * @param out
	 * @param f
	 * @throws IOException
	 */
	public void writeObject(ObjectOutput out, Object f)
	throws IOException
	{
		Byte id = class2IdMap.get(f.getClass());
		if(id != null)
		{
			out.writeByte(id);
			((Externalizable)f).writeExternal(out);
		}
		else
		{
			out.writeByte(0);
			out.writeObject(f);
		}
	}
	
	/**
	 * This reads an object from the stream.
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object readObject(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		try
		{
			byte b = in.readByte();
			if(b == 0)
			{
				return in.readObject();
			}
			else
			{
				Class<? extends Externalizable> c = id2ClassMap.get(new Byte(b));
				Externalizable f = c.newInstance();
				f.readExternal(in);
				return f;
			}
		}
		catch(InstantiationException e)
		{
			logger.log(Level.SEVERE, "Unexpected exception inflating Filter", e);
			throw new IOException(e);
		}
		catch(IllegalAccessException e)
		{
			logger.log(Level.SEVERE, "Unexpected exception inflating Filter", e);
			throw new IOException(e);
		}
	}
}
