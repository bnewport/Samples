package com.devwebsphere.wxsutils;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;

public abstract class WXSBaseMap 
{
	BackingMap bmap;
	ThreadLocalSession tls;
	WXSUtils utils;
	String mapName;
	ObjectGrid grid;

	protected WXSBaseMap(WXSUtils utils, String mapName)
	{
		this.mapName = mapName;
		this.utils = utils;
		grid = utils.getObjectGrid();
		bmap = utils.grid.getMap(mapName);
		tls = new ThreadLocalSession(utils);
	}
}
