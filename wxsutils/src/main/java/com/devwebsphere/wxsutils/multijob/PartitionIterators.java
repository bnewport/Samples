//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2012
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.multijob;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;

public class PartitionIterators {
	static class Itr implements PartitionIterator {
		int start;
		int end;
		int countBy;
		int current;

		Itr(int start, int end, int countBy) {
			this.start = start;
			this.end = end;
			this.countBy = countBy;
			reset();
		}

		@Override
		public boolean hasNext() {
			if (countBy > 0) {
				return current <= end;
			}
			return current >= end;
		}

		@Override
		public int next() {
			int val = current;
			current += countBy;
			return val;
		}

		@Override
		public void reset() {
			current = start;
		}
	}

	public static PartitionIterator ascending(ObjectGrid og) {
		return ascending(og, 0);
	}

	public static PartitionIterator ascending(ObjectGrid og, int start) {
		return new Itr(start, getPartitionSize(og) - 1, 1);
	}

	public static PartitionIterator descending(ObjectGrid og) {
		return descending(og, 0);
	}

	public static PartitionIterator descending(ObjectGrid og, int end) {
		return new Itr(getPartitionSize(og) - 1, end, -1);
	}

	static int getPartitionSize(ObjectGrid og) {
		String aMapName = (String) og.getListOfMapNames().get(0);
		BackingMap bmap = og.getMap(aMapName);
		return bmap.getPartitionManager().getNumOfPartitions();
	}
}
