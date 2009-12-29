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
package com.devwebsphere.rediswxs.agent.set;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;

import com.devwebsphere.jdbc.loader.BaseJDBCLoader;
import com.devwebsphere.jdbc.loader.GenericJDBCLoader;
import com.devwebsphere.jdbc.loader.NamedParameterStatement;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.set.SetHead;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;

/**
 * This extends the GenericJDBCLoader and adds two helper methods
 * used by the set server code
 * 
 * @see SetHead#fetchSetItems(Session)
 * @see SRemove#process(Session, com.ibm.websphere.objectgrid.ObjectMap, Object)
 *
 */
public class SetItemJDBCLoader extends GenericJDBCLoader implements
		SetLoaderOperations 
{
	ArrayList<Field> allFields;
	ArrayList<Field> keyFields2;
	ArrayList<Field> keyFields;
	
	public SetItemJDBCLoader()
	{
		try
		{
			keyFields = new ArrayList<Field>();
			keyFields.add(ListItem.class.getField("keyz"));
			
			keyFields2 = new ArrayList<Field>(keyFields);
			keyFields2.add(ListItem.class.getField("value"));
			
			allFields = new ArrayList<Field>(keyFields2);
			allFields.add(ListItem.class.getField("pos"));
		}
		catch(NoSuchFieldException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ListItem getSetRecord(Session sess, String key, Long value)
	{
		try
		{
			// first the pos value for this set element
			Connection conn = BaseJDBCLoader.getConnection(sess.getTxID());
			String sql = "SELECT * FROM " + getTableName() + " WHERE KEYZ=:keyz AND VALUE=:value";
			ListItem item = new ListItem(key, 0L, value);
			
			NamedParameterStatement s = new NamedParameterStatement(conn, sql);
			copyPojoToStatement(s, item, keyFields2);
			ResultSet rs = s.executeQuery();
			if(rs.first())
			{
				item = copyResultSetToPojo(rs, ListItem.class, allFields);
			}
			return item;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	public HashSet<Serializable> getAllMembers(Session sess, String key) {
		try
		{
			Connection conn = BaseJDBCLoader.getConnection(sess.getTxID());
			String sql = "SELECT * FROM " + getTableName() + " WHERE KEYZ=:keyz";
			NamedParameterStatement s = new NamedParameterStatement(conn, sql);
			ListItem itemKey = new ListItem();
			itemKey.keyz = key;
			copyPojoToStatement(s, itemKey, keyFields);
			ResultSet rs = s.executeQuery();

			HashSet<Serializable> value = new HashSet<Serializable>();
			while(rs.next())
			{
				ListItem anItem = copyResultSetToPojo(rs, ListItem.class, allFields);
				value.add(anItem.value);
			}
			return value;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}
