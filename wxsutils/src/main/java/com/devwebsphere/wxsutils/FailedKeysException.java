package com.devwebsphere.wxsutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class FailedKeysException extends Exception {
	private static final long serialVersionUID = -6906400645084195636L;
	private Set keys;
	private List<EntryErrorValue> entryErrorValues = new LinkedList<EntryErrorValue>();

	public <K> FailedKeysException(EntryErrorValue eev, Collection<K> keys) {
		this.keys = new HashSet<K>(keys);
		entryErrorValues.add(eev);
	}

	public <K> Set<K> getKeys() {
		return (Set<K>) keys;
	}

	public List<EntryErrorValue> getErrorEntryValues() {
		return entryErrorValues;
	}

	public void combine(FailedKeysException fe) {
		entryErrorValues.addAll(fe.entryErrorValues);
		keys.addAll(fe.keys);
	}

	@Override
	public String toString() {
		StringBuilder msg = new StringBuilder();
		msg.append(entryErrorValues.toString());
		return msg.toString();
	}
}
