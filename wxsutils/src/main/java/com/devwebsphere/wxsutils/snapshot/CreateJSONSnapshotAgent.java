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
package com.devwebsphere.wxsutils.snapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;
import com.ibm.ws.objectgrid.index.MapKeyIndex;

public class CreateJSONSnapshotAgent implements ReduceGridAgent 
{
	static Logger logger = Logger.getLogger(CreateJSONSnapshotAgent.class.getName());
	
	public String rootFolder;
	public Filter entryFilter;

	/**
	 * 
	 */
	private static final long serialVersionUID = -792968127082040666L;

	public static String getFileName(String rootFolder, int pid, String mapName)
	{
		String fileName = rootFolder + File.separator + pid + "-" + mapName + EXTENSION;
		return fileName;
	}
	
	public static String MAGIC = "(:JSONSNAPSHOT:)";
	public static String EXTENSION = ".txt";
	
	public Object reduce(Session sess, ObjectMap map) 
	{
		try
		{
			int pid = sess.getObjectGrid().getMap(map.getName()).getPartitionId();
			// ****************
			// internal call, this isn't supported publicly
			// this will be replaced with a supported version
			MapKeyIndex mki = (MapKeyIndex) map.getIndex(com.ibm.ws.objectgrid.Constants.BUILTIN_MAP_KEY_INDEX_NAME);
			// ****************
			String fileName = CreateJSONSnapshotAgent.getFileName(rootFolder, pid, map.getName());
			logger.log(Level.INFO, "Creating snapshot in file " + fileName);
			File file = new File(fileName);
			file.delete();
			if(file.createNewFile() == false)
			{
				logger.log(Level.SEVERE, "Cannot create file " + fileName);
				throw new ObjectGridRuntimeException("Cannot create file " + fileName);
			}
			FileWriter fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			
			ObjectMapper mapper = new ObjectMapper();
			
			Iterator<Object> keyIter = mki.findAllKeys(sess.getTxID());
			boolean writtenKeyValueClassNames = false;
			pw.println(MAGIC);
			pw.println(Integer.toString(pid));
			pw.println(map.getName());
			
			while(keyIter.hasNext())
			{
				Object key = keyIter.next();
				Object value = map.get(key);
				if(writtenKeyValueClassNames == false)
				{
					writtenKeyValueClassNames = true;
					pw.println(key.getClass().getName());
					pw.println(value.getClass().getName());
				}
				if(entryFilter == null || (entryFilter != null && entryFilter.filter(value)))
				{
					String json = mapper.writeValueAsString(key);
					pw.println(json);
					json = mapper.writeValueAsString(value);
					pw.println(json);
				}
			}
			pw.close();
			fw.close();
			return new Integer(pid);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception creating snapshot file ", e);
			throw new ObjectGridRuntimeException(e);
		}
		// TODO Auto-generated method stub
	}

	public Object reduce(Session arg0, ObjectMap arg1, Collection arg2) 
	{
		logger.log(Level.SEVERE, "Unimplemented method, should never be called");
		throw new ObjectGridRuntimeException("Unimplemented method, should never be called");
	}

	public Object reduceResults(Collection arg0) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(Object c : arg0)
		{
			Integer i = (Integer)c;
			list.add(i);
		}
		return list;
	}

	/**
	 * This writes a remote disk based snapshot of a given map in a grid. The snapshot
	 * files are placed remotely in the specified rootFolder
	 * @param utils The client to the grid with the map
	 * @param mapName The map to dump
	 * @param rootFolder The name of a directory. It will be created if it is missing
	 * @throws ObjectGridRuntimeException
	 */
	public static void writeSnapshot(WXSUtils utils, String mapName, String rootFolder)
		throws ObjectGridRuntimeException
	{
		try
		{
			CreateJSONSnapshotAgent agent = new CreateJSONSnapshotAgent();
			agent.rootFolder = rootFolder;
	
			WXSMap<?, ?> map = utils.getCache(mapName);
	
			AgentManager am = utils.getSessionForThread().getMap(mapName).getAgentManager();
			Object rawRC = am.callReduceAgent(agent);
			List<Integer> pids = (List<Integer>)rawRC; 
			if(utils.getObjectGrid().getMap(mapName).getPartitionManager().getNumOfPartitions() != pids.size())
			{
				logger.log(Level.SEVERE, "Cannot write all partitions to snapshot");
				throw new ObjectGridRuntimeException("Problem writing all partitions to snapshot on remote side");
			}
			for(int i = 0; i < pids.size(); ++i)
			{
				if(!pids.contains(new Integer(i)))
				{
					String msg = "Partition #" + i + " was not written to snapshot";
					logger.log(Level.SEVERE, msg);
					throw new ObjectGridRuntimeException(msg);
				}
			}
		}
		catch(UndefinedMapException e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException("Map doesn't exist: " + mapName);
		}
	}
}
