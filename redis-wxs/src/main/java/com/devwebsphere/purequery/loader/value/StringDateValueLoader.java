package com.devwebsphere.purequery.loader.value;

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


import com.devwebsphere.purequery.loader.BaseKeyValue;
import com.devwebsphere.purequery.loader.PQValueLoader;
import com.ibm.websphere.objectgrid.plugins.LogElement;


public class StringDateValueLoader extends PQValueLoader {

	@Override
	public BaseKeyValue<String,Date> getPOJOProperty(LogElement e) 
	{
		return new TemplateStringKeyProperty<Date>(e);
	}

	@Override
	public Class getPOJOClass() 
	{
		return TemplateStringKeyProperty.class;
		
	}
}
