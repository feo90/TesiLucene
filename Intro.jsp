<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="org.apache.lucene.search.ScoreDoc"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Lucene and JSP Example</title>
</head>
<body>
	<form action="SearchServlet">
	<p>Search:</p>
	
	<!-- Input Testo -->
	<p><input name="search" type="text" id="search"></p>
	
	<!-- Bottone -->
	<p>
		<input name="Search" type="submit" id="start" value="start">
	</p>
	<%
	String[][] result = (String[][]) session.getAttribute("result");
	if (result!=null)
	{
		String[] resultID=result[0];
		String[] resultSentence=result[1];
		%>
		<%="Found " + resultID.length + " hits." %>
		
		<%
		int i;
		for (i=0;i<resultID.length;i++)
		{
			String line=(i+1)+". "+resultID[i]+" \t"+resultSentence[i];
		%>
			<p><%=line%></p>
		<%
		}
		%>	
	<%
	} 
	%>	
	
	</form>
</body>
</html>