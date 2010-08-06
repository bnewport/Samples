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
		mapName = "map";
	String key = request.getParameter("key");
	
	WXSMap<String, String> map = client.getCache(mapName);
	if(key != null)
	{
		String oldValue = map.remove(key);
		
		if(oldValue != null)
		{
			%>Old Value was '<%= oldValue %>'<br><%
		}
		else
		{
			%>Key wasn't found<br><%
		}
	}
	else
	{
		%>No key parameter specified<br><%
	}
%>
</body>
</html>