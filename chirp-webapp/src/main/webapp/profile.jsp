<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="utils.PageUtils"%><jsp:include page="header.jsp"/>
<div class="content">
<%
	String u = PageUtils.getParam(request, "u");
	Long userid = R.c_str_long.get("un:"+u+":id");
	if(u == null || userid == null)
	{
		%> <jsp:forward page="index.jsp"/> <%
	}
	else
	{
		%><h2 class="username"><%= PageUtils.escapeHtmlFull(u) %> </h2><%
		Long loggedInUID = PageUtils.getUserID(request);
		if(PageUtils.isLoggedIn(request) && !loggedInUID.equals(userid))
		{
			Boolean isFollowing = R.str_long.sismember(loggedInUID.toString()+":following", userid);
			if(isFollowing == Boolean.FALSE)
			{
				%><a href="follow.jsp?uid=<%= userid.toString() %>&f=1" class="button">Follow this user</a><%
			}
			else
			{
				%><a href="follow.jsp?uid=<%= userid.toString() %>&f=0" class="button">Stop following</a><%
			}
		}
		String startString = PageUtils.getParam(request, "start");
		int start = startString == null ? 0 : Integer.parseInt(startString);
		StringBuilder buff = new StringBuilder();
		PageUtils.showPagesWithPagination(request, buff, u, userid, start, 10);
		%> <%= buff.toString() %>
		</div>
		<jsp:include page="footer.jsp"/>
		<%
	}
%>
