<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="java.net.URI" %>  
    <%@ page import="control.ImDrawRect" %> 
     <%@ page import="java.awt.image.BufferedImage" %> 
     <%@page import="javax.imageio.ImageIO"%>
	<%@page import="java.io.*"%>
     
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Selected Image</title>
</head>
<body>

<%
String[][] single_result= (String[][]) session.getAttribute("sinres");
String[] resultID=single_result[0]; //sono gli id delle captions
String[] resultSentence=single_result[1];
String[] resultImageID=single_result[2];
String[] resultCategories=single_result[3];
String[] resultBBox=single_result[4];

String cat=resultCategories[0].substring(0,resultCategories[0].length()-2); //elimino l'ultimo " #"
String[] categories=cat.split(" # ");
String[] bbox=resultBBox[0].split("#");

String imageId=resultImageID[0]; //é sempre la stessa
int imageIdSize=imageId.length();

int k;
String zeroString="";
for (k=0;k<12-imageIdSize;k++) //Aggiungo gli zero necessari
{
	zeroString=zeroString+"0";
}

String imageName="COCO_val2014_"+zeroString+imageId+".jpg"; 

//Non mostro l'immagine originale quindi non ho necessità di recuperarla 
//String contextPath = request.getContextPath(); 
//URI imURI = new URI(contextPath+"/images/val2014/"+imageName);

//Creo e mostro l'immagine con i riquadri degli oggetti
//http://stackoverflow.com/questions/2438375/how-to-convert-bufferedimage-to-image-to-display-on-jsp
BufferedImage bImage = ImDrawRect.drawAndGetImage(imageName, bbox);

if (bImage!=null)
{
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ImageIO.write( bImage, "jpg", baos );
	baos.flush();
	byte[] imageInByteArray = baos.toByteArray();
	baos.close();
	String b64 = javax.xml.bind.DatatypeConverter.printBase64Binary(imageInByteArray); //base 64 string
	%>
<h2 align="center"><img src="data:image/jpg;base64, <%=b64%>" alt="Image not found" /> </h2><% 
}
else 
{
	%>IMAGE LOADING FAILED<% 
}

%>
<h2 align="center">Image ID: <%=imageId %> </h2>
<h2 align="center">Image Name: <%=imageName %> </h2>

<h2 align="center">Captions: </h2>
<%
String[] captions=resultSentence[0].split("#");
String[] captionsID=resultID[0].split("#");
int i;
for (i=0;i<captionsID.length;i++)
	{
	%>
	<p  align="center"><%=captionsID[i] %>) <%=captions[i] %> </p>
<%} %>

<h2 align="center">Objects: </h2>
<%

for (i=0;i<categories.length;i++)
	{
	String color="#"+ImDrawRect.COLOR[i%10];
	%>
	<p  align="center"> <font color=<%=color %>> <%=categories[i] %>) <%=bbox[i] %> </font> </p>
<%} 

if (session.getAttribute("gscheck")!=null)
{
	%>
	<form action="ShowGoldStandard.jsp"> 
	<!-- Bottone Indietro -->
	<h2 align="center">
		<input name="Back" type="submit" id="back" value="back">
	</h2>
	</form>
	<%
}
else
{
	%>
	<form action="Intro.jsp"> 
	<!-- Bottone Indietro -->
	<h2 align="center">
		<input name="Back" type="submit" id="back" value="back">
	</h2>
	</form>
	<%
}
%>
</body>
</html>