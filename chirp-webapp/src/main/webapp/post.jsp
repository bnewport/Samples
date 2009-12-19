
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="utils.PageUtils"%>
<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashSet"%>
<%
    	String status = PageUtils.getParam(request, "status");
    	if(!PageUtils.isLoggedIn(request) || status == null)
    	{
    		%> <jsp:forward page="index.jsp"/> <% 
    	}
    	else
    	{
    		status = status.replace('\n', ' ');
    		if(status.length() <= 140)
    		{
        		Long postid = R.str_long.incr("nextPostId");
    			Long userId = PageUtils.getUserID(request);
	    		long time = System.currentTimeMillis();
	    		String post=Long.toString(userId)+"|"+Long.toString(time)+"|"+status;
	    		R.c_str_str.set("p:"+Long.toString(postid), post);
	    		List<Long> followersList = R.str_long.smembers(Long.toString(userId)+":followers");
	    		if(followersList == null)
	    		{
	    			followersList = new ArrayList<Long>();
	    		}
	    		// use a set to remove dups
	    		HashSet<Long> followerSet = new HashSet<Long>(followersList);
	    		followerSet.add(userId);
	    		
	    		// handle replies
	    		long replyId = PageUtils.isReply(status);
	    		if(replyId != -1)
	    			followerSet.add(new Long(replyId));

	    		// this will become problematic if someone has thousands of followers.
	    		// needs to be done asynchronously in blocks if possible
	    		for(Long i : followerSet)
	    		{
	    			R.str_long.lpush(Long.toString(i)+":posts", postid);
	    		}
	    		// -1 uid is global timeline
	    		String globalKey = Long.toString(-1)+":posts";
	    		R.str_long.lpush(globalKey,postid);
	    		R.str_long.ltrim(globalKey, 200);
	    		%> <jsp:forward page="index.jsp"/> <%
    		}
    		else
    		{
    			%> <%= PageUtils.goback("The post must be < 140 characters!") %> <%
    		}
    	}
%>