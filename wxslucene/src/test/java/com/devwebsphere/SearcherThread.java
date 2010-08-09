package com.devwebsphere;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;

public class SearcherThread implements Runnable
{
	static Logger logger = Logger.getLogger(SearcherThread.class.getName());
	Directory idx;
	
	public SearcherThread(Directory d)
	{
		idx = d;
	}
	
	public void run()
	{
		try
		{
	        // Build an IndexSearcher using the in-memory index
	        Searcher searcher = new IndexSearcher(idx);
	
	        // Run some queries
	//        search(searcher, "britney");
	//        search(searcher, "free");
	//        search(searcher, "progress or achievements");
	        MinMaxAvgMetric qArtist = new MinMaxAvgMetric();
	        MinMaxAvgMetric qCategory = new MinMaxAvgMetric();
	        for(int i = 0; i < 1000000; ++i)
	        {
	            TopDocs q1 = InMemoryExample.search(searcher, "ARTIST", "JOURNEY", qArtist);
	            TopDocs q2 = InMemoryExample.search(searcher, "CATEGORY_LIST", "19", qCategory);
	            if(i % 100 == 0)
	            {
	            	System.out.println("T:qArtist: " + qArtist.toString());
	            	System.out.println("T:qCat: " + qCategory.toString());
	            	qArtist.reset();
	            	qCategory.reset();
//	            	InMemoryExample.dumpTopDocs(searcher, q1, "ARTIST", "JOURNEY");
	            }
//	            Thread.sleep(10);
	        }
	
	        searcher.close();
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Thread threw exception", e);
		}
	}
		

}
