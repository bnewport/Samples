//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.filter.set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.devwebsphere.wxsutils.wxsmap.SessionPool;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.CopyMode;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.OutputFormat;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.index.MapIndex;
import com.ibm.websphere.objectgrid.plugins.index.MapIndexPlugin;
import com.ibm.websphere.objectgrid.plugins.index.MapRangeIndex;
import com.ibm.websphere.objectgrid.plugins.io.MapSerializerPlugin;
import com.ibm.websphere.objectgrid.plugins.io.SerializerAccessor;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContext;
import com.ibm.websphere.objectgrid.plugins.io.dataobject.SerializedEntry;

/**
 * This executes a filtered single index search across all partitions, one partition at a time serially. The filter is
 * applied against the V, not the key. It uses the MultiJob framework. The getNextResult method should be called to get
 * all valid results. Keep calling getNextResult until it returns null. There is usually one call per partition.
 * 
 * @author bnewport
 * 
 * @param <K>
 * @param <V>
 */
public class GridFilteredIndex<K extends Serializable, V extends Serializable> implements
		MultipartTask<HashMap<Serializable, Serializable>, HashMap<K, V>> {
	static Logger logger = Logger.getLogger(GridFilteredIndex.class.getName());

	public enum Operation {
		eq, lt, lte, gt, gte, btwn
	};

	/**
	 * This is called to convert from the network form to the client form. Its the same in this case.
	 */
	public HashMap<K, V> extractResult(HashMap<Serializable, Serializable> rawRC) {
		BackingMap bmap = je.getObjectGrid().getMap(mapName);
		SerializerAccessor sa = bmap.getSerializerAccessor();

		MapSerializerPlugin msp = sa.getMapSerializerPlugin();
		boolean convertKeys = msp != null && msp.getKeySerializerPlugin() != null;
		boolean convertVals = filter.requiresDataObjectContext();
		if (convertKeys || convertVals) {
			DataObjectContext ctx = sa.getDefaultContext();
			HashMap<K, V> result = new HashMap<K, V>(rawRC.size());
			for (Map.Entry<Serializable, Serializable> e : rawRC.entrySet()) {
				K key = getObject(ctx, e.getKey(), convertKeys);
				V val = getObject(ctx, e.getValue(), convertVals);
				result.put(key, val);
			}
			return result;
		} else {
			return (HashMap<K, V>) rawRC;
		}
	}

	private <T> T getObject(DataObjectContext ctx, Object o, boolean convert) {
		if (convert) {
			SerializedEntry se = (SerializedEntry) o;
			se.applyContext(ctx);
			return se.getObject();
		} else {
			return (T) o;
		}
	}

	/**
	 * This is called grid size, once per partition to use the index to generate a subset and then filter the result.
	 * Finally, any filtered K,V pairs are returned.
	 * 
	 * @author bnewport
	 * 
	 * @param <K>
	 * @param <V>
	 */
	static public class GridFilteredIndexSingleTask<K extends Serializable, V extends Serializable> implements
			SinglePartTask<HashMap<Serializable, Serializable>, HashMap<K, V>> {
		static Logger logger = Logger.getLogger(GridFilteredIndexSingleTask.class.getName());
		/**
		 * 
		 */
		private static final long serialVersionUID = 1722977140374061823L;
		String mapName;
		String indexName;
		Filter filter;
		Operation opCode;
		Serializable value1, value2;

		public GridFilteredIndexSingleTask() {
		}

		/**
		 * This constructs a grid side object to search an individual partition
		 * 
		 * @param mapName
		 *            The map to search
		 * @param indexName
		 *            The name of the index on the map
		 * @param filter
		 *            The filter to apply to the index results
		 * @param op
		 *            The index operation
		 * @param v1
		 *            The index value for relational operations or low value for between
		 * @param v2
		 *            The high value for between operations
		 */
		public GridFilteredIndexSingleTask(String mapName, String indexName, Filter filter, Operation op, Serializable v1, Serializable v2) {
			this.mapName = mapName;
			this.indexName = indexName;
			this.filter = filter;
			this.opCode = op;
			this.value1 = v1;
			this.value2 = v2;
		}

		/**
		 * This can be called to check if a partition result is empty and not interesting for clients
		 */
		public boolean isResultEmpty(HashMap<K, V> result) {
			return result.isEmpty();
		}

		/**
		 * This is called to search the index for all entries matching the index criteria and then filter those results
		 * with the filter.
		 */
		public HashMap<Serializable, Serializable> process(Session sess) {
			try {
				// wrap session with a WXSUtils
				WXSUtils utils = new WXSUtils(sess.getObjectGrid());
				sess = utils.getSessionForThread();
				sess.setTransactionIsolation(Session.TRANSACTION_READ_COMMITTED);
				WXSMap<Serializable, Serializable> wMap = utils.getCache(mapName);
				ObjectMap map = sess.getMap(mapName);

				// set the key output -- if we have a key serializer
				SerializerAccessor sa = utils.getObjectGrid().getMap(mapName).getSerializerAccessor();
				if (sa != null && sa.getMapSerializerPlugin().getKeySerializerPlugin() != null) {
					map.setKeyOutputFormat(OutputFormat.RAW);
				}

				// set the value output
				if (filter.requiresDataObjectContext()) {
					map.setCopyMode(CopyMode.COPY_TO_BYTES_RAW, null);
				}
				MapIndex index = (MapIndex) map.getIndex(indexName);
				MapRangeIndex rindex = null;
				// could be a range index (supports relational ops)
				if (index instanceof MapRangeIndex) {
					rindex = (MapRangeIndex) index;
				}

				sess.beginNoWriteThrough();

				Map<Serializable, Serializable> rc = null;
				if (opCode == Operation.eq) {
					FilteredIndex<Serializable, Serializable> fi = new FilteredIndex<Serializable, Serializable>(wMap, index);
					rc = (indexName.equals(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME)) ? fi.filterAll(filter) : fi.eq(value1, filter);
				} else {
					FilteredRangeIndex<Serializable, Serializable> fir = new FilteredRangeIndex<Serializable, Serializable>(wMap, rindex);
					switch (opCode) {
					case lt:
						rc = fir.lt(value1, filter);
						break;
					case lte:
						rc = fir.lte(value1, filter);
						break;
					case gt:
						rc = fir.gt(value1, filter);
						break;
					case gte:
						rc = fir.gte(value1, filter);
						break;
					case btwn:
						rc = fir.btwn(value1, value2, filter);
						break;
					}
				}

				if (rc instanceof HashMap) {
					return (HashMap<Serializable, Serializable>) rc;
				}
				return new HashMap<Serializable, Serializable>(rc);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			} finally {
				try {
					if (sess.isTransactionActive()) {
						// always rollback since we are only doing reads
						sess.rollback();
					}
				} catch (ObjectGridException e) {
					logger.log(Level.SEVERE, "Exception", e);
					throw new ObjectGridRuntimeException(e);
				}
				SessionPool.returnSession(sess);
			}
		}

		private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
			ois.defaultReadObject();
			if (indexName.equals(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME)) {
				indexName = MapIndexPlugin.SYSTEM_KEY_INDEX_NAME;
			}
		}

	}

	String mapName;
	String indexName;
	Filter filter;
	Serializable value1, value2;
	Operation op;
	JobExecutor<HashMap<Serializable, Serializable>, HashMap<K, V>> je;

	/**
	 * This allows a relational operation against the index and then a filter. Operations allowed are eq/lt/lte/gt/gte.
	 * 
	 * @param ogclient
	 * @param mapName
	 * @param indexName
	 * @param filter
	 * @param op
	 * @param v
	 */
	public GridFilteredIndex(ObjectGrid ogclient, String mapName, String indexName, Filter filter, Operation op, Serializable v) {
		this.mapName = mapName;
		this.filter = filter;
		this.indexName = indexName;
		this.op = op;
		if (op == Operation.btwn)
			throw new ObjectGridRuntimeException("Between needs two values");
		this.value1 = v;
		je = new JobExecutor<HashMap<Serializable, Serializable>, HashMap<K, V>>(ogclient, this);
	}

	/**
	 * This allows a between index operation followed by a filter
	 * 
	 * @param ogclient
	 * @param mapName
	 * @param indexName
	 * @param filter
	 * @param v1
	 * @param v2
	 */
	public GridFilteredIndex(ObjectGrid ogclient, String mapName, String indexName, Filter filter, Serializable v1, Serializable v2) {
		this.mapName = mapName;
		this.indexName = indexName;
		this.filter = filter;
		this.op = Operation.btwn;
		this.value1 = v1;
		this.value2 = v2;
		je = new JobExecutor<HashMap<Serializable, Serializable>, HashMap<K, V>>(ogclient, this);
	}

	public SinglePartTask<HashMap<Serializable, Serializable>, HashMap<K, V>> createTaskForPartition(
			SinglePartTask<HashMap<Serializable, Serializable>, HashMap<K, V>> previousTask) {
		// prevtask is null when called for first time for a partition
		if (previousTask == null) {
			GridFilteredIndexSingleTask<K, V> t = new GridFilteredIndexSingleTask<K, V>(mapName, indexName, filter, op, value1, value2);
			return t;
		} else {
			// only need one run per partition so return null
			// second time it's called for a partition
			return null;
		}
	}

	/**
	 * This is just a delegate to the JobExecutor. This can return Maps of zero size. Only a null return indicates the
	 * end of the operation.
	 * 
	 * @return
	 */
	public Map<K, V> getNextResult() {
		return je.getNextResult();
	}

	public JobExecutor<HashMap<Serializable, Serializable>, HashMap<K, V>> getJE() {
		return je;
	}
}
