package com.devwebsphere.jdbc.loader;

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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.persistence.Column;

import com.devwebsphere.jdbc.loader.GenericJDBCLoader.ValueHolder;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.plugins.LogElement;

/**
 * This class is meant as a base class for loaders. It's main purpose is to provide
 * a common table name and the getData method.
 * @author bnewport
 *
 */
public abstract class BaseJDBCLoader <KEY>
{
	protected String mapName = LoaderMBeanManager.UNKNOWN_MAP;
	
	Class<KEY> keyClass;
	/**
	 * This holds the simple key column name if specified
	 */
	String keyAttributeColumn;

	/**
	 * This will create a JDBC Connection instance or return the current one associated with this
	 * WXS transaction.
	 * @param tx
	 * @return A JDBC Connection that can be used for the specified tx
	 */
	static public Connection getConnection(TxID tx)
		throws SQLException
	{
		/*
		 * These purequery Loaders MUST have a transaction callback specified of the type PQTxCallback.
		 */
		JDBCTxCallback cb = (JDBCTxCallback)tx.getSession().getObjectGrid().getTransactionCallback();
		return cb.getConnection(tx);
	}

	public void copyPojoListToBatch(Connection conn, String sql, ArrayList<LogElement> list, ArrayList<Field> fields, ArrayList<String> fieldNames, ArrayList<Field> keyFields, ArrayList<String> keyFieldNames)
		throws SQLException, IllegalAccessException, NoSuchFieldException
	{
		NamedParameterStatement s = new NamedParameterStatement(conn, sql);
		ValueHolder v = new ValueHolder();
		for(int row = 0; row < list.size(); ++row)
		{
			LogElement item = list.get(row);
			if(keyAttributeColumn != null)
			{
				v._wxsutil_value = item.getCacheEntry().getKey();
				copyPojoToStatement(s, v, keyFieldNames, keyFields);
			}
			else
				copyPojoToStatement(s, item.getCacheEntry().getKey(), keyFieldNames, keyFields);
			copyPojoToStatement(s, item.getCurrentValue(), fieldNames, fields);
			s.addBatch();
		}
		s.executeUpdate();
	}
	
	static final String VALUE_FIELD = "_wxsutil_value";
	
	String getSQLColumnName(Field f)
	{
		String name = null;
		if(f.getName().equals(VALUE_FIELD))
			return keyAttributeColumn;
		if(f.isAnnotationPresent(Column.class))
		{
			Column col = f.getAnnotation(Column.class);
			if(col.name().length() > 0)
				name = col.name();
		}
		else
			name = f.getName().toUpperCase();
		return name;
	}
	
	public <I> void copyPojoToStatement(NamedParameterStatement s, I item, ArrayList<String> fieldNames, ArrayList<Field> fields)
		throws IllegalAccessException, SQLException, NoSuchFieldException
	{
		for(int i = 0; i < fields.size(); ++i)
		{
			Field f_typical = fields.get(i);
			Class fieldType = f_typical.getType();
			if(f_typical.getName().equals(VALUE_FIELD))
				fieldType = keyClass;
			String sqlColumn = getSQLColumnName(f_typical);
			Field f = item.getClass().getField(f_typical.getName());
			Object fValue = f.get(item);
			if(fieldType.equals(String.class))
			{
				String sv = (String)fValue;
				s.setString(sqlColumn, sv);
			} else if(fieldType.equals(Integer.class))
			{
				Integer o = (Integer)fValue;
				s.setInt(sqlColumn, o.intValue());
			} else if(fieldType.getName().equals("int"))
			{
				int ii = f.getInt(item);
				s.setInt(sqlColumn, ii);
			} else if(fieldType.equals(Long.class))
			{
				Long l = (Long)fValue;
				s.setLong(sqlColumn, l.longValue());
			} else if(fieldType.getName().equals("long"))
			{
				long l = f.getLong(item);
				s.setLong(sqlColumn, l);
			} else if(fieldType.equals(Timestamp.class))
			{
				Timestamp t = (Timestamp)fValue;
				s.setTimestamp(sqlColumn, t);
			} else
			{
				Object t = fValue;
				s.setObject(sqlColumn, t);
			}
		}
	}
	
	public <T> T copyResultSetToPojo(ResultSet rs, Class<T> c, ArrayList<Field> allFields)
		throws InstantiationException, IllegalAccessException, SQLException
	{
		T value = c.newInstance();
		// jdbc columns are numbered from 1 so we +1 here
		for(int i = 0; i < allFields.size(); ++i)
		{
			Field f = allFields.get(i);
			Class fieldType = f.getType();
			if(f.getName().equals(VALUE_FIELD))
				fieldType = keyClass;
			String sqlColumn = getSQLColumnName(f);
			if(fieldType.equals(String.class))
			{
				f.set(value, rs.getString(sqlColumn));
			} else if(fieldType.equals(Integer.class))
			{
				f.set(value, new Integer(rs.getInt(sqlColumn)));
			} else if(fieldType.getName().equals("int"))
			{
				f.setInt(value, rs.getInt(sqlColumn));
			} else if(fieldType.equals(Long.class))
			{
				f.set(value, new Long(rs.getLong(sqlColumn)));
			} else if(fieldType.getName().equals("long"))
			{
				f.setLong(value, rs.getLong(sqlColumn));
			} else if(fieldType.equals(Timestamp.class))
			{
				f.set(value, rs.getTimestamp(sqlColumn));
			} else
			{
				f.set(value, rs.getObject(sqlColumn));
			}
		}
		return value;
	}

}
