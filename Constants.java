package entities;

public abstract class Constants 
{
	private static String GOLD_FILE="D:\\workspace_eclipse\\CoCoRetrieval\\gold_standard.txt";
	private static String GOLD_FILE_TEMP="D:\\workspace_eclipse\\CoCoRetrieval\\gold_standard_temp.txt";
	private static String IMAGE_LOCATION="D:\\workspace_eclipse\\CoCoRetrieval\\WebContent\\images\\val2014\\";
	private static String TUMB_LOCATION="D:\\workspace_eclipse\\CoCoRetrieval\\WebContent\\images\\thumbnails\\";
	private static String DATADIRECTORYPATH="D:\\workspace_eclipse\\CoCoRetrieval\\Data";
	
	private static String[] COLOR={"FF0000","FF8000","FFFF000","00FF00","00FFFF","0000FF","FF00FF","660000","006600","FFFFFF"}; 
	private static double RATIO=0.4; //Dimensione delle thumbnail in rapporto all'originale
	private static String UNDERCATWEIGHT="^0.1 "; //Peso da dare alle categorie dove presente la super categoria
	
	
	public static String getUNDERCATWEIGHT() {
		return UNDERCATWEIGHT;
	}
	public static String getGOLD_FILE() {
		return GOLD_FILE;
	}
	public static String getGOLD_FILE_TEMP() {
		return GOLD_FILE_TEMP;
	}
	public static String getIMAGE_LOCATION() {
		return IMAGE_LOCATION;
	}
	public static String getTUMB_LOCATION() {
		return TUMB_LOCATION;
	}
	public static String getDATADIRECTORYPATH() {
		return DATADIRECTORYPATH;
	}
	public static String[] getCOLOR() {
		return COLOR;
	}
	public static double getRATIO() {
		return RATIO;
	}

	
}
