package com.devwebsphere.wxsutils.jmx.listset;

public interface WXSMapOfListsMBean {
	public String getMapName();
	
	public Integer getLenExceptionCount();
	public String getLenLastExceptionString();
	public Double getLenTimeAvgMS();
	public Double getLenTimeMaxMS();
	public Double getLenTimeMinMS();
	public Double getLenTotalTimeMS();
	
	public Integer getPopExceptionCount();
	public String getPopLastExceptionString();
	public Double getPopTimeAvgMS();
	public Double getPopTimeMaxMS();
	public Double getPopTimeMinMS();
	public Double getPopTotalTimeMS();

	public Integer getPushExceptionCount();
	public String getPushLastExceptionString();
	public Double getPushTimeAvgMS();
	public Double getPushTimeMaxMS();
	public Double getPushTimeMinMS();
	public Double getPushTotalTimeMS();

	public Integer getRangeExceptionCount();
	public String getRangeLastExceptionString();
	public Double getRangeTimeAvgMS();
	public Double getRangeTimeMaxMS();
	public Double getRangeTimeMinMS();
	public Double getRangeTotalTimeMS();

	public Integer getTrimExceptionCount();
	public String getTrimLastExceptionString();
	public Double getTrimTimeAvgMS();
	public Double getTrimTimeMaxMS();
	public Double getTrimTimeMinMS();
	public Double getTrimTotalTimeMS();

	public Integer getRemoveExceptionCount();
	public String getRemoveLastExceptionString();
	public Double getRemoveTimeAvgMS();
	public Double getRemoveTimeMaxMS();
	public Double getRemoveTimeMinMS();
	public Double getRemoveTotalTimeMS();
}
