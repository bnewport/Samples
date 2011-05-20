package com.devwebsphere.wxsutils.wxsmap.dirtyset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent;
import com.devwebsphere.wxsutils.wxsmap.SetElement;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * This will check a single partition for all keys for the single set called
 * dirtyKey within this partition. It iterates over every bucket for
 * that set and skips empty buckets
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public class FetchDirtyJobsInPartitionTask <K extends Serializable, V extends Serializable> implements SinglePartTask<PartitionResult<V>, ArrayList<DirtyKey<V>>>
{
	K setKey;
	String setMapName;
	String leaseMapName;
	int nextBucket;
	int desiredMaxKeys = Integer.MAX_VALUE;
	
	long leasePeriodMS;
	
	static Logger logger = Logger.getLogger(FetchDirtyJobsInPartitionTask.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1722977140374061823L;
	
	public FetchDirtyJobsInPartitionTask() {}

	/**
	 * This can be called to check if a partition result is
	 * empty and not interesting for clients
	 */
	public boolean isResultEmpty(ArrayList<DirtyKey<V>> result) 
	{
		return result.isEmpty();
	}

	/**
	 * This will start at nextBucket and scan it and subsequent buckets for non empty set.
	 * The result is a PartitionResult that indicates the last bucket checked
	 * and the keys found if any.
	 */
	public PartitionResult<V> process(Session sess) 
	{
		try
		{
			ObjectMap setMap = sess.getMap(setMapName);
			ObjectMap leaseMap = sess.getMap(leaseMapName);
			// serialize access to set
			setMap.getForUpdate(setKey);
			TreeSet<DirtyKey<V>> set = new TreeSet<DirtyKey<V>>();
			int i = nextBucket;
			for(i = nextBucket; i < SetAddRemoveAgent.NUM_BUCKETS; ++i)
			{
				LinkedHashSet<SetElement<V>> bucketSet = (LinkedHashSet<SetElement<V>>)setMap.get(SetAddRemoveAgent.getBucketKeyForBucket(setKey, i));
				if(bucketSet != null)
				{
					for(SetElement<V> se : bucketSet)
					{
						Long leaseTime = (Long)leaseMap.getForUpdate(se.getValue());
						// if lease has not expired then skip to next element
						if(leaseTime != null)
						{
							if(leaseTime.longValue() > System.currentTimeMillis())
								continue;
						}
						DirtyKey<V> dk = new DirtyKey<V>();
						dk.setValue(se.getValue());
						dk.setTimeStamp(se.getTimeStamp());
						set.add(dk);
						
						// update lease time
						if(leasePeriodMS != 0)
						{
							Long now = new Long(System.currentTimeMillis() + leasePeriodMS);
							if(leaseTime == null)
								leaseMap.insert(se.getValue(), now);
							else
								leaseMap.update(se.getValue(), now);
						}
					}
					// keep adding buckets until max size exceeded
					// if preserveOrder is true then only return a bucket at a time
					if(set.size() >= desiredMaxKeys)
						break;
				}
			}
			PartitionResult<V> rc = new PartitionResult<V>();
			if(set != null)
			{
				rc.result = new ArrayList<DirtyKey<V>>(set.size());
				rc.result.addAll(set);
			}
			else
				rc.result = new ArrayList<DirtyKey<V>>();
			// remember the current bucket so we start at the next one
			// during the next call to this method
			rc.nextBucket = i;
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

}