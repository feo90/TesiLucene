package control;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import entities.Constants;

public abstract class ImResizer 
{
	private static double RATIO=Constants.getRATIO();
	private static String TUMB_LOCATION=Constants.getTUMB_LOCATION();
	private static String IMAGE_LOCATION=Constants.getIMAGE_LOCATION();
	
	/**
	 * Questo metodo si occupa di creare le thumbnail di tutti i file nella directory IMAGE_LOCATION e metterle in TUMB_LOCATION
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean resizeAllImages() throws IOException
	{
		//System.out.println("Sono in resizeAllImages");
		File folder = new File(IMAGE_LOCATION);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
		      if (listOfFiles[i].isFile()) 
		      {
		        //System.out.println("File " + listOfFiles[i].getName());
		        
		        //Controllo se non esiste già la thumbnail 
		        File f = new File(TUMB_LOCATION+listOfFiles[i].getName());
		        if(!f.exists()) 
		        { 
		            if (!resizeSingleImage(IMAGE_LOCATION+listOfFiles[i].getName())) //Se non riesco a fare la thumbnail del file
		            {
		            	System.out.println("ERRORE: thumbnail del file: "+IMAGE_LOCATION+listOfFiles[i].getName()+" fallita!");
		            }
		        }	 
		        else
		        {
		        	//System.out.println("Il file: "+IMAGE_LOCATION+listOfFiles[i].getName()+" esiste già");
		        }
		      } 
		      else if (listOfFiles[i].isDirectory()) 
		      {
		        //System.out.println("Directory " + listOfFiles[i].getName());
		      }
		}
		return true;
	}
	
	
	/**
	 * Questo metodo riduce una singola immagine
	 * @param immPath
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean resizeSingleImage(String immPath) throws IOException
	{
		try
		{
			BufferedImage img = ImageIO.read(new File (immPath)); //image loading
			BufferedImage scaled = scale(img); //scaling
	
			//path retriver 
			Path p = Paths.get(immPath);
			String file_name = p.getFileName().toString();
			File output= new File (TUMB_LOCATION+file_name);
			
			return ImageIO.write(scaled, "jpg", output);
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("ERRORE: L'immagine: "+immPath+" ha sollevato un'eccezione del tipo IllegalArgumentException");	
			return false;
		}
	}
	
	
	/**
	 * Questa funzione si occupa di creare l'immagine rimpicciolita 
	 * @param source: l'immagine originale nel formato BufferedImage
	 * @return BufferedImage: l'immagine rimpicciolita 
	 */
	private static BufferedImage scale(BufferedImage source) 
	{
		  int w = (int) (source.getWidth() * RATIO);
		  int h = (int) (source.getHeight() * RATIO);
		  
		  BufferedImage bi = getCompatibleImage(w, h);
		  Graphics2D g2d = bi.createGraphics();
		  
		  double xScale = (double) w / source.getWidth();
		  double yScale = (double) h / source.getHeight();
		  
		  AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
		  g2d.drawRenderedImage(source, at);
		  g2d.dispose();
		  return bi;
	}

	private static BufferedImage getCompatibleImage(int w, int h) 
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		return image;
	}
	

}
