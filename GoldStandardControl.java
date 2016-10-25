package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import entities.Constants;

public abstract class GoldStandardControl 
{
	private static String GOLD_FILE=Constants.getGOLD_FILE();
	private static String GOLD_FILE_TEMP=Constants.getGOLD_FILE_TEMP();
	
	
	/**
	 * Questo metodo prende in input l'indirizzo di un file e restituisce una linked list che ne contiene le singole righe
	 * @param file il filename 
	 * @return LinkedList<String>
	 * @throws IOException 
	 */
	private static LinkedList<String> parseLn(String file) throws IOException
    {
		//System.out.println("Sono in parseLn del file: "+file);
		LinkedList<String> queries=new LinkedList<>();
        
        BufferedReader br = new BufferedReader(new FileReader(file));
	    try 
	    {
	        String line = br.readLine();

	        while (line != null) 
	        {
	        	queries.add(line);    	
	            line = br.readLine();
	        }
	    } 
	    finally 
	    {
	        br.close();
	    }
        return queries;
    }
	
	/**
	 * Questo metodo recupera, se esiste, il gold standard per la query indicata 
	 * @param query la query cercata
	 * @return String[] gli id delle immagini nell'ordine del gold standard, null se non esiste
	 */
	public static String[] findGoldStandard(String query)
	{
		System.out.println("Sono in findGoldStandard con query: "+query);
		LinkedList<String> gold_standard_ln = null;
		
		try {
			gold_standard_ln=parseLn(GOLD_FILE);
		} 
		catch (IOException e) 
		{
			System.out.println("ERRORE: Parser fallito sul file: "+GOLD_FILE);
			e.printStackTrace();
			return null;
		}
		
		//Esamino le singole query
		int i;
		for (i=0;i<gold_standard_ln.size(); i++)
		{
			//System.out.println("Esamino la riga: "+gold_standard_ln.get(i));
			String[] elements=gold_standard_ln.get(i).split("#");
			String[] gold_standard=new String[elements.length-1];
			if (query.equals(elements[0])) //la query è il primo elemento della riga
			{
				System.out.println(gold_standard_ln.get(i)+" è il gold standard cercato");
				//Riempio l'array
				int j;
				for (j=0;j<elements.length-1;j++)
				{
					gold_standard[j]=elements[j+1];
				}
				return gold_standard; 
			}
		}
		System.out.println("Non esiste un gold standard per questa query: "+query);
		return null;
	}
	
	/**
	 * Questo metodo restituisce tutte le query per le quali esiste un gold standard
	 * @return LinkedList<String>
	 */
	public static LinkedList<String> findAllGoldStandards()
	{
		LinkedList<String> gold_standard_ln = null;
		LinkedList<String> gs_list=new LinkedList<>();
		
		try {
			gold_standard_ln=parseLn(GOLD_FILE);
		} 
		catch (IOException e) 
		{
			System.out.println("ERRORE: Parser fallito sul file: "+GOLD_FILE);
			e.printStackTrace();
			return null;
		}
		
		//Esamino le singole query
		int i;
		for (i=0;i<gold_standard_ln.size(); i++)
		{
			//System.out.println("Esamino la riga: "+gold_standard_ln.get(i));
			String[] elements=gold_standard_ln.get(i).split("#");
			gs_list.add(elements[0]); //la query è il primo elemento della riga
		}
		return gs_list;
	}

	/**
	 * Questo metodo permette di salvare un nuovo gold standard
	 * @param LinkedList<String> relevant la lista degli elementi rilevanti
	 * @param LinkedList<String> relevant la lista degli elementi irrilevanti
	 * @param String query la query considerata
	 * @throws IOException
	 */
	public static void saveGoldStandard(LinkedList<String> relevant, LinkedList<String> irrelevant, String query) throws IOException
	{
		System.out.println("Sono in saveGoldStandard della query: "+query);
		 
		String gs_string=query;
		String[] old_gs=findGoldStandard(query);
		
		if (old_gs==null)
		{
			int i;
			for (i=0;i<relevant.size();i++)
			{
				gs_string=gs_string+"#"+relevant.get(i);
			}			
			saveNewGoldStandard(gs_string);
		}
		else
		{
			String[] gold_standard=refineGoldStandard(relevant, irrelevant, old_gs);
			int i;
			for (i=0;i<gold_standard.length;i++)
			{
				gs_string=gs_string+"#"+gold_standard[i];
			}			
			updateGoldStandard(gs_string);
		}
	}
	
	/**
	 * Questo metodo permette di inserire un gold standard non ancora presente sul file
	 * @param String Il nuovo gold standard nel formato query#im1#im2....
	 * @throws IOException 
	 * 
	 */
	public static void saveNewGoldStandard(String newsg) throws IOException
	{
		//System.out.println("Sono in saveNewGoldStandard della query: "+newsg);
 
        BufferedWriter bw = new BufferedWriter(new FileWriter(GOLD_FILE, true));
        PrintWriter pw= new PrintWriter(bw);
        pw.println(newsg);
        pw.close();
	}
	
	/**
	 * Questo metodo si occupa di aggiornare un gold standard già presente sul file
	 * @param newgs String Il nuovo gold standard nel formato query#im1#im2....
	 * @throws IOException
	 */
	public static void updateGoldStandard(String newgs) throws IOException
	{
		//System.out.println("Sono in updateGoldStandard della query: "+newgs);

		File tempFile = new File(GOLD_FILE_TEMP);
		File goldFile = new File(GOLD_FILE);
        
		BufferedReader br = new BufferedReader(new FileReader(goldFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        PrintWriter pw= new PrintWriter(bw);
        
        String currentline;
        String[] el=newgs.split("#");
        String query=el[0];
        
        while ((currentline=br.readLine()) !=null)
        {
        	String[] elements=currentline.split("#");
        	//System.out.println("esamino la query: "+elements[0]);
        	
        	if (elements[0].equals(query)) //E' quella da aggiornare
        	{
        		//System.out.println("E' quella da aggiornare");
        		pw.println(newgs);
        	}
        	else
        	{
        		pw.println(currentline);
        	}
        }
        pw.close();
        br.close();
        bw.close();
        boolean del=goldFile.delete();
        boolean ren=tempFile.renameTo(goldFile);
        
        if (!del || !ren)
        {
        	System.out.println("ERROR: failure in update gold standard: delete return "+del+" rename return: "+ren);
        }
        //System.out.println("Il risultato del delete è "+del+" ed il risultato del rename è: "+ren);
	}
	
	/**
	 * Questo metodo si occupa di trovare il nuovo gold standard partendo dal vecchio ed aggiungendo/rimuovendo elementi 
	 * @param relevant nuovi elementi rilevanti in formato LinkedList<String>
	 * @param irrelevant nuovi elementi irrilevanti in formato LinkedList<String>
	 * @param oldgs il vecchio Gold Standard in formato String[]
	 * @return il nuovo gold standard in formato String[]
	 */
	public static String[] refineGoldStandard(LinkedList<String> relevant, LinkedList<String> irrelevant, String[] oldgs)
	{
		//System.out.println("Sono in refineGoldStandard");
		LinkedList<String> new_gs_list=new LinkedList<String>();
		
		//Scorro la vecchia lista ed elimino gli irrilevanti, se ci sono doppioni con i nuovi rilevanti li tolgo
		int i;
		for (i=0;i<oldgs.length;i++)
		{
			String element=oldgs[i];
			int j;
			boolean rel=true;
			//controllo sugli irrilevanti
			for (j=0;j<irrelevant.size();j++)
			{
				if (irrelevant.get(j).equals(element))
				{
					irrelevant.remove(j);
					rel=false;
					break;
				}
			}
			if (rel)
			{
				new_gs_list.add(element);
				//controllo sui rilevanti per evitare doppioni
				for (j=0;j<relevant.size();j++)
				{
					if (relevant.get(j).equals(element))
					{
						relevant.remove(j);
						break;
					}
				}	
			}					
		}
		
		//creo l'array con i vecchi ed i nuovi rilevanti
		String [] newgs= new String[new_gs_list.size()+relevant.size()];
		
		for (i=0;i<new_gs_list.size();i++)
		{
			newgs[i]=new_gs_list.get(i);
		}
		int nwl_size=new_gs_list.size();
		for (i=0;i<relevant.size();i++)
		{
			newgs[i+nwl_size]=relevant.get(i);
		}
		
		return newgs;
	}

	public static void editGoldStandard(boolean[] gs_value, String query) throws IOException 
	{
		//System.out.println("Sono in editGoldStandard della query: "+query);
		String[] old_gs=findGoldStandard(query);
		LinkedList<String> new_gs= new LinkedList<>();
		String gs_string=query;
		
		int i;
		for (i=0;i<old_gs.length;i++)
		{
			if (gs_value[i])
			{
				new_gs.add(old_gs[i]);
			}
		}
		for (i=0;i<new_gs.size();i++)
		{
			gs_string=gs_string+"#"+new_gs.get(i);
		}			
		updateGoldStandard(gs_string);
	}

}