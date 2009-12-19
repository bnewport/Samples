<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="utils.PageUtils"%>
<%@page import="com.devwebsphere.rediswxs.*"%>
<jsp:include page="header.jsp"/>
<div class="content">
<%
	String username = PageUtils.getParam(request, "username");
	String password = PageUtils.getParam(request, "password");
	String password2 = PageUtils.getParam(request, "password2");
	if(username == null || password == null || password2 == null)
	{
		%> <%= PageUtils.goback("Every field of the registration form is needed") %> <%
	} else if(password.equals(password2) == false)
	{
		%> <%= PageUtils.goback("The two passwords don't match!") %> <%
	} else if(username.length() > PageUtils.MAX_USERNAME_SIZE)
	{
		%> <%= PageUtils.goback("Maximum user name is " + PageUtils.MAX_USERNAME_SIZE + " characters long!") %> <%
	}
	else
	{
		if(R.c_str_long.get("un:"+username + ":id") != null)
		{
			%> <%= PageUtils.goback("Sorry, the selected username is already in use") %> <%
		}
		else
		{
			long userid = R.str_long.incr("nextUserId");
			R.c_str_long.set("un:"+username + ":id", userid);
			R.c_str_str.set("u:"+Long.toString(userid)+":username", username);
			
			String encryptedPassword = PageUtils.hashPassword(password);
			if(encryptedPassword == null)
				encryptedPassword = password;
			R.c_str_str.set("u:"+Long.toString(userid)+":password", encryptedPassword);
			
			R.str_long.sadd("users", userid);
			R.str_long.lpush("last50users", userid);
			R.str_long.ltrim("last50users", 50);
			
			PageUtils.loadUserInfo(request, userid);
			
			%><h2>Welcome aboard!</h2>Hey <%= PageUtils.escapeHtmlFull(username) %>, now that you have an account, <a href="index.jsp"> a good start is to write your first message!</a> <%
		}
	}
%>
</div>
<jsp:include page="footer.jsp"/>