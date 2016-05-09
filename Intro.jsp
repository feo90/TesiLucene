<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="org.apache.lucene.search.ScoreDoc"%>
    <%@ page import="java.net.URI" %>  
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
	
	<!--VECCHIO METODO DIRETTO
	 <p><img src="${pageContext.request.contextPath}/images/im.gif"/></p>	 -->
	
	<%
	String[][] result = (String[][]) session.getAttribute("result");
	if (result!=null)
	{
		String[] resultID=result[0];
		String[] resultSentence=result[1];
		String[] resultImageID=result[2];
		%>
		<%="Found " + resultID.length + " hits." %>
		
		<%
		int i;
		for (i=0;i<resultID.length;i++)
		{
			//Questa è la stringa testuale
			String line=(i+1)+") ID: "+resultID[i]+" \t Image ID: "+resultImageID[i]+"\t Caption "+resultSentence[i];
			
			//Ricerco l'immagine associata
			String imageId=resultImageID[i];
			int imageIdSize=imageId.length();
			
			int j;
			String zeroString="";
			for (j=0;j<12-imageIdSize;j++) //Aggiungo gli zero necessari
			{
				zeroString=zeroString+"0";
			}
			
			String imageName="COCO_val2014_"+zeroString+imageId+".jpg"; //CERCO SOLO NELLE IMMAGINI VAL2014 PER IL MOMENTO!
		%>
			<p><%=line%></p>
			<p><%=imageName %></p>	
			
			<% String contextPath = request.getContextPath(); 
			URI imURI = new URI(contextPath+"/images/val2014/"+imageName);%>			
			<p> <img src="<%=imURI %>"/></p>		
		<%
		}
		%>	
	<%
	} 
	%>	
	
	</form>
</body>
</html>