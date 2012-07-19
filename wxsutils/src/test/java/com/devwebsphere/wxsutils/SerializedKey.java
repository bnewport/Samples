package com.devwebsphere.wxsutils;

import java.io.Serializable;
import java.util.Comparator;

public class SerializedKey implements Serializable {
	static private class SKComparator implements Comparator<SerializedKey>, Serializable {
		private static final long serialVersionUID = 3194832609058799553L;

		@Override
		public int compare(SerializedKey o1, SerializedKey o2) {
			return o1.id.hashCode() - o2.id.hashCode();
		}

	}

	static public Comparator<SerializedKey> COMPARATOR = new SKComparator();

	private static final long serialVersionUID = -7450448630461653501L;
	String id;

	public SerializedKey(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj.getClass() != SerializedKey.class) {
			return false;
		}
		return id.equals(((SerializedKey) obj).id);
	}
}
