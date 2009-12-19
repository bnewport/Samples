
<%@page import="utils.PageUtils"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.net.URLEncoder"%>

<%
	if(PageUtils.isLoggedIn(request) == false)
	{
		%> <jsp:forward page="index.jsp"/> <%
	}
	else
	{
		String username = PageUtils.getUsername(request);
		Long userId = PageUtils.getUserID(request);
		%> <jsp:include page="header.jsp"/>
		<div class="content">
		<div id="postform">
		<div class="update">
		<form method="POST" action="post.jsp">
		<table>
		<tr><td><b><%=PageUtils.escapeHtmlFull(PageUtils.getUsername(request))%></b>, what you are doing?
		</td></tr>
		<tr><td><textarea cols="75" rows="2" name="status"></textarea>
		</td></tr>
		<tr><td align="right"><input type="submit" name="doit" value="Update"></td></tr>
		<tr><td><br></td></tr>
		</table>
		</form>
		</div>
		<div id="homeinfobox">
		<%= R.str_long.scard(Long.toString(userId) + ":followers") %> followers<br>
		<%
   		List<Long> followersList = R.str_long.smembers(Long.toString(userId)+":followers");
   		if(followersList == null)
   		{
   			followersList = new ArrayList<Long>();
   		}
   		// use a set to remove dups
   		HashSet<Long> followerSet = new HashSet<Long>(followersList);
   		for(Long i : followerSet)
		{
            String fname = R.c_str_str.get("u:"+i.toString()+ ":username");
            if (fname !=null) {
                    if (fname.startsWith("user")) continue;
    	%>
    	<a class="follower" href="profile.jsp?u=<%=URLEncoder.encode(fname)%>"><%=PageUtils.escapeHtmlFull(fname)%></a>
    	<br>
    	<%
            }
    	}
		%>		
		<br>
		<br>
		<%= R.str_long.scard(Long.toString(userId) + ":following") %> following<br>
		<%
		List<Long> followingList = R.str_long.smembers(Long.toString(userId)+":following");
   		if(followingList == null)
   		{
   			followingList = new ArrayList<Long>();
   		}
   		// use a set to remove dups
   		HashSet<Long> followingSet = new HashSet<Long>(followingList);
   		for(Long i : followingSet)
		{
   			String fname = R.c_str_str.get("u:"+i.toString()+ ":username");
   		%>
   		<a class="follower" href="profile.jsp?u=<%=URLEncoder.encode(fname)%>"><%=PageUtils.escapeHtmlFull(fname)%></a>
   		<br>
   		<%	
		}   		
	    %>		
		</div>
		</div>
		<div style="width:690px;">
		<%
			String startString = PageUtils.getParam(request,"start");
			int start = 0;
			if(startString != null)
				start = Integer.parseInt(startString);
			StringBuilder buff = new StringBuilder();
			PageUtils.showPagesWithPagination(request, buff, username, userId, start, 50);
		%> 
			<%=buff.toString()%>

		</div>
		</div>
		<jsp:include page="footer.jsp"/>
		<%
	}
	
%>