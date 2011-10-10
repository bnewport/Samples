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
package com.devwebsphere.wxs.asyncserviceimpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxs.asyncservice.Job;
import com.devwebsphere.wxs.asyncservice.KeyOperator;
import com.devwebsphere.wxs.asyncservice.KeyOperatorResult;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.Session;

public class ChainedTransactionJob<K extends Serializable, D extends KeyOperator<K>> implements Job<Boolean> {
	static Logger logger = Logger.getLogger(ChainedTransactionJob.class.getName());

	static class Result<K extends Serializable> implements KeyOperatorResult<K>, Serializable {
		private static final long serialVersionUID = 2852167934398065802L;
		KeyOperator<K> keyOperator;
		boolean applied;
		boolean unapplied;
		String errorString;

		public Result(KeyOperator<K> oper) {
			keyOperator = oper;
		}

		public boolean isApplied() {
			return applied;
		}

		public boolean isUnapplied() {
			return unapplied;
		}

		public String getErrorString() {
			return errorString;
		}

		public KeyOperator<K> getKeyOperator() {
			return keyOperator;
		}
	}

	private static final long serialVersionUID = 3079959679138948391L;
	ArrayList<K> keys;
	ArrayList<D> operators;
	int nextStep;
	RoutableKey originalUUID;
	boolean isCompensating;
	String mapName;
	K badApplyKey;

	Map<K, KeyOperatorResult<K>> results;

	public ChainedTransactionJob() {
	}

	public ChainedTransactionJob(String mapName, Map<K, D> operators) {
		this.mapName = mapName;
		originalUUID = null;
		keys = new ArrayList<K>(operators.size());
		this.operators = new ArrayList<D>(operators.size());

		for (Map.Entry<K, D> e : operators.entrySet()) {
			keys.add(e.getKey());
			this.operators.add(e.getValue());
		}

		nextStep = -1;
		isCompensating = false;
		badApplyKey = null;

		results = new HashMap<K, KeyOperatorResult<K>>(keys.size());
	}

	public Boolean process(Session localSession, RoutableKey msgId, ObjectGrid clientGrid) {
		System.out.println("Processing step " + msgId.getUUID());
		try {
			WXSUtils clientUtils = new WXSUtils(clientGrid);
			AsyncServiceManagerImpl clientAsyncMgr = new AsyncServiceManagerImpl(clientUtils);

			if (nextStep == -1) {
				originalUUID = msgId;
				nextStep = 0;
				K key = keys.get(0);
				StringBuilder builder = new StringBuilder(originalUUID.getUUID());
				builder.append(":");
				builder.append(key.toString());
				String nextID = builder.toString();
				clientAsyncMgr.sendAsyncRoutedJob(mapName, keys.get(0), this, nextID);
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "Routing job " + originalUUID + " to first partition");
				}
			} else {
				K key = keys.get(nextStep);
				D oper = operators.get(nextStep);
				if (!isCompensating) {
					boolean goodApply = false;
					Result<K> result = new Result<K>(oper);
					results.put(key, result);
					try {
						goodApply = oper.apply(localSession, key);
						result.applied = goodApply;
					} catch (Throwable e) {
						logger.log(Level.WARNING, "Job " + originalUUID + " Key " + keys.get(nextStep) + " apply has failed with exception", e);
						goodApply = false;
						result.errorString = e.toString();
					}

					if (!goodApply) {
						logger.log(Level.FINE, "Job " + originalUUID + " Key " + keys.get(nextStep) + " apply returned false");
						isCompensating = true;
						badApplyKey = key;
					}
				} else {
					Result<K> result = (Result<K>) results.get(key);
					try {
						oper.unapply(localSession, key);
						result.unapplied = true;
					} catch (Throwable e) {
						logger.log(Level.WARNING,
								"Job " + originalUUID + " Key " + keys.get(nextStep) + " unapply has failed with exception" + e.toString());
						result.errorString = e.toString();
					}
				}

				boolean isFinished = false;
				if (isCompensating) {
					nextStep--;
					isFinished = (nextStep < 0);
				} else {
					nextStep++;
					isFinished = (nextStep == keys.size());
				}

				if (!isFinished) {
					// do next step
					K nextKey = keys.get(nextStep);
					StringBuilder b = new StringBuilder();
					b.append(originalUUID.getUUID());
					b.append(nextKey.toString());
					if (isCompensating)
						b.append(":C");
					clientAsyncMgr.sendAsyncRoutedJob(mapName, keys.get(nextStep), this, b.toString());
				} else {
					// store result
					clientUtils.getCache(MapNames.CHAINED_RESULTS).put(originalUUID.getKey(), this);
				}
			}
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE,
					"Job " + originalUUID + " Key " + keys.get(nextStep) + " unapply has failed with a serious exception" + e.toString());
		}
		return Boolean.TRUE;
	}

	public Map<K, KeyOperatorResult<K>> getResult() {
		return results;
	}
}
