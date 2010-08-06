<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="com.devwebsphere.wxsutils.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
	WXSUtils client = WXSUtils.getDefaultUtils();

	String mapName = request.getParameter("map");
	if(mapName == null)
		mapName = "Map1";
	String key = request.getParameter("key");
	String value = request.getParameter("value");
	
	WXSMap<String, String> map = client.getCache(mapName);
	
	if(key != null && value != null)
	{
		map.put(key, value);
		%>New Value is '<%= value %>'<br><%
	}
	else
	{
		if(key == null) %>Parameter key not specified<br><%;
		if(value == null) %>Parameter value not specified<br><%;
	}
%>
</body>
</html>