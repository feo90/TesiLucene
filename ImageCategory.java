package entities;

import java.util.LinkedList;

public class ImageCategory 
{

	//Attributi
	private String id_image;
	private LinkedList<String> bboxList=new LinkedList<>();
	private LinkedList<Category> catList=new LinkedList<>();
	
	//Metodi
	
	public LinkedList<String> getBboxList() 
	{
		return bboxList;
	}
	
	public LinkedList<Category> getCatList() 
	{
		return catList;
	}

	public ImageCategory(String i)
	{
		id_image=i;
	}
	
	public String getIdImage() {
		return id_image;
	}

	public boolean addCategory(Category c, String bbox) 
	{
		catList.add(c);
		bboxList.add(bbox);
		return true;
	}
	
	

	
}
