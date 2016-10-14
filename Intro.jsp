<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@page import="org.apache.lucene.search.ScoreDoc"%>
    <%@ page import="java.net.URI" %>
    <%@ page import="control.StatisticControl" %>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Searching on CoCo</title>
</head>
<body>
	<form action="SearchServlet">
	<h1 align="center" >Image Retrieval on CoCo</h1>
	<h2 align="center" > 40.000 images with 5 captions each end 80 object categories in total  </h2>
	
	<!-- Input Testo -->
	<h2 align="center" > <input name="search" type="text" id="search"></h2>
	
	<!-- Menu a tendina -->
	<h2 align="center" >
	<select name="mode">
    <option value="cap">Captions</option>
    <option value="cat">Category</option>
    <option value="both">Captions and Category</option>
    <option value="capplus">Captions+</option>
    <option value="catplus">Category+</option>
    <option value="bothplus">Captions+ and Category+</option>
  </select>
	</h2>
	
		<!-- Bottone Ricerca -->
	<h2 align="center" >
		<input name="start_search" type="submit" id="start" value="Search">
	</h2>

<!-- Bottone per mostrare il GS -->
<h2 align="center" >
		<input name="showGS" type="submit" id="show" value="Show Gold Standard">
	</h2>
	</form>

<form action="GoldStandard.jsp"> 
	
	<!-- Trasferisco i risultati della precedente ricerca se esiste -->
	<input type="hidden" name="query" value="<%=session.getAttribute("query") %>">
	
	<% String[][] result1=(String[][]) session.getAttribute("result"); 
	if (result1!=null)
	{
		session.setAttribute("result", result1);
		%>
		
		<!-- Bottone Export Gold Standard -->
	<p>
		<input name="Export" type="submit" id="export" value="export">
	</p>	
	<%} %>
</form>
		
	<%String[][] result = (String[][]) session.getAttribute("result");
	if (result!=null)
	{
		session.setAttribute("result", result);
		String[] resultID=result[0];
		String[] resultSentence=result[1];
		String[] resultImageID=result[2];
		String[] resultCategories=result[3];
		
		//Calcolo delle statistiche
		String query= (String) session.getAttribute("query");
		float[] statistic=StatisticControl.findStatistc( resultImageID,  query);
		%>
		
		<p><%="Found " + resultID.length + " hits for the query: " %> "<%=query %>"</p>
		<% if (statistic!=null)
			{
			float precision= statistic[0]*100;
			float recall= statistic[1]*100;
			float precision_ten= statistic[2]*100;
			float recall_ten= statistic[3]*100;
			float average_prec= statistic[4]*100;
			float average_prec_ten= statistic[5]*100;
			float f1= statistic[6]*100;
			float f1_ten= statistic[7]*100;
			%>
		<p>precision: <%=precision %>% , recall: <%=recall %>% , average precision: <%=average_prec %>% , F1: <%=f1 %>%</p>
		<p>In top 10: precision: <%=precision_ten %>%, recall: <%=recall_ten %>% , average precision: <%=average_prec_ten %>% , F1: <%=f1_ten %>% </p>
		<%}
		else 
		{
		%>
		<p>unfortunately doesn't exist a gold standard for this query yet</p>
		<%} %>
		
		<form action="SingleImageServlet"> 
		<%
		int i;
		for (i=0;i<resultID.length;i++)
		{
			//numero
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
		%>
			
			<%=num%> 
			<% String contextPath = request.getContextPath(); 
			URI imURI = new URI(contextPath+"/images/thumbnails/"+imageName);
			String caption=resultSentence[i];
			%>			
			 <button type="submit" name="imagebt" value="<%=imageId %>"><img src="<%=imURI %>" ></button>
		<%
		}
		%>
		</form>
	<%
	} 
	%>	
</body>
</html>