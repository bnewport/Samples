package com.devwebsphere.wxsutils.wxsagent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public interface ReduceAgentFactory<A extends ReduceGridAgent> {
	public <K extends Serializable> A newAgent(List<K> keys);

	public <K extends Serializable, V> A newAgent(Map<K, V> map);

	public <K extends Serializable> K getKey(A a);

	public <X> X emptyResult();
}