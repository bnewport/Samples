
<%@page import="com.devwebsphere.rediswxs.*"%>
<%@page import="utils.PageUtils"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%
	String username = PageUtils.getParam(request, "username");
	String password = PageUtils.getParam(request, "password");
	Long userid = R.str_long.get("un:"+username+":id");
	if(userid == null)
	{
		%> <%= PageUtils.goback("Wrong username or password") %> <%
	}
	else
	{
		String passwordKey = "u:"+Long.toString(userid)+":password";
		String storedPassword = R.str_str.get(passwordKey);
		String encryptedPassword = PageUtils.hashPassword(password);
		if(encryptedPassword == null) encryptedPassword = password;
		if(storedPassword.equals(password) == false && encryptedPassword.equals(storedPassword) == false)
		{
			%> <%= PageUtils.goback("Wrong username of password") %> <%
		}
		else
		{
			PageUtils.loadUserInfo(request, userid);
			if(password.equals(storedPassword))
			{
				// reencrypt password if needed
				R.str_str.set(passwordKey, encryptedPassword);
			}
			%> <jsp:forward page="index.jsp"/> <%
		}
	}
%>
