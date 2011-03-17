package com.devwebsphere.wxsutils.jmx.listset;

public class WXSMapOfSetsMBeanImpl implements WXSMapOfSetsMBean {
	String gridName;
	String mapName;
	
	public WXSMapOfSetsMBeanImpl(String g, String m)
	{
		gridName = g;
		mapName = m;
	}
}
