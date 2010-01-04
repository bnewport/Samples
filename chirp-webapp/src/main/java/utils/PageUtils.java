package utils;
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



import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.devwebsphere.rediswxs.R;

public class PageUtils 
{
	static public int MAX_USERNAME_SIZE = 20;
	static public String VERSION = "V1.1";
	
	static public boolean isLoggedIn(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		if(session == null)
			return false;
		if(getUserID(request) == null || getUsername(request) == null)
			return false;
		return true;
	}

	static public void loadUserInfo(HttpServletRequest request, long userId)
	{
		request.getSession().setAttribute("userid", userId);
		String username = R.str_str.get("u:" + Long.toString(userId) + ":username");
		request.getSession().setAttribute("username", username);
	}
	
	static public String getUsername(HttpServletRequest request)
	{
		return (String)request.getSession().getAttribute("username");
	}
	
	static public void logout(HttpServletRequest request)
	{
		request.removeAttribute("userid");
		request.removeAttribute("username");
		request.getSession().invalidate();
	}
	
	static public Long getUserID(HttpServletRequest request)
	{
		return (Long)request.getSession().getAttribute("userid");
	}
	
	static public String strElapsed(long time)
	{
		long now = System.currentTimeMillis() / 1000;
		time /= 1000;
		long i = now - time;
		
		if(i < 60)
			return Long.toString(i) + " second" + ((i > 1) ? "s" : "");
		if(i < 3600)
		{
			long m = i / 60;
			return Long.toString(m) + " minute" + ((m > 1) ? "s" : "");
		}
		if(i < 3600*24)
		{
			long h = i / 3600;
			return Long.toString(h) + " hour" + ((h > 1) ? "s": "");
		}
		i = i / 3600/24;
		return Long.toString(i) + " day" + ((i > 1) ? "s":"");
	}

	static private String trimOrNull(String s)
	{
		String v = s.trim();
		return (v.length() > 0) ? v : null;
	}
	
	static public String getParam(HttpServletRequest request, String name)
	{
		String v = request.getParameter(name);
		if(v != null)
			return trimOrNull(v);
		v = (String)request.getAttribute(name);
		if(v != null)
			return trimOrNull(v);
		return null;
	}
	
	static public String getUserLink(String username)
	{
		String userlink = "<a class=\"username\" href=\"profile.jsp?u=" + URLEncoder.encode(username) + "\">" + escapeHtmlFull(username) + "</a> ";
		return userlink;
	}
	
	static public void showPost(StringBuilder buff, long id)
	{
		String postData = R.str_str.get("p:"+Long.toString(id));
		if(postData == null)
			return;
		StringTokenizer tok = new StringTokenizer(postData, "|");
		String uid = tok.nextToken();
		long time = Long.parseLong(tok.nextToken());
		String post = tok.nextToken();
		String username = R.str_str.get("u:"+uid + ":username");
		String elapsed = strElapsed(time);
		String userlink = getUserLink(username);
		buff.append("<div class=\"post\">");
		buff.append(userlink);
		buff.append(post);
		buff.append("<br>");
		buff.append("<i>posted "); buff.append(elapsed); buff.append(" ago via web</i></div>");
	}
	
	// Sourced from http://www.owasp.org/index.php/How_to_perform_HTML_entity_encoding_in_Java
	
	public static StringBuilder escapeHtmlFull(String s)
	 {
	     StringBuilder b = new StringBuilder(s.length());
	     for (int i = 0; i < s.length(); i++)
	     {
	       char ch = s.charAt(i);
	       if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9')
	       {
	         // safe
	         b.append(ch);
	       }
	       else if (Character.isWhitespace(ch))
	       {
	         // paranoid version: whitespaces are unsafe - escape
	         // conversion of (int)ch is naive
	         b.append("&#").append((int) ch).append(";");
	       }
	       else if (Character.isISOControl(ch))
	       {
	         // paranoid version:isISOControl which are not isWhitespace removed !
	         // do nothing do not include in output !
	       }
	       else if (Character.isHighSurrogate(ch))
	       {
	         int codePoint;
	         if (i + 1 < s.length() && Character.isSurrogatePair(ch, s.charAt(i + 1))
	           && Character.isDefined(codePoint = (Character.toCodePoint(ch, s.charAt(i + 1)))))
	         {
	            b.append("&#").append(codePoint).append(";");
	         }
	         else
	         {
	//           log("bug:isHighSurrogate");
	         }
	         i++; //in both ways move forward
	       }
	       else if(Character.isLowSurrogate(ch))
	       {
	         // wrong char[] sequence, //TODO: LOG !!!
//	         log("bug:isLowSurrogate");
	         i++; // move forward,do nothing do not include in output !
	       }
	       else
	       {
	         if (Character.isDefined(ch))
	         {
	           // paranoid version
	           // the rest is unsafe, including <127 control chars
	           b.append("&#").append((int) ch).append(";");
	         }
	         //do nothing do not include undefined in output!
	       }
	    }
	     return b;
	 }
	
	static public String goback(String msg)
	{
		StringBuilder buff = new StringBuilder();
		buff.append("<div id=\"error\">");
		buff.append(escapeHtmlFull(msg));
		buff.append("<br><a href=\"javascript:history.back()\">Please return back and try again</a></div>");
		return buff.toString();
	}
	
	static public boolean showUserPosts(StringBuilder buff, long userid, int start, int count)
	{
		// global timelime is -1
		String key = Long.toString(userid) + ":posts";
		ArrayList<Long> posts = R.str_long.lrange(key, start, start+count);
		int c = 0;
		for(Iterator<Long> iter = posts.iterator(); iter.hasNext() && c++ < count; )
		{
			Long p = iter.next();
			showPost(buff, p);
		}
		return posts.size() == count + 1;
	}
	
	static public void showPagesWithPagination(HttpServletRequest request, StringBuilder buff, String username, Long userid, int start, int count)
	{
		String thisPage = request.getRequestURI();
		String navlink = "";
		int next = start + 10;
		int prev = start - 10;
		String nextlink = null;
		String prevlink = "";
		if(prev < 0) prev = 0;
		String u = username != null ? "&u=" + URLEncoder.encode(username) : "";
		if(showUserPosts(buff, userid, start, count))
		{
			nextlink = "<a href=\"" + thisPage + "?start=" + Integer.toString(next) + u + "\"/>Older posts &raquo;</a>";
		}
		if(start > 0)
		{
			prevlink = "<a href=\"" + thisPage + "?start=" + Integer.toString(prev) + u + "\"/>Newer posts &raquo;</a>" + ((nextlink != null) ? " | " : "");
		}
		if(nextlink != null || prevlink != null)
		{
			buff.append("<div class=\"rightlink\">");
			if(prevlink != null) buff.append(prevlink);
			if(nextlink != null) buff.append(nextlink);
			buff.append("</div>");
			
		}
	}
	
	static public void showLastUsers(StringBuilder buff, int count)
	{
		List<Long> lastUsers = R.str_long.lrange("last50users", 0, 50);
		
		for(Long userId : lastUsers)
		{
			String userName = R.str_str.get("u:"+Long.toString(userId)+":username");
			String userlink = null;    
			if(userName != null)     
				userlink = getUserLink(userName);    
			else     
				userlink = "*" + userId + "*";    
			buff.append(userlink);
			buff.append(" ");
		}
	}
	
	static public long isReply(String status)
	{
		if(status.startsWith("@"))
		{
			StringTokenizer tok = new StringTokenizer(status.substring(1), " ,.:;");
			String username = tok.nextToken();
			if(username.length() <= MAX_USERNAME_SIZE)
			{
				Long userid = R.str_long.get("un:"+username+":id");
				if(userid != null)
					return userid.longValue();
			}
		}
		return -1;
	}

	/**
	 * This generates a hexadecimal representation of a MD5 hash of a string.
	 * @param password
	 * @return
	 */
	static public String hashPassword(String password)
	{
		try
		{
			MessageDigest mdAlgorithm = MessageDigest.getInstance("MD5");
			mdAlgorithm.update(password.getBytes());
	
			byte[] digest = mdAlgorithm.digest();
			StringBuilder hexString = new StringBuilder();
	
			for (int i = 0; i < digest.length; i++) {
			    String s = Integer.toHexString(0xFF & digest[i]);
	
			    if (s.length() < 2) {
			    	hexString.append("0");
			    }
	
			    hexString.append(s);
			}
			return hexString.toString();
		}
		catch(NoSuchAlgorithmException e)
		{
			return null;
		}
	}
}
