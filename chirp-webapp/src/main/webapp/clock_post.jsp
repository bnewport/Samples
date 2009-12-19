
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
<% long duration = 300;
long interval = 10;
%>
	Clock load driver
	<br>
	<%
	long startTime = System.currentTimeMillis();
	String temp = PageUtils.getParam(request, "duration");
	if (temp != null) duration = Long.parseLong(temp);
	long endTime = startTime + duration*1000 ;
	long currTime = startTime;
	
	temp = PageUtils.getParam(request, "interval");
	if (temp != null) interval = Long.parseLong(temp);
	
    String username = PageUtils.getParam(request,"user");
    username = username==null ? "clock" : username;
	Long userId = R.c_str_long.get("un:"+username+":id");
	PageUtils.loadUserInfo(request, userId);
	
	while (currTime <= endTime) {
		long time = System.currentTimeMillis();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
		String timeStr = df.format(new Date(time));
	
		String status = "It is now " + timeStr;
	    Long postid = R.str_long.incr("nextPostId");
	  	String post=Long.toString(userId)+"|"+Long.toString(time)+"|"+status;
	  	long before = System.currentTimeMillis();
	  	R.c_str_str.set("p:"+Long.toString(postid), post);
	  	System.out.println("near cache post=" + (System.currentTimeMillis()-before));
	  	//System.out.println("finding followers");
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
	  	//System.out.println("posting to global timeline");
	  	// -1 uid is global timeline
	  	String globalKey = Long.toString(-1)+":posts";
	  	before = System.currentTimeMillis();
	  	R.str_long.lpush(globalKey,postid);
	  	System.out.println("grid post=" + (System.currentTimeMillis()-before));
	  	R.str_long.ltrim(globalKey, 200);

	  	if (currTime < endTime) 
	  		Thread.sleep(interval*1000);
		currTime = System.currentTimeMillis();
	}
%>
End.