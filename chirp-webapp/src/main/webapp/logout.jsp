<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page session="false"%>
<%@page import="utils.PageUtils"%>
<%
	PageUtils.logout(request);
%>
Logged out.