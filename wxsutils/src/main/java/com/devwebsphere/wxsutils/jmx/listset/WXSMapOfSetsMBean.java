package com.devwebsphere.wxsutils.jmx.listset;

public interface WXSMapOfSetsMBean {
	public String getMapName();
	public String getGridName();
	
	public Integer getAddRemoveExceptionCount();
	public String getAddRemoveLastExceptionString();
	public Double getAddRemoveTimeAvgMS();
	public Double getAddRemoveTimeMaxMS();
	public Double getAddRemoveTimeMinMS();
	public Double getAddRemoveTotalTimeMS();

	public Integer getSizeExceptionCount();
	public String getSizeLastExceptionString();
	public Double getSizeTimeAvgMS();
	public Double getSizeTimeMaxMS();
	public Double getSizeTimeMinMS();
	public Double getSizeTotalTimeMS();

	public Integer getContainsExceptionCount();
	public String getContainsLastExceptionString();
	public Double getContainsTimeAvgMS();
	public Double getContainsTimeMaxMS();
	public Double getContainsTimeMinMS();
	public Double getContainsTotalTimeMS();
}
