package com.devwebsphere;
/**
 * A simple example of an in-memory search using Lucene.
 */
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import com.devwebsphere.wxslucene.ClientGridDirectory;
import com.devwebsphere.wxslucene.GridDirectory;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.plugins.TransactionCallbackException;

public class InMemoryExample 
{


    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws TransactionCallbackException, ObjectGridException, IOException, URISyntaxException {
        // Construct a RAMDirectory to hold the in-memory representation
        // of the index.
        
    	String indexFileName = "/Users/ibm/Downloads/index_hs0_2";
//		GridDirectory gidx = new GridDirectory(indexFileName);
        ClientGridDirectory gidx = new ClientGridDirectory(indexFileName);
        NIOFSDirectory didx = new NIOFSDirectory(new File(indexFileName));
        
        Directory idx = gidx;
        Executor exec = WXSUtils.getDefaultUtils().getExecutorService();

        for(int i = 0; i < 0; ++i)
        {
        	SearcherThread t = new SearcherThread(idx);
        	exec.execute(t);
        }
    	

        try {
//            // Make an writer to create the index
//            IndexWriter writer =
//                new IndexWriter(idx, new StandardAnalyzer(), true, new IndexWriter.MaxFieldLength(5000));
//
//            // Add some Document objects containing quotes
//            writer.addDocument(createDocument("Theodore Roosevelt",
//                "It behooves every man to remember that the work of the " +
//                "critic, is of altogether secondary importance, and that, " +
//                "in the end, progress is accomplished by the man who does " +
//                "things."));
//            writer.addDocument(createDocument("Friedrich Hayek",
//                "The case for individual freedom rests largely on the " +
//                "recognition of the inevitable and universal ignorance " +
//                "of all of us concerning a great many of the factors on " +
//                "which the achievements of our ends and welfare depend."));
//            writer.addDocument(createDocument("Ayn Rand",
//                "There is nothing to take a man's freedom away from " +
//                "him, save other men. To be free, a man must be free " +
//                "of his brothers."));
//            writer.addDocument(createDocument("Mohandas Gandhi",
//                "Freedom is not worth having if it does not connote " +
//                "freedom to err."));
//
//            // Optimize and close the writer to finish building the index
//            writer.optimize();
//            writer.close();

            // Build an IndexSearcher using the in-memory index
            Searcher searcher = new IndexSearcher(idx);

            // Run some queries
//            search(searcher, "britney");
//            search(searcher, "free");
//            search(searcher, "progress or achievements");
            MinMaxAvgMetric qArtist = new MinMaxAvgMetric();
            MinMaxAvgMetric qCategory = new MinMaxAvgMetric();
            int maxRuns = 1200;
            String [] artists = {"U2", "MADONNA", "GAGA", "POLICE", "WHAM", "BEATLES", "STING"};
            for(int i = 0; i < maxRuns; ++i)
            {
	            TopDocs q1 = search(searcher, "ARTIST", artists[i % artists.length], qArtist);
	            TopDocs q2 = search(searcher, "CATEGORY_LIST", "19", qCategory);
	            if(i % 100 == 0 || (i + 1 == maxRuns))
	            {
	            	System.out.println("qArtist: " + qArtist.toString());
	            	System.out.println("qCat: " + qCategory.toString());
	            	qArtist.reset();
	            	qCategory.reset();
	            	if(gidx.getLRUBlockCache() != null)
	            		System.out.println("Hit rate:" + gidx.getLRUBlockCache().getHitRate());
//	            	dumpTopDocs(searcher, q1, "ARTIST", "U2");
	            }
            }

            searcher.close();
        }
        catch(IOException ioe) {
            // In this example we aren't really doing an I/O, so this
            // exception should never actually be thrown.
            ioe.printStackTrace();
        }
        catch(ParseException pe) {
            pe.printStackTrace();
        }
    }

    /**
     * Make a Document object with an un-indexed title field and an
     * indexed content field.
     */
    private static Document createDocument(String title, String content) {
        Document doc = new Document();

        // Add the title as an unindexed field...
      
        doc.add(new Field("title", title, Field.Store.YES, Field.Index.NO));

        // ...and the content as an indexed field. Note that indexed
        // Text fields are constructed using a Reader. Lucene can read
        // and index very large chunks of text, without storing the
        // entire content verbatim in the index. In this example we
        // can just wrap the content string in a StringReader.               
        doc.add(new Field("content", new StringReader(content)));

        return doc;
    }

    /**
     * Searches for the given string in the "content" field
     */
    static TopDocs search(Searcher searcher, String fieldName, String value, MinMaxAvgMetric metric)
        throws ParseException, IOException {

    	Query query = new QueryParser(fieldName, new StandardAnalyzer()).parse(value);
//    	QueryParser qp = new QueryParser("content", new StandardAnalyzer());
        // Build a Query object
//        Query query = qp.parse(queryString);
        
        // Search for the query
    	long now = System.nanoTime();
        TopDocs td = searcher.search(query, 100);
        long duration = System.nanoTime() - now;
        metric.logTime(duration);

        // Examine the Hits object to see if there were any matches
        int hitCount = td.totalHits;
        if (hitCount == 0) {
            System.out.println(
                "No matches were found for \"" + fieldName + "=" + value + "\"");
        }
        return td;
//        System.out.println();
    }

    static void dumpTopDocs(Searcher searcher, TopDocs td, String fieldName, String value)
    	throws IOException
    {
        System.out.println(td.totalHits + " hits for \"" +
        		fieldName + "=" + value + "\" were found in quotes by:");

        // Iterate over the Documents in the Hits object
        for (ScoreDoc hit : td.scoreDocs) {
            Document doc = searcher.doc(hit.doc);
            List<Fieldable> fields = doc.getFields();
            for(Fieldable f : fields)
            {
                System.out.println("  " + f.name() + "=" + doc.get(f.name()));
            }
            // Print the value that we stored in the "title" field. Note
            // that this Field was not indexed, but (unlike the
            // "contents" field) was stored verbatim and can be
            // retrieved.
        }
    }
}