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
package com.devwebsphere.jsoncrud;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Main;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This is a Java script shell client for connecting to a WXS grid. The shell has
 * a grid variable defined that allows the user to connect to the
 * grid specified on the command line.
 * @author bnewport
 *
 */
public class WXSShell {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options opts = new Options();
		CmdLineParser parser = new CmdLineParser(opts);
		try
		{
			parser.parseArgument(args);
			ObjectGrid ogclient = WXSUtils.connectClient(opts.ch + ":" + opts.cp, opts.gridName, null);

			Context ctxt = Context.enter();
			ErrorReporter reporter = new ErrorReporter() {
				
				public void warning(String arg0, String arg1, int arg2, String arg3,
						int arg4) 
				{
					System.out.println("WARNING");
				}
				
				public EvaluatorException runtimeError(String arg0, String arg1, int arg2,
						String arg3, int arg4) {
					System.out.println("RUNTIMEERROR");
					// TODO Auto-generated method stub
					return null;
				}
				
				public void error(String msg, String file, int linenum, String arg3, int arg4) {
					System.out.println("ERROR:" + msg);
				}
			};
			ctxt.setErrorReporter(reporter);
			
			Scriptable scope = ctxt.initStandardObjects();
			
			// define a "grid" variable to allow access to maps
			JSGrid gridShim = new JSGrid(ogclient, ctxt, scope);
			Object wrappedGridShim = Context.javaToJS(gridShim, scope);
			
			Main.global.init(Main.shellContextFactory);
			Main.global.defineProperty("grid", wrappedGridShim, ScriptableObject.CONST);

			while(true)
			{
				// run the shell command line loop
				Main.processSource(ctxt, null);
			}
		}
		catch(CmdLineException e)
		{
			parser.printSingleLineUsage(System.out);
		}
		catch(Exception e)
		{
			System.out.println("Exception " + e.toString());
		}
	}

}
