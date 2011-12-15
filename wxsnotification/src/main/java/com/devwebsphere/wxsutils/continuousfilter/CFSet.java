package com.devwebsphere.wxsutils.continuousfilter;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.devwebsphere.wxsutils.continuousfilter.CFMessage.Operation;

public class CFSet<E> extends AbstractSet<E> implements CFCallback {

	static interface Listener {
		void keysAdded(Object[] keys);

		void keysRemoved(Object[] keys);
	}

	ConcurrentMap<Integer, Set<E>> partitionedSet = new ConcurrentHashMap<Integer, Set<E>>();
	Listener listener = null;

	public int size() {
		return 0;
	}

	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public void received(CFMessage m) {
		Integer pid = m.partitionId;
		E[] keys = (E[]) m.keys;
		Set<E> aSet;
		switch (m.operation) {
		case ADD:
			if (keys != null && keys.length > 0) {
				aSet = getPartitionSet(pid, true);
				for (E k : keys) {
					aSet.add(k);
				}
				notifyAdded(keys);
			}
			break;
		case REMOVE:
			if (keys != null && keys.length > 0) {
				aSet = getPartitionSet(pid, false);
				for (E k : keys) {
					aSet.remove(k);
				}
				notifyRemoved(keys);
			}
			break;
		case CLEAR:
			aSet = partitionedSet.remove(pid);
			if (aSet != null && aSet.size() > 0) {
				keys = (E[]) aSet.toArray(new Object[aSet.size()]);
				notifyRemoved(keys);
			}
			break;
		case SET:
			m.operation = Operation.CLEAR;
			received(m);
			m.operation = Operation.ADD;
			received(m);
		}
	}

	private Set<E> getPartitionSet(Integer pid, boolean create) {
		Set<E> aSet = partitionedSet.get(pid);
		if (aSet == null && create) {
			aSet = new ConcurrentSkipListSet<E>();
			partitionedSet.put(pid, aSet);
		}
		return aSet;
	}

	private void notifyAdded(E[] keys) {
		try {
			if (listener != null) {
				listener.keysAdded(keys);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void notifyRemoved(E[] keys) {
		try {
			if (listener != null) {
				listener.keysRemoved(keys);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}
}
