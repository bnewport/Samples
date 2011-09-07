//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxssearch;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.devwebsphere.wxssearch.type.ExactIndex;
import com.devwebsphere.wxssearch.type.PrefixIndex;
import com.devwebsphere.wxssearch.type.SubstringIndex;
import com.devwebsphere.wxsutils.ByteArrayKey;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This is a client class for managing full search indexes. The application should make one Index for each attribute
 * that needs to be searchable.
 * 
 */
public class IndexManager<A, RK extends Serializable> {
	static String DYN_ATTRIBUTES_MAP_SUFFIX = "Attributes";

	static Logger logger = Logger.getLogger(IndexManager.class.getName());

	WXSUtils utils;
	Class<A> indexClass;
	ConcurrentHashMap<String, Index<A, RK>> indices = new ConcurrentHashMap<String, Index<A, RK>>();
	List<Field> indexedFields;

	String internalKeyToRealKeyMapName;

	/**
	 * Create a WXSUtils and provide this. This is used for all bulk operations. The indexClass should be the Map value
	 * being indexed and should have annotations to indicate which fields are being indexed.
	 * 
	 * @param utils
	 *            The client connection wrapped with utils
	 * @param indexClass
	 *            The value class to 'index'
	 * @See ExactIndex
	 * @See SubstringIndex
	 * @See PrefixIndex
	 */
	public IndexManager(WXSUtils utils, Class<A> indexClass) {
		this.utils = utils;
		this.indexClass = indexClass;
		// hash to RK map is shared between indexes for atttributes on
		// the same class
		internalKeyToRealKeyMapName = getIndexClass().getName() + "_" + DYN_ATTRIBUTES_MAP_SUFFIX;
		initIndexes(indexClass);
	}

	void initIndexes(Class<A> indexClass) {
		Field[] fields = indexClass.getDeclaredFields();
		indexedFields = new ArrayList<Field>();

		for (Field f : fields) {
			Index<A, RK> index = null;
			SubstringIndex s = f.getAnnotation(SubstringIndex.class);
			ExactIndex e = f.getAnnotation(ExactIndex.class);
			PrefixIndex p = f.getAnnotation(PrefixIndex.class);
			String indexName = indexClass.getSimpleName() + "_" + f.getName();
			if (s != null) {
				index = new SubstringIndexImpl<A, RK>(this, indexName, s);
			}
			if (e != null) {
				index = new ExactIndexImpl<A, RK>(this, indexName, e);
			}
			if (p != null) {
				index = new PrefixIndexImpl<A, RK>(this, indexName, p);
			}
			if (index != null) {
				indexedFields.add(f);
				f.setAccessible(true);
				indices.put(f.getName(), index);
			}
		}
	}

	/**
	 * This returns and initializes the index for a specific attribute. The index name should be a combination of the
	 * object name and attribute name. The object map being indexed should have keys of type RK.
	 * 
	 * @param indexName
	 *            The name of this index.
	 * @return An index object
	 */
	public Index<A, RK> getIndex(String indexName) {
		Index<A, RK> rc = (Index<A, RK>) indices.get(indexName);
		if (rc == null)
			throw new ObjectGridRuntimeException("Unknown index:" + indexName);
		return rc;
	}

	/**
	 * This method searches across multiple indexes and combines the answers. Create the business object and only
	 * initialize the attributes you want to actually use in the query. You can AND or OR all these attributes.
	 * 
	 * @param criteria
	 *            A business object with only attributes in the query set. Others must be null
	 * @param isAND
	 *            TRUE if results are to be ANDed otherwise the results are ORed
	 * @return The list of keys for the matching entries
	 * @See TestSubstringIndex
	 */
	public SearchResult<RK> searchMultipleIndexes(A criteria, boolean isAND) {
		try {
			Set<ByteArrayKey> result = null;
			// first run the individual queries for each attribute in parallel
			Collection<Future<SearchResult<byte[]>>> threads = new ArrayList<Future<SearchResult<byte[]>>>();
			for (Field f : indexedFields) {
				final String value = (String) f.get(criteria); // non null field means it's in the query
				if (value != null) {
					final Index<A, RK> index = getIndex(f.getName());

					Callable<SearchResult<byte[]>> c = new Callable<SearchResult<byte[]>>() {
						public SearchResult<byte[]> call() {
							SearchResult<byte[]> r = index.rawContains(value);
							return r;
						}
					};

					Future<SearchResult<byte[]>> future = utils.getExecutorService().submit(c);
					threads.add(future);
				}
			}

			// the collect the results and AND/OR them
			for (Future<SearchResult<byte[]>> future : threads) {
				SearchResult<byte[]> r = future.get();
				if (!r.isTooManyMatches()) {
					Collection<ByteArrayKey> rr = convertToKeys(r.getResults());
					if (result == null) {
						result = new HashSet<ByteArrayKey>(rr);
					} else if (isAND) {
						// AND this index match with the current running results
						result.retainAll(rr);
					} else {
						// OR
						result.addAll(rr);
					}
				}

			}
			if (result != null) {
				WXSMap<ByteArrayKey, RK> ik2rkMap = utils.getCache(internalKeyToRealKeyMapName);
				Map<ByteArrayKey, RK> rc = ik2rkMap.getAll(result);
				return new SearchResult<RK>(new ArrayList<RK>(rc.values()));
			} else {
				return new SearchResult<RK>();
			}
		} catch (Exception e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	public static List<ByteArrayKey> convertToKeys(Collection<byte[]> keys) {
		if (keys == null) {
			return Collections.emptyList();
		}

		ArrayList<ByteArrayKey> k = new ArrayList<ByteArrayKey>(keys.size());
		for (byte[] b : keys)
			k.add(new ByteArrayKey(b));
		return k;
	}

	/**
	 * This takes a Map of keys and business object entries and indexes all indexes attributes in one method call.
	 * 
	 * @param entries
	 */
	public void indexAll(Map<RK, A> entries) {
		if (entries.size() > 0) {
			try {
				Map<String, Map<RK, String>> fentries = new HashMap<String, Map<RK, String>>(indexedFields.size());
				for (Field f : indexedFields) {
					Map<RK, String> m = new HashMap<RK, String>();
					fentries.put(f.getName(), m);
				}

				for (Map.Entry<RK, A> e : entries.entrySet()) {
					A bo = e.getValue();
					RK key = e.getKey();

					for (Field f : indexedFields) {
						String value = (String) f.get(bo);
						Map<RK, String> m = fentries.get(f.getName());
						m.put(key, value);
					}
				}

				for (Field f : indexedFields) {
					Map<RK, String> m = fentries.get(f.getName());
					Index<A, RK> index = getIndex(f.getName());
					index.insert(m);
				}
			} catch (IllegalAccessException e) {
				throw new ObjectGridRuntimeException(e);
			}
		}
	}

	/**
	 * This just returns the digest we will use to generate hashes. SHA and MD5 are very good hash functions. MD5 is
	 * about 40% faster than SHA so we'll use MD5
	 * 
	 * @return
	 */
	public static MessageDigest getDigest() {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return digest;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public final Class<A> getIndexClass() {
		return indexClass;
	}

	public final String getInternalKeyToRealKeyMapName() {
		return internalKeyToRealKeyMapName;
	}
}
