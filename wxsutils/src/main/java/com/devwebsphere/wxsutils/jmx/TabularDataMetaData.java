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
package com.devwebsphere.wxsutils.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * This is used to track attribute meta data for a specific TabularData type. It also
 * is used to fetch only those attributes as a CompositeDataType for each row.
 *
 * @param <T> The MBeanImpl being managed
 */
public class TabularDataMetaData<T>
{
	CompositeType jmxType;
	
	/**
	 * All tables have one column as a key column. This names the attribute in this role.
	 */
	String idColumnName;
	
	static HashMap<Class, SimpleType> typeConversion;
	static {
		typeConversion = new HashMap<Class, SimpleType>();
		typeConversion.put(Integer.class, SimpleType.INTEGER);
		typeConversion.put(Long.class, SimpleType.LONG);
		typeConversion.put(Double.class, SimpleType.DOUBLE);
		typeConversion.put(String.class, SimpleType.STRING);
	}
	
	ArrayList<TabularDataColumnMetaData> attributes;
	
	/**
	 * This is used to fetch all MBeans and extract the necessary attributes for this table
	 */
	MBeanGroupManager<T> beanSource;

	/**
	 * This returns the attribute name from a get Method definition.
	 * @param m The getter or is method
	 * @return The attribute name
	 */
	static public String getAttributeFromMethod(Method m)
	{
		String mName = m.getName();
		String name = null;
		if(mName.startsWith("is")) name = mName.substring("is".length());
		if(mName.startsWith("get")) name = mName.substring("get".length());
		if(name != null)
		{
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		else
			throw new IllegalArgumentException("Method " + m.getName() + " isn't an is or get");
		return name;
	}
	
	/**
	 * Constructs the class to manage rows from a collection of MBeans. The collection is abstracted
	 * as bs, the key column to use is idColumn Name. If the name parameter is the default mbean
	 * name "mbean" then all attributes are included. If a different name is specified then only the attributes
	 * annotated with that name using TabularAttribute are included.
	 * @param bs The collection of MBeans to summarise as a table
	 * @param idColumnName The attribute name of the key column for the table
	 * @param source The Class of the MBeanImpl
	 * @param name Only attributes annotated using TabularAttribute with this name are included
	 * @param typeName The JMX type name to use for the composite
	 * @param typeDescription 
	 * @throws OpenDataException
	 */
	public TabularDataMetaData(MBeanGroupManager<T> bs, String idColumnName, Class<T> source, String name, String typeName, String typeDescription)
		throws OpenDataException
	{
		beanSource = bs;
		this.idColumnName = idColumnName;
		attributes = new ArrayList<TabularDataColumnMetaData>();
		Method[] allMethods = source.getDeclaredMethods();
		
		for(Method m : allMethods)
		{
			TabularKey keyAttr = m.getAnnotation(TabularKey.class);
			if(keyAttr != null)
			{
//				this.idColumnName = getAttributeFromMethod(m);
			}
			TabularAttribute attr = m.getAnnotation(TabularAttribute.class);
			if(attr != null)
			{
				if(attr.mbean().equals(name) || name.equals(TabularAttribute.defaultMBean))
				{
					String description = m.getName();
					if(!attr.description().equals(""))
						description = attr.description();
					SimpleType atype = typeConversion.get(m.getReturnType());
					if(atype == null)
						throw new IllegalArgumentException("Unmapped return type " + m.getReturnType().getName());
					attributes.add(new TabularDataColumnMetaData(description, atype, m));
				}
			}
		}
		jmxType = getCompositeType(typeName, typeDescription);
	}
	
	public CompositeType getCompositeType(String itemType, String itemDescription)
		throws OpenDataException
	{
		return new CompositeType(itemType, itemDescription, getItemNames(), getItemDescriptions(), getItemTypes());
	}

	/**
	 * Returns a TabularData containing only the specified attributes from the Collection of MBeans.
	 * @param typeName The JMX type for the table being returned.
	 * @param description
	 * @return
	 * @throws OpenDataException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public TabularData getData(String typeName, String description)
		throws OpenDataException, InvocationTargetException, IllegalAccessException
	{
		String[] indexNames = {idColumnName};
		TabularType ttype = new TabularType(typeName, description, jmxType, indexNames);
		TabularDataSupport table = new TabularDataSupport(ttype);
		
		List<String> names = beanSource.getCurrentBeanNames();

		String[] colNames = getItemNames();
		Object[] params = new Object[0];
		
		for(String mapName : names)
		{
			Object[] values = new Object[attributes.size() + 1];
			values[0] = mapName;
			T mbean = beanSource.getBean(mapName);
			int i = 1;
			for(TabularDataColumnMetaData a : attributes)
			{
				Object r = a.method.invoke(mbean, params);
				values[i++] = a.convertType(r);
			}
			CompositeData cdata = new CompositeDataSupport(jmxType, colNames, values);
			table.put(cdata);
		}
		return table;
	}
	
	AtomicReference<String[]> itemNames = new AtomicReference<String[]>();
	
	String[] getItemNames()
	{
		if(itemNames.get() == null)
		{
			String[] values = new String[attributes.size() + 1];
			int i = 1;
			values[0] = idColumnName;
			for(TabularDataColumnMetaData a : attributes) values[i++] = a.name;
			itemNames.compareAndSet(null, values);
		}
		return itemNames.get();
	}
	
	AtomicReference<String[]> itemDescriptions = new AtomicReference<String[]>();
	
	String[] getItemDescriptions()
	{
		if(itemDescriptions.get() == null)
		{
			String[] values = new String[attributes.size() + 1];
			int i = 1;
			values[0] = "Key";
			for(TabularDataColumnMetaData a : attributes) values[i++] = a.description;
			itemDescriptions.compareAndSet(null, values);
		}
		return itemDescriptions.get();
	}

	AtomicReference<SimpleType[]> itemTypes = new AtomicReference<SimpleType[]>();
	
	SimpleType[] getItemTypes()
	{
		if(itemTypes.get() == null)
		{
			SimpleType[] values = new SimpleType[attributes.size() + 1];
			int i = 1;
			values[0] = SimpleType.STRING;
			for(TabularDataColumnMetaData a : attributes) values[i++] = a.type;
			itemTypes.compareAndSet(null, values);
		}
		return itemTypes.get();
	}
}