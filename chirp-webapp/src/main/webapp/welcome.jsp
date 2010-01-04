<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<div id="welcomebox"><b>Hello!</b> Chirp is a very simple 
<a style="color: #00a1cf;" href="http://twitter.com">Twitter</a>-like application 
that demonstrates the power of <a style="color: #00a1cf;"
	href="http://www-01.ibm.com/software/webservers/appserv/extremescale//">IBM
WebSphere eXtreme Scale</a>.  Key
points:
<ul>
	<li>WebSphere eXtreme Scale is an elastic key-graph in memory data grid.</li>
	<li>WebSphere eXtreme Scale is writing all data to a relational SQL database which can be sharded
	using JDBC APIs.</li>
	<li>WebSphere eXtreme Scale can
	automatically linearly scale as more boxes are added to the grid. More
	boxes increases memory capacity and processing and network performance
	in a linear fashion.</li>
	<li>The web application communicates using a Redis like API which
	sits in front of the WXS Java client.</li>
	<li>The source code of this application, and a tutorial explaining
	its design, will be available as a sample in WebSphere eXtreme Scale v7.0.</li>
	
</ul>
<table>
	<tr>
		<td style="font-family:'Arial';font-size:24px;color:#f47920;font-weight:bold;">Register</td>
		<td width="10px">
		</td>
		<td style="font-family:'Arial';font-size:24px;color:#6ec200;font-weight:bold;">Login</td>
	</tr>
	<tr>
		<td>
		<div class="register">
		<form method="POST" action="register.jsp">
		<table>
			<tr>
				<td>username</td>
				<td><input type="text" name="username"></td>
			</tr>
			<tr>
				<td>password</td>
				<td><input type="password" name="password"></td>
			</tr>
			<tr>
				<td>confirm password</td>
				<td><input type="password" name="password2"></td>
			</tr>
			<tr>
				<td colspan="2" align="right"><input type="submit" name="doit"
					value="Create an account"></td>
			</tr>
		</table>
		</form>
		</div>
		</td>
		<td></td>
		<td valign="top">
		<form method="POST" action="login.jsp">
		<table class="login">
			<tr>
				<td>username</td>
				<td><input type="text" name="username"></td>
			</tr>
			<tr>
				<td>password</td>
				<td><input type="password" name="password"></td>
			</tr>
			<tr>
				<td colspan="2" align="right"><input type="submit" name="doit" value="Login"></td>
			</tr>
		</table>
		</form>
		</td>
	</tr>
</table>
</div>

