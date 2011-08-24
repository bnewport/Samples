package com.devwebsphere.wxsutils.wxsagent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public interface ReduceAgentFactory<A extends ReduceGridAgent, K extends Serializable, V, X> {
	public A newAgent(List<K> keys);

	public A newAgent(Map<K, V> map);

	public K getKey(A a);

	public X emptyResult();
}