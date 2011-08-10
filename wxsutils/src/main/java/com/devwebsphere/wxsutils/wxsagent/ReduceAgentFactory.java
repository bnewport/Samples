package com.devwebsphere.wxsutils.wxsagent;

import java.util.List;
import java.util.Map;

import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public interface ReduceAgentFactory<A extends ReduceGridAgent> {
	public <K> A newAgent(List<K> keys);

	public <K, V> A newAgent(Map<K, V> map);

	public <K> K getKey(A a);

	public <X> X emptyResult();
}