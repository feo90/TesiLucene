package control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import entities.Category;
import entities.Constants;

public abstract class RankingControl 
{
	private static String dataDirectoryPath=Constants.getDATADIRECTORYPATH();
	
	/**
	 * Questo metodo si occupa di riordinare i risultati secondo dei criteri prefissati
	 * @param result
	 * @param query
	 * @return String[][] i risultati riordinati
	 * @throws IOException
	 */
	public static String[][] rank(String[][] result, String query) throws IOException
	{
		System.out.println("PRINTI DI CONTROLLO: Sono in rank con query: "+query+" e "+result[0].length+" risultati");
		
		float[] weights=new float[result[0].length]; //l'array dei pesi
		
		//inserisco i pesi iniziali
		int i;
		for (i=0;i<result.length;i++)
		{
			float w=i+1;
			weights[i]=1/w;
		}
		
		//recupero le categorie
		LinkedList<Category> categoryList= loadCategory();
		LinkedList<String> present_category= new LinkedList<>();
		LinkedList<String> present_undercategory= new LinkedList<>();
			
		//Verifico quali categorie sono presenti nella query
		for (i=0;i<categoryList.size();i++)
		{
			//System.out.println("PRINT DI CONTROLLO: Esamino la categoria: "+categoryList.get(i).getName());
			if (query.contains(categoryList.get(i).getName()))
			{
				System.out.println("PRINT DI CONTROLLO: La categoria: "+categoryList.get(i).getName()+" è presente nella query");
				present_category.add(categoryList.get(i).getName());
			}
			else if (query.contains(categoryList.get(i).getSupercategory()))
			{
				System.out.println("PRINT DI CONTROLLO: La supercategoria: "+categoryList.get(i).getSupercategory()+" è presente nella query, aggiungo quindi la categoria "+categoryList.get(i).getName());
				present_undercategory.add(categoryList.get(i).getName());
			}
		}
		
		//Calcolo i pesi sulle captions
		int caption_mode=0; //quale metodo usare, è una variabile
		float[] capt_weights= findCaptionsWeights(result,present_category,present_undercategory,caption_mode);
		
		int object_mode=0; //quale metodo usare, è una variabile
		//Calcolo i pesi sugli oggetti
		float[] obj_weights= findObjectWeights(result,present_category,present_undercategory,object_mode);
		
		for (i=0;i<weights.length;i++)
		{
			//System.out.println("\nPRINT DI CONTROLLO: Il peso originale dell'immagine "+i+" è "+weights[i]+" quello dovuto alle captions è "+capt_weights[i]+" quello dovuto alle categorie è "+obj_weights[i]);
			weights[i]=weights[i]+capt_weights[i]+obj_weights[i];
			//System.out.println("PRINT DI CONTROLLO: Il nuovo peso dell'immagine in posizione "+i+" è pari a "+weights[i]+"\n");
		}
		
		//Riordino i risultati 
		String [][] new_order=sort(result, weights);
		float[] ordered_weights=sortWeights(weights);
		
		//Scarto i pesi inferiori a zero
		String [][] positive_order=filterUnderZero(new_order, ordered_weights);
		
		for (i=0;i<positive_order[1].length;i++)
		{
			System.out.println("PRINT DI CONTROLLO: Il peso definitivo dell'immagine "+positive_order[0][i]+" in posizione "+i+" è pari a "+ordered_weights[i]);
		}
		
		return positive_order;
	}


	/**
	 * Questo metodo si occupa di riordinare l'array dei pesi in maniera decrescente 
	 * @param weights float[]
	 * @return float[] l'array riordinato 
	 */
	private static float[] sortWeights(float[] weights) 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in sortWeights");
		boolean sorted=false;
		while (!sorted)
		{
			int i;
			sorted=true;
			for (i=0;i<weights.length-1;i++)
			{
				if (weights[i]<weights[i+1]) //se il peso del secondo è più grande li scambio
				{
					//System.out.println("PRINT DI CONTROLLO: Ho scambiato gli elementi "+i+" e "+(i+1));
					sorted=false; //Sto ancora ordinando
					
					float first_w=weights[i];
					float second_w=weights[i+1];	
					weights[i]=second_w;
					weights[i+1]=first_w;	
				}
			}
			//System.out.println("PRINT DI CONTROLLO: Ho terminato un ciclo, sorted è: "+sorted);
		}
		return weights;
	}

	/**
	 * Questo metodo elimina dai risultati quelli con peso negativo
	 * @param result
	 * @param weights
	 * @return String[][] il nuovo result
	 */
	private static String[][] filterUnderZero(String[][] result, float[] weights) 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in filterZero");
		//Controllo i pesi partendo dal basso per trovare dove iniziano i positivi
		int i;
		int minimum_positive=weights.length-1;
		for (i=weights.length-1;i>-1;i--)
		{
			//System.out.println("PRINT DI CONTROLLO: Esamino il valore "+i+" che ha come peso "+weights[i]);
			if (weights[i]>0)
			{
				 minimum_positive=i;
				 //System.out.println("PRINT DI CONTROLLO: Il minimo valore con peso positivo è "+i);
				 break;
			}
		}
		String [][] filter_result=new String[result.length][minimum_positive+1];
		
		int k;
		for (k=0;k<result.length;k++) //eseguo per tutti le liste 
		{
			//copio solo i risultati con peso positivo
			int j;
			for (j=0;j<minimum_positive+1;j++)
			{
				filter_result[k][j]=result[k][j];
				//System.out.println("PRINT DI CONTROLLO: Al passo j="+j+" inserisco l'elemento "+filter_result[k][j]);
			}
		}
		return filter_result;
	}


	/**
	 * Questo metodo riordina result con bubble sort in ordine descrescente secondo il peso
	 * @param result String[][] i risultati da riordinare
	 * @param weights float[] i pesi associati ai vari risultati
	 * @return String[][] result
	 */
	private static String[][] sort(String[][] result, float[] weights) 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in sort");
		boolean sorted=false;
		while (!sorted)
		{
			int i;
			sorted=true;
			for (i=0;i<weights.length-1;i++)
			{
				if (weights[i]<weights[i+1]) //se il peso del secondo è più grande li scambio
				{
					//System.out.println("PRINT DI CONTROLLO: Ho scambiato gli elementi "+i+" e "+(i+1));
					sorted=false; //Sto ancora ordinando
					
					int k;
					for (k=0;k<result.length;k++) //scambio per tutti le liste 
					{
						String first=result[k][i];
						String second=result[k][i+1];					
						result[k][i]=second;
						result[k][i+1]=first;
					}
					
					float first_w=weights[i];
					float second_w=weights[i+1];	
					weights[i]=second_w;
					weights[i+1]=first_w;	
				}
			}
			//System.out.println("PRINT DI CONTROLLO: Ho terminato un ciclo, sorted è: "+sorted);
		}
		return result;
	}


	/**
	 * Questo metodo si occupa di valutare il peso aggiuntivo da dare ad un'immagine tenendo conto delle categorie dei suoi oggetti
	 * @param result
	 * @param present_undercategory
	 * @param present_category
	 * @param caption_mode
	 * @return
	 */
	private static float[] findObjectWeights(String[][] result, LinkedList<String> present_category, LinkedList<String> present_undercategory, int caption_mode) 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in findObjectWeights");
		float[] weights= new float[result[0].length];
		String[] resultCategories=result[3];
		
		
		int i;
		for (i=0;i<weights.length;i++)
		{
			//System.out.println("PRINT DI CONTROLLO: Sono al passo "+i+" e le categories sono "+resultCategories[i]);
			String cat=resultCategories[i].substring(0,resultCategories[i].length()-2); //elimino l'ultimo " #"
			String[] categories=cat.split(" # ");
			
			float weight=0;		
			int j;
			for (j=0;j<present_category.size();j++) //Ciclo su tutte le categorie presenti nella query
			{
				boolean present=false;
				//System.out.println("PRINT DI CONTROLLO: Esamino la categoria: "+present_category.get(j));
				
				int k;
				for (k=0;k<categories.length;k++)
				{
					//System.out.println("PRINT DI CONTROLLO: La confronto con: "+categories[k]);
					if (categories[k].equals(present_category.get(j))) 
					{
						//System.out.println("PRINT DI CONTROLLO: la categoria è presente nell'immagine!");
							
						weight=(float) (weight+0.4); //aumento il peso di 0.4
						present=true;
						break;
					}
				}
					
				if (!present) //la categoria non è presente nell'immagine!
				{
					weight=(float) (weight-0.8); //diminuisco il peso di 0.8
				}
			}
			//System.out.println("PRINT DI CONTROLLO: Dopo aver esaminato le categorie il peso è "+weight);
			
			//Verifico le sottocategorie
			for (j=0;j<present_undercategory.size();j++) //Ciclo su tutte le categorie presenti nella query
			{
				//System.out.println("PRINT DI CONTROLLO: Esamino la sottocategoria: "+present_undercategory.get(j));
				int k;
				for (k=0;k<categories.length;k++)
				{
					//System.out.println("PRINT DI CONTROLLO: La confronto con: "+categories[k]);
					if (categories[k].equals(present_undercategory.get(j))) 
					{
						//System.out.println("PRINT DI CONTROLLO: la categoria è presente nell'immagine!");
							
						weight=(float) (weight+0.2); //aumento il peso di 0.2
						break;
					}
				}	
			}
			//System.out.println("PRINT DI CONTROLLO: Dopo aver esaminato le sottocategorie il peso è "+weight);
			weights[i]=weight;
		}
		return weights;
	}


	/**
	 * Questo metodo si occupa di valutare il peso aggiuntivo da dare ad un'immagine tenendo conto delle sue captions
	 * @param result
	 * @param present_undercategory
	 * @param present_category
	 * @param caption_mode
	 * @return
	 */
	private static float[] findCaptionsWeights(String[][] result, LinkedList<String> present_category, LinkedList<String> present_undercategory, int caption_mode) 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in findCaptionsWeights");
		float[] weights= new float[result[0].length];
		String[] resultSentence=result[1];
		
		int i;
		for (i=0;i<weights.length;i++)
		{
			//System.out.println("PRINT DI CONTROLLO: Sono al passo "+i+" e la captions è "+resultSentence[i]);
			String captions=resultSentence[i]; // le singole captions sono separate da un /n
			
			float weight=0;		
			int j;
			for (j=0;j<present_category.size();j++) //Ciclo su tutte le categorie presenti nella query
			{
				boolean present=false;
				//System.out.println("PRINT DI CONTROLLO: Esamino la categoria: "+present_category.get(j));
				if (captions.contains(present_category.get(j))) 
				{
					//System.out.println("PRINT DI CONTROLLO: la categoria è presente tra le caption!");
						
					weight=(float) (weight+0.4); //aumento il peso di 0.4
					present=true;
				}
				if (!present) //la categoria non è presente nelle captions!
				{
					weight=(float) (weight-0.8); //diminuisco il peso di 0.8
				}
			}
			//System.out.println("PRINT DI CONTROLLO: Dopo aver esaminato le categorie il peso è "+weight);
			
			//Verifico le sottocategorie
			for (j=0;j<present_undercategory.size();j++) //Ciclo su tutte le categorie presenti nella query
			{
				//System.out.println("PRINT DI CONTROLLO: Esamino la sottocategoria: "+present_undercategory.get(j));
				if (captions.contains(present_undercategory.get(j))) 
				{
					//System.out.println("PRINT DI CONTROLLO: la sottocategoria è presente tra le caption!");
						
					weight=(float) (weight+0.2); //aumento il peso di 0.2
				}	
			}
			//System.out.println("PRINT DI CONTROLLO: Dopo aver esaminato le sottocategorie il peso è "+weight);
			weights[i]=weight;
		}
		
		return weights;
	}


	/**
	 * Questo metodo si occupa di caricare le categorie degli oggetti presenti nelle immagini
	 * @return LinkedList<Category> 
	 * @throws IOException
	 */
	private static LinkedList<Category> loadCategory() throws IOException
    {
    	System.out.println("Sono in loadCategory");
    	
    	File[] files = new File(dataDirectoryPath).listFiles(); //get all files in the data directory
    	LinkedList<Category> categoryList=new LinkedList<Category>();
	      
	    if (files!=null)
	    {
	    	//Scorro tutti i file e recupero i dati 
	    	for (File file : files) 
	    	{
	    		//System.out.println("Esamino il file: "+file.getName());   
	    		if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && fileFilterInstances(file))
	    		{
	    			//System.out.println("Il file: "+file.getName()+" è un file di instances");
	    			
	    			String fileContent=readFile(file.getPath());  //estrapolo i dati dal file
	    			
	    			String[] filepart=fileContent.split("\"annotations\": \\["); //Separo le info sulle immagini, da scartare, dal resto del file
	    			filepart[1]=filepart[1].substring(1, filepart[1].length()); //rimuovo la {
	    			String[] filedata=filepart[1].split("], \"categories\": \\["); //Separo la prima parte che contiene i dati delle immagini dalla seconda che contiene le categorie
	    			filedata[1]=filedata[1].substring(1, filedata[1].length()); //rimuovo la {
	    			
	    			//LAVORO SULLE CATEGORIE 
	    			filedata[1]=filedata[1].substring(0, filedata[1].length()-3); //Rimuovo il }]} finale
	    			String[] categories=filedata[1].split("}, \\{"); //Isolo le singole categorie nella forma: "supercategory": "vehicle", "id": 2, "name": "bicycle"
	    			
	    			int j;
	    			for (j=0;j<categories.length;j++)
	    			{
	    				String category=categories[j].substring(1, categories[j].length()-1); //elimino i " ad inizio e fine
	    				String[] elements=category.split("\": \"|\", \"|, \"n|d\": ");//splitto su <": "> E <", "> E <, "n> E <d": > in questo modo name diventa ame ed id solo i ma separo i singoli elementi
	    				
	    				//System.out.println("Ho trovato la categoria con id: "+elements[3]+" nome: "+ elements[5]+" e supercategoria: "+elements[1]);
	    				Category c=new Category(elements[3],elements[5],elements[1]);
	    				categoryList.add(c);
	    			}    			
	    		}   
	    	}
	    }	
	    return categoryList;
    }
	
	
	/**
	 * Questo metodo si occupa di leggere il file contenente i dati
	 * @param fileName
	 * @return String
	 * @throws IOException
	 */
	private static String readFile(String fileName) throws IOException 
	{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) 
	        {
	        	sb.append(line);
	            line = br.readLine();
	        }      
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}

	
	/**
	 * Questo metodo restituisce true se il file è un .json ed inizia con instances
	 * 
	 * @param pathname il percorso del File
	 * @return boolean
	 */
	private static boolean fileFilterInstances (File pathname)
	{
		return pathname.getName().toLowerCase().endsWith(".json") && pathname.getName().toLowerCase().startsWith("instances");
	}
}
