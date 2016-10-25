<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="control.GoldStandardControl" %> 
    <%@ page import="java.util.LinkedList" %> 
    <%@ page import="java.net.URI" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Gold Standards</title>
</head>
<body>
<form action="AllGoldStandards.jsp">
	<h1 align="center" >Image Retrieval on CoCo</h1>
	<h2 align="center" > Select one of the query to show the gold standard  </h2>

	<%
	LinkedList<String> gs_list=GoldStandardControl.findAllGoldStandards();
	%>
	
	<!-- Menu a tendina -->
	<h2 align="center" >
	<select name="query">
	<%
	int i;
	for (i=0;i<gs_list.size();i++)
	{
		%>
		<option value="<%=gs_list.get(i) %>"><%=gs_list.get(i) %></option>
		<%
	}
	%>
  </select>
	</h2>
	
	<h2 align="center" >
		<input name="showGS" type="submit" id="show" value="Show Gold Standard">
	</h2>
</form>

<%
String query=request.getParameter("query");

if (query!=null)
{
	String[] gs=GoldStandardControl.findGoldStandard(query);
	session.setAttribute("query", query);
	session.setAttribute("gs",gs);
	
	%>
	<h2> Query: <%=query %> </h2>
	<% 
	
	for (i=0;i<gs.length;i++)
	{
		//numero
		String num=(i+1)+")";

		//Ricerco l'immagine associata
		String imageId=gs[i];
		int imageIdSize=imageId.length();
		
		int k;
		String zeroString="";
		for (k=0;k<12-imageIdSize;k++) //Aggiungo gli zero necessari
		{
			zeroString=zeroString+"0";
		}
		
		String imageName="COCO_val2014_"+zeroString+imageId+".jpg"; //CERCO SOLO NELLE IMMAGINI VAL2014 PER IL MOMENTO!
		String cb="cb"+i;
	%>	
		<%=num%> <%=imageId%>
		
		<% 
		String contextPath = request.getContextPath(); 
		URI imURI = new URI(contextPath+"/images/thumbnails/"+imageName);
		%>			
		 <img src="<%=imURI %>" >
	<%
	}
	%>
	
	<!-- Bottone Edit Gold Standard -->
	<form action="ShowGoldStandard.jsp">
	<p>
		<input name="edit" type="submit" id="edit" value="edit gold standard">
	</p>
	</form>
	
<%} %>

<!-- Bottone Indietro -->
<form action="Intro.jsp"> 
	<h2 align="center">
		<input name="Back" type="submit" id="back" value="back">
	</h2>
</form>
</body>
</html>