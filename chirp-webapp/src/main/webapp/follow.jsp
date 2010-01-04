
<%@page import="utils.PageUtils"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>


<%@page import="com.devwebsphere.rediswxs.*"%>
<%
	String uid_str = PageUtils.getParam(request, "uid");
	String f_str = PageUtils.getParam(request, "f");
	String username = R.str_str.get("u:"+uid_str+":username");
	if(PageUtils.isLoggedIn(request) == false || uid_str == null || f_str == null)
	{
		%><jsp:forward page="index.jsp"/><%
	}
	else
	{
		int f = Integer.parseInt(f_str);
		Long uid = Long.parseLong(uid_str);
		if(f != 0)
		{
			R.str_long.sadd(uid+":followers", PageUtils.getUserID(request));
			R.str_long.sadd(PageUtils.getUserID(request)+":following", uid);
		}
		else
		{
			R.str_long.srem(uid+":followers", PageUtils.getUserID(request));
			R.str_long.srem(PageUtils.getUserID(request)+":following", uid);
		}
		%> <jsp:forward page="profile.jsp"><jsp:param name="u" value="<%= PageUtils.escapeHtmlFull(username) %>"/></jsp:forward> <%
	}
%>
