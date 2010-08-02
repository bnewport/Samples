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
package com.devwebsphere.wxsutils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;

/**
 * This is a helper class to lazy create a reference to an MBeanManager when
 * the getLazyRef method is called.
 *
 * @param <T>
 */
public class LazyMBeanManagerAtomicReference<T> extends AtomicReference<T>
{
	static Logger logger = Logger.getLogger(LazyMBeanManagerAtomicReference.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = -8929211766280309799L;
	Class<T> clazz;
	
	/**
	 * The class of the instance to create lazily is provided here. It must have
	 * a default constructor.
	 * @param c
	 */
	public LazyMBeanManagerAtomicReference(Class<T> c)
	{
		clazz = c;
	}

	/**
	 * This is called to fetch the instance. It is created the first time this
	 * method is called. It's possible for two instances to be created but
	 * the JMX MBean Server will reject one when it is registered and throws
	 * an InstanceAlreadyExists exception in this case.
	 * @return
	 */
	public T getLazyRef()
	{
		while(get() == null)
		{
			try
			{
				T m = clazz.newInstance();
				compareAndSet(null, m);
			}
			catch(Exception e)
			{
				// this exception is expected and ok
				if(!(e instanceof InstanceAlreadyExistsException))
				{
					logger.log(Level.SEVERE, "Unexpected exception creating MBeanManager", e);
					throw new RuntimeException(e);
				}
			}
		}
		T rc = get();
		if(rc == null)
		{
			logger.log(Level.SEVERE, "MBeanManager doesn't exist");
		}
		return get();
	}
}
