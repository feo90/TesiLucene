<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="java.net.URI" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Gold Standard Setting</title>
</head>
<body>
<form action="ExportGS"> 
<%String[][] result = (String[][]) session.getAttribute("result");
String query=(String) request.getParameter("query");
session.setAttribute("query", query);

	if (result!=null)
	{
		String[] resultID=result[0];
		String[] resultImageID=result[2];
		session.setAttribute("resultImID", resultImageID);
		
		int i;
		for (i=0;i<resultID.length;i++)
		{
			//Questa è la stringa testuale
			String num=(i+1)+")";
			//Ricerco l'immagine associata
			String imageId=resultImageID[i];
			int imageIdSize=imageId.length();
			
			int k;
			String zeroString="";
			for (k=0;k<12-imageIdSize;k++) //Aggiungo gli zero necessari
			{
				zeroString=zeroString+"0";
			}
			
			String imageName="COCO_val2014_"+zeroString+imageId+".jpg"; //CERCO SOLO NELLE IMMAGINI VAL2014 PER IL MOMENTO!
			String contextPath = request.getContextPath(); 
			URI imURI = new URI(contextPath+"/images/thumbnails/"+imageName);
			String cb="cb"+i;
			%>		
			<%=num %>
			 <img src="<%=imURI %>"/>
			 <!-- Menu a tendina per  la valutazione -->
	  		<select name="<%=cb %>" >
	  		<%if (i<20) 
	  		{%>
	  		<option value="relevant" selected="selected">relevant </option>
   			<option value="irrelevant">irrelevant  </option>
	  		<%}
	  		else 
	  		{%>
	  		<option value="relevant" >relevant </option>
   			<option value="irrelevant" selected="selected">irrelevant  </option>
	  		<%} %>
  			</select>
		<% }
		%>
		<!-- Bottone Export Gold Standard -->
		<h2 align="center">
			<input name="Export" type="submit" id="export" value="export">
		</h2>	
		<%
	} 
	%>
	</form>	
	<form action="Intro.jsp"> 
	<!-- Bottone Indietro -->
	<h2 align="center">
		<input name="Back" type="submit" id="back" value="back">
	</h2>
</form>
</body>
</html>