package entities;

public class Category 
{
	private String id;
	private String name;
	private String supercategory;
	
	/**
	 * Costruttore
	 * @param i: id
	 * @param n: nome della categoria
	 * @param s: supercategoria di riferimento
	 */
	public Category(String i, String n, String s)
	{
		id=i;
		name=n;
		supercategory=s;
	}
	
	public boolean isSuper()
	{
		if (name.equals(supercategory))
		{
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getSupercategory() {
		return supercategory;
	}


}
