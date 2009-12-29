package com.devwebsphere.jdbc.loader.value;

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

import java.sql.Date;

import com.devwebsphere.jdbc.loader.BaseKeyValue;
import com.devwebsphere.jdbc.loader.JDBCValueLoader;
import com.ibm.websphere.objectgrid.plugins.LogElement;


public class StringDateValueLoader extends JDBCValueLoader<String,Date> {
	static public class KV extends TemplateStringKeyProperty<Date>
	{
		public Date value;
		public KV() {};
		public KV(LogElement elem)
		{
			super(elem);
			value = (Date)elem.getCurrentValue();
		}
		@Override
		public Date baseGetValue() 
		{
			return value;
		}
		public String toString()
		{
			return "" + keyz + ":" + value;
		}
	}

	@Override
	public BaseKeyValue<String,Date> getPOJOProperty(LogElement e) 
	{
		return new KV(e);
	}

	@Override
	public Class getPOJOClass() 
	{
		return KV.class;
		
	}
}
