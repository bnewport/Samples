package com.devwebsphere.wxsutils;

import java.io.Serializable;

import com.ibm.websphere.objectgrid.plugins.PartitionableKey;

public class PartitionKey<V extends Serializable & Comparable<V>> implements Serializable, PartitionableKey, Comparable<PartitionKey<V>> {

	private static final long serialVersionUID = 7760190389911061642L;
	private Serializable partitionKey;
	private V actualKey;

	public PartitionKey(Serializable partitionKey, V actualKey) {
		this.partitionKey = partitionKey;
		this.actualKey = actualKey;
	}

	public Object ibmGetPartition() {
		return partitionKey;
	}

	public V getKey() {
		return actualKey;
	}

	@Override
	public int hashCode() {
		return actualKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		if (actualKey == null) {
			return false;
		}
		PartitionKey<?> other = (PartitionKey<?>) obj;
		return actualKey.equals(other.actualKey);
	}

	public int compareTo(PartitionKey<V> o) {
		return actualKey.compareTo(o.actualKey);
	}
	
	@Override
	public String toString() {
		return actualKey.toString();
	}

}
