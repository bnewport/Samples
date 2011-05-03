package com.devwebsphere.wxsutils.wxsmap;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;

public abstract class WXSBaseMap 
{
	public BackingMap bmap;
	public ThreadLocalSession tls;
	public WXSUtils utils;
	public String mapName;
	public ObjectGrid grid;

	protected WXSBaseMap(WXSUtils utils, String mapName)
	{
		this.mapName = mapName;
		this.utils = utils;
		grid = utils.getObjectGrid();
		bmap = utils.getObjectGrid().getMap(mapName);
		tls = new ThreadLocalSession(utils);
	}
	
	public WXSUtils getWXSUtils()
	{
		return utils;
	}
}
