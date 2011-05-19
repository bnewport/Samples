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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ConnectException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This agent runs on every container and then reads a local file that
 * contains the json encoded key/value pairs to read in to memory
 * It returns the partition id as an integer and the callReduceAgent
 * method should return a list of partition ids that have been processed
 * 
 * A PER_CONTAINER grid should be used to execute this agent once
 * PER container JVM. A file level lock based around the timestamp the
 * client invoked the agent is used to ensure only one container PER
 * box actually reads the local snapshot.
 * @author bnewport
 *
 */
public class ReadJSONSnapshotAgent implements ReduceGridAgent 
{
	static Logger logger = Logger.getLogger(ReadJSONSnapshotAgent.class.getName());
	
	public String rootFolder;
	public String gridName;
	public String mapName;
	
	private long lockTimestamp = System.currentTimeMillis();

	/**
	 * 
	 */
	private static final long serialVersionUID = -792968127082040666L;

	public static WXSMap<Serializable, Serializable> getRemoteMap(String gridName, String mapName)
	throws ConnectException
	{
		ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
		ObjectGrid clientOG = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
		WXSUtils utils = new WXSUtils(clientOG);
		WXSMap<Serializable,Serializable> remoteMap = utils.getCache(mapName);
		return remoteMap;
	}

	/**
	 * One JVM on a box attempts to create a specific lock file. Only one will succeed and
	 * it will be the one that reads the snapshot. Every separate attempt to read a snapshot
	 * uses a different lock file so they do not have to be cleared out.
	 * @return
	 * @throws IOException
	 */
	boolean isAlreadyInProgress()
		throws IOException
	{
		// this code make sures that only one JVM on a specific physical server box
		// will do the preload.
		String lockFileName = rootFolder + File.separator + Long.toString(lockTimestamp) + ".lock";
		
		File lockFile = new File(lockFileName);
		boolean alreadyInProgress = false;
		alreadyInProgress = lockFile.exists();
		if(!alreadyInProgress)
		{
			alreadyInProgress = !lockFile.createNewFile();
		}
		return alreadyInProgress;
	}
	
	public Object reduce(Session sess, ObjectMap map) 
	{
		try
		{
			if(isAlreadyInProgress())
				return new ArrayList<Integer>();
				
			File directory = new File(rootFolder);
			if(directory.exists() == false)
			{
				if(directory.mkdir() == false)
				{
					logger.log(Level.SEVERE, "Cannot create folder for snapshot at " + rootFolder);
					throw new ObjectGridRuntimeException("Cannot create root folder for snapshot " + rootFolder);
				}
			}
			if(directory.isDirectory() == false)
			{
				logger.log(Level.SEVERE, "Root must be a directory " + rootFolder);
				throw new ObjectGridRuntimeException("Root must be a directory " + rootFolder);
			}
			String[] files = directory.list();
			ArrayList<Integer> partitionsLoaded = new ArrayList<Integer>();
			for(String fileName : files)
			{
				if(!fileName.endsWith(CreateJSONSnapshotAgent.EXTENSION))
					continue;
				fileName = rootFolder + File.separator + fileName;
				File file = new File(fileName);
				if(!file.canRead())
					continue;
				FileInputStream fw = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(fw);
				BufferedReader rd = new BufferedReader(new InputStreamReader(dis));

				int pid = 0;
				String mapName = null;
				try
				{
					try
					{
						String magic = rd.readLine();
						// check if first line has magic code
						if(magic == null || magic.equals(CreateJSONSnapshotAgent.MAGIC) == false)
							continue;
						String partitionLine = rd.readLine();
						try
						{
							pid = Integer.parseInt(partitionLine);
						}
						catch(Exception e)
						{
							logger.log(Level.SEVERE, "Badly formatted snapshot file, no partition id after magic line");
							continue;
						}
						mapName = rd.readLine();
						if(mapName == null)
						{
							logger.log(Level.SEVERE, "Badly formatted snapshot file, mapname line not present");
							continue;
						}
					}
					catch(Exception e)
					{
						// not our kind of file, just skip it
						continue;
					}
					
					logger.log(Level.INFO, "Reading snapshot in file " + fileName);
					WXSMap<Serializable, Serializable> remoteMap = getRemoteMap(gridName, mapName);
					int partitionCount = remoteMap.getWXSUtils().getObjectGrid().getMap(mapName).getPartitionManager().getNumOfPartitions();
					ObjectMapper mapper = new ObjectMapper();
					
					boolean readKeyValueClassNames = false;
		
					Class<? extends Serializable> keyClass = null;
					Class<? extends Serializable> valueClass = null;
					String line = null;
					int entryCounter = 0;
					Map<Serializable, Serializable> batch = new HashMap<Serializable, Serializable>();
					while((line = rd.readLine()) != null)
					{
						if(readKeyValueClassNames == false)
						{
							keyClass = (Class<? extends Serializable>)Class.forName(line);
							valueClass = (Class<? extends Serializable>)Class.forName(rd.readLine());
							readKeyValueClassNames = true;
							logger.log(Level.FINE, "Key Class is " + keyClass.getName() + ", ValueClass is " + valueClass.getName());
							continue;
						}
						String keyJSON = line;
						String valueJSON = rd.readLine();
						Serializable key = mapper.readValue(keyJSON, keyClass);
						Serializable value = mapper.readValue(valueJSON, valueClass);
						batch.put(key, value);
						if(entryCounter++ > 500 * partitionCount) // we want a decent batch size per partition
						{
							entryCounter = 0;
							remoteMap.putAll_noLoader(batch);
							batch = new HashMap<Serializable, Serializable>();
						}
					}
					if(batch.size() > 0)
						remoteMap.putAll_noLoader(batch);
				}
				finally {
					rd.close();
					dis.close();
					fw.close();
				}
				partitionsLoaded.add(new Integer(pid));
			}
			return partitionsLoaded;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception creating snapshot file ", e);
			throw new ObjectGridRuntimeException(e);
		}
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
			if(c instanceof EntryErrorValue)
			{
				return c;
			}
			else
			{
				List<Integer> i = (List<Integer>)c;
				list.addAll(i);
			}
		}
		return list;
	}
	
	/**
	 * This method will load a snapshot from the specified folder on the grid side
	 * boxes.
	 * @param utils
	 * @param mapName
	 * @param rootFolder
	 * @param cep
	 * @throws ObjectGridRuntimeException If an issue occurs during loading the snapshot
	 */
	static public void readSnapshot(WXSUtils utils, String mapName, String rootFolder, String cep)
	{
		try
		{
			ReadJSONSnapshotAgent agent = new ReadJSONSnapshotAgent();
			agent.rootFolder = rootFolder;
			agent.gridName = utils.getObjectGrid().getName();
			agent.mapName = mapName;
	
			WXSMap<?, ?> map = utils.getCache(mapName);
	
			ObjectGrid perContainerClient = WXSUtils.connectClient(cep, "PerContainerGrid");
			
			AgentManager am = perContainerClient.getSession().getMap("M.MAIN").getAgentManager();
			Object rawRC = am.callReduceAgent(agent);
			if(rawRC instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "Remote exception reading snapshot: " + rawRC.toString());
				throw new ObjectGridRuntimeException(rawRC.toString());
			}
			List<Integer> pids = (List<Integer>)rawRC; 
			BackingMap bmap = utils.getObjectGrid().getMap(mapName);
			if(bmap.getPartitionManager().getNumOfPartitions() != pids.size())
			{
				logger.log(Level.SEVERE, "Partitions were not fully loaded from the snapshot");
				throw new ObjectGridRuntimeException("Partitions were not fully loaded from the snapshot");
			}
			for(int i = 0; i < pids.size(); ++i)
			{
				if(!pids.contains(new Integer(i)))
				{
					logger.log(Level.SEVERE, "Partition #" + i + " was not reloaded");
					throw new ObjectGridRuntimeException("Partition " + i + " wasn't read from snapshot");
				}
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception reading snapshot", e);
			throw new ObjectGridRuntimeException("Exception reading snapshot", e);
		}
	}
}
