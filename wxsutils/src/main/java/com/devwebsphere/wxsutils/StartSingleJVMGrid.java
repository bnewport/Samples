package com.devwebsphere.wxsutils;

public class StartSingleJVMGrid {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		int i = 0;
		String gridName = args[i++];
		String objectgridxml = args[i++];
		String deploymentxml = args[i++];
		
		System.out.println("Grid: " + gridName);
		System.out.println("ObjectGrid xml: " + objectgridxml);
		System.out.println("Deployment xml:" + deploymentxml);
		WXSUtils.startTestServer(gridName, objectgridxml, deploymentxml);
	}

}
