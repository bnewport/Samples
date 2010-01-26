package com.devwebsphere.wxssearch.jmx;

import com.devwebsphere.wxsutils.jmx.MBeanGroupManager;

public class TextIndexMBeanManager extends MBeanGroupManager<TextIndexMBeanImpl> 
{
	public TextIndexMBeanManager(String gridName) {
		super(TextIndexMBeanImpl.class, TextIndexMBean.class, gridName, "TextIndex", "IndexName");
	}

	@Override
	public TextIndexMBeanImpl createMBean(String gridName, String indexName) 
	{
		return new TextIndexMBeanImpl(indexName);
	}
}
