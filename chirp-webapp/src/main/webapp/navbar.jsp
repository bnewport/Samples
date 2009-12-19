<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="utils.PageUtils"%><div id="navbar">
<br>
<br>
<a href="index.jsp">home</a>
| <a href="timeline.jsp">timeline</a>
<%
	if(PageUtils.isLoggedIn(request))
	{
		%> | <a href="logout.jsp">logout</a> <%
	}
%>
</div>
