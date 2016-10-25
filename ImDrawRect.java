package control;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import entities.Constants;

public abstract class ImDrawRect 
{
	private static String IMAGE_LOCATION=Constants.getIMAGE_LOCATION();
	public static String[] COLOR=Constants.getCOLOR();
	
	
	/**
	 * Questo metodo disegna i riquadri sull'immagine e la restituisce come BufferedImage
	 * @param imName
	 * @param bbox
	 * @return BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage drawAndGetImage(String imName,String[] bbox)
	{
		//System.out.println("Sono in drawAndGetImage");
		String imPath=IMAGE_LOCATION+imName;
		
		//image loading
		BufferedImage img=null;
		try 
		{
			img = ImageIO.read(new File (imPath));
		} 
		catch (IllegalArgumentException | IOException e1) 
		{
			System.out.println("ERROR: Can't load image: "+imPath);
			e1.printStackTrace();
			return null;
		} 
		
		//Trovo 
		int i;
		for (i=0;i<bbox.length;i++)
		{
			int[] bboxInt=FindBBox(bbox[i]);
			int x=bboxInt[0];
			int y=bboxInt[1];
			int width=bboxInt[2];
			int height=bboxInt[3];
			
			String col=COLOR[i%10];
			
			try
			{
				int j;
				//Traccio le linee orizzontali
				for (j=0;j<width;j++)
				{
					img.setRGB(x+j,y,Integer.parseInt(col, 16));
					img.setRGB(x+j,y+height,Integer.parseInt(col, 16));
				}
				
				//Traccio le linee verticali
				for (j=0;j<height;j++)
				{
					img.setRGB(x,y+j,Integer.parseInt(col, 16));
					img.setRGB(x+width,y+j,Integer.parseInt(col, 16));
				}
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				System.out.println("ERROR: CAN'T DRAW THE BOUNDING BOX ON IMAGE "+imName);
				System.out.println(e);
			}		
		}
		return img;
	}



	/**
	 * questo metodo si occupa di convertire il contenuto di una singola bounding box nei suoi valori interi
	 * @param bbox come String
	 * @return int[] x,y,width,height
	 */
	private static int[] FindBBox(String bbox) 
	{
		//System.out.println("Sono in FindBBox");
		
		bbox=bbox.substring(1, bbox.length()-1); //tolgo le [ ]
		String[] stringA=bbox.split(", ");
		int[] stringInt=new int[4];
		
		int i;
		for (i=0;i<stringA.length;i++)
		{
			//System.out.println("l'elemento "+i+" è "+stringA[i]);
			try
			{
				stringInt[i]=Integer.parseInt(stringA[i]);
			}
			catch (NumberFormatException e)
			{
				//System.out.println("WARNING: "+ stringA[i]+" ISN'T AN INTEGER, WE WILL TRY TO PARSE LIKE A FLOAT");
				
				float num=Float.parseFloat(stringA[i]);
				
				try
				{
					stringInt[i]= (int) num;
				}
				catch (NumberFormatException f)
				{
					System.out.println("ERROR: FLOAT PARSING FAILED");
				}
			}
		}
		return stringInt;
	}


}
