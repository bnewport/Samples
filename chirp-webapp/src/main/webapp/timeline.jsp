<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="utils.PageUtils"%><jsp:include page="header.jsp"/>
<div id="timeline">
<h2>Timeline</h2>
<div id="users">
<i>Latest registered users</i><br>
<%
	StringBuilder buffUsers = new StringBuilder();
	PageUtils.showLastUsers(buffUsers, 50);
	%><%= buffUsers.toString() %><%

//	PageUtils.show();
%>
</div>
<br><i>Latest 50 messages from users around the world!</i><br>
<%
	StringBuilder buff = new StringBuilder();
	PageUtils.showUserPosts(buff, -1, 0, 50);
%>
<%= buff.toString() %>
</div>
<jsp:include page="footer.jsp"/>
