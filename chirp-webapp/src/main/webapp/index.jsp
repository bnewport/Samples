<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="utils.PageUtils"%>

<%
	if(!PageUtils.isLoggedIn(request))
	{
%>

<jsp:include page="header.jsp"/>
<jsp:include page="welcome.jsp"/>
<jsp:include page="footer.jsp"/>

<%
	}
	else
	{
		%> <jsp:forward page="home.jsp"/> <%
	}
%>