
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="utils.PageUtils"%>
<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="utils.PageUtils"%>

<%
String[] msgs = new String[6];
msgs[0] = "good morning, it's day 4 at Impact";
msgs[1] = "walking through the hotel to main tent";
msgs[2] = "time for hair and makeup";
msgs[3] = "getting the mic hooked up";
msgs[4] = "walking out on stage";
msgs[5] = "hello Vegas!";

Long uidObj = R.c_str_long.get("un:Patty:id");
long userId = uidObj == null? 0 : uidObj.longValue();
PageUtils.loadUserInfo(request, userId);

	for (int i=0; i<msgs.length; i++) {
		long time = System.currentTimeMillis();
	    Long postid = R.str_long.incr("nextPostId");
	  	String post=Long.toString(userId)+"|"+Long.toString(time)+"|"+msgs[i];
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
	  	long replyId = PageUtils.isReply(msgs[i]);
	  	if(replyId != -1)
	  		followerSet.add(new Long(replyId));
	
	  	// this will become problematic if someone has thousands of followers.
	  	// needs to be done asynchronously in blocks if possible
	  	for(Long j : followerSet)
	  	{
	  		R.str_long.lpush(Long.toString(j)+":posts", postid);
	  	}
	  	//System.out.println("posting to global timeline");
	  	// -1 uid is global timeline
	  	String globalKey = Long.toString(-1)+":posts";
	  	R.str_long.lpush(globalKey,postid);
	  	R.str_long.ltrim(globalKey, 200);
	  	Thread.sleep(60000);
	}
%>
