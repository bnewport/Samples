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

import java.util.NoSuchElementException;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;

public class PartitionIterators {
	static class Itr implements PartitionIterator {
		int start;
		int end;
		int countBy;
		int current;

		Itr(int start, int end, int countBy) {
			if (countBy == 0) {
				throw new IllegalArgumentException("countBy == 0");
			}

			if (start < end && countBy < 0) {
				throw new IllegalArgumentException("Decrementing with start < end");
			}

			if (start > end && countBy > 0) {
				throw new IllegalArgumentException("Incrementing with start > end");
			}

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
			if (!hasNext())
				throw new NoSuchElementException();
			
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
		int size = getPartitionSize(og);
		check("start", start, size);
		return new Itr(start, size - 1, 1);
	}

	public static PartitionIterator descending(ObjectGrid og) {
		return descending(og, 0);
	}

	public static PartitionIterator descending(ObjectGrid og, int end) {
		int size = getPartitionSize(og);
		check("end", end, size);
		return new Itr(size - 1, end, -1);
	}

	public static PartitionIterator range(ObjectGrid og, int start, int end) {
		int size = getPartitionSize(og);
		check("start", start, size);
		check("end", end, size);
		int countBy = (start < end) ? 1 : -1;
		return new Itr(start, end, countBy);
	}

	public static int getPartitionSize(ObjectGrid og) {
		String aMapName = (String) og.getListOfMapNames().get(0);
		BackingMap bmap = og.getMap(aMapName);
		return bmap.getPartitionManager().getNumOfPartitions();
	}

	static void check(String idxMsg, int val, int partitionSize) {
		if (val < 0 || val >= partitionSize) {
			throw new IllegalArgumentException(val + " (" + idxMsg + ") is not between [0.." + partitionSize + ")");
		}
	}
}
