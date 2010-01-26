package com.devwebsphere.wxssearch.jmx;


public interface TextIndexMBean {

	public void reset();

	public String getIndexName();

	public Integer getInsertExceptionCount();

	public String getInsertLastExceptionString();

	public Double getInsertTimeAvgMS();

	public Double getInsertTimeMaxMS();

	public Double getInsertTimeMinMS();

	public Double getInsertTotalTimeMS();

	public Integer getContainsExceptionCount();

	public String getContainsLastExceptionString();

	public Double getContainsTimeAvgMS();

	public Double getContainsTimeMaxMS();

	public Double getContainsTimeMinMS();

	public Double getContainsTotalTimeMS();

	public Integer getRemoveExceptionCount();

	public String getRemoveLastExceptionString();

	public Double getRemoveTimeAvgMS();

	public Double getRemoveTimeMaxMS();

	public Double getRemoveTimeMinMS();

	public Double getRemoveTotalTimeMS();

	public Long getQueryTimeMS();

}