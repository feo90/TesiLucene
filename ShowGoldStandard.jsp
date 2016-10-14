<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="java.net.URI" %>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Gold Standard</title>
</head>
<body>
<h2>Query: <%=session.getAttribute("query") %></h2>
<form action="SingleImageServlet">
 <!-- Segnalo che sto guardando le immagini del GS -->
<input type="hidden" name="show" value="true">

<% String[] gs = (String[]) session.getAttribute("gs"); 
if (gs!=null)
{
	int i;
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
		 <button type="submit" name="imagebt" value="<%=imageId %>"><img src="<%=imURI %>" ></button>
		 <!-- Menu a tendina per  la valutazione -->
	  	<select name="<%=cb %>" >
   		<option value="relevant" selected="selected">relevant </option>
   		<option value="irrelevant">irrelevant  </option>
  		</select>
	<%
	}
	%>
	<!-- Bottone Edit Gold Standard -->
	<p>
		<input name="edit" type="submit" id="edit" value="edit gold standard">
	</p>
	<% 	
}
else
{
%>
<h3>THERE ISN'T A GOLD STANDARD FOR THIS QUERY</h3>
<% 
}%>
</form>
<!-- Bottone Indietro -->
<form action="Intro.jsp"> 
	<h2 align="center">
		<input name="Back" type="submit" id="back" value="back">
	</h2>
</form>

</body>
</html>