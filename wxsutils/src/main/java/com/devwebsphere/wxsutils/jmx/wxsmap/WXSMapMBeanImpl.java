package com.devwebsphere.wxsutils.jmx.wxsmap;

public class WXSMapMBeanImpl implements WXSMapMBean 
{
	String mapName;
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.WXSMapMBean#getMapName()
	 */
	public final String getMapName() {
		return mapName;
	}

	public WXSMapMBeanImpl(String mapName)
	{
		this.mapName = mapName;
	}
}
