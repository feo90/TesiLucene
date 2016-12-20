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
	private static String GOLD_FILE_PART=Constants.getGOLD_FILE_PART();
	

	
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
	 * @param alsoPart boolean, indica se si vogliono considerare anche i risultati parzialmente rilevanti 
	 * @return String[] gli id delle immagini nell'ordine del gold standard, null se non esiste
	 */
	public static String[] findGoldStandard(String query, boolean alsoPart)
	{
		System.out.println("PRINT DI CONTROLLO: Sono in findGoldStandard con query: "+query+" ed also Part vale: "+alsoPart); //TODO
		
		String[] relevant_GS=findSingleGoldStandard(query,true);
		String [] part_GS=findSingleGoldStandard(query,false);
		
		if (alsoPart && part_GS!=null) //Se voglio anche i parziali e questi non sono nulli
		{	
			//unisco gli array
			int doublesize=relevant_GS.length+part_GS.length;
			int relevantsize=relevant_GS.length;
			String[] gold=new String[doublesize];
			int i;
			for (i=0;i<relevantsize;i++)
			{
				gold[i]=relevant_GS[i];
			}
			for(i=0;i<part_GS.length;i++)
			{
				gold[i+relevantsize]=part_GS[i];
			}
			return gold;
		}
		else
		{
			return relevant_GS;
		}
	}
	
	
	/**
	  * Questo metodo recupera, se esiste, il gold standard per la query indicata 
	 * @param query la query cercata
	 * @param relevant indica se si vuole recuperare i rilevanti od i parzialmente rilevanti
	 * @return String[] gli id delle immagini nell'ordine del gold standard, null se non esiste
	 */
	public static String[] findSingleGoldStandard(String query, boolean relevant)
	{
		System.out.println("PRINT DI CONTROLLO: Sono in findSingleGoldStandard con query: "+query+" e relevant "+ relevant); //TODO rimuovilo 
		LinkedList<String> gold_standard_ln = null;
		
		try {
			
			if (relevant)
			{
				gold_standard_ln=parseLn(GOLD_FILE);
			}
			else
			{
				gold_standard_ln=parseLn(GOLD_FILE_PART);
			}
		} 
		catch (IOException e) 
		{
			System.out.println("ERROR: can't parse file: "+GOLD_FILE);
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
				//System.out.println(gold_standard_ln.get(i)+" è il gold standard cercato"); 
				//Riempio l'array
				int j;
				for (j=0;j<elements.length-1;j++)
				{
					gold_standard[j]=elements[j+1];
				}
				return gold_standard; 
			}
		}
		System.out.println("WARNING: doesn't exist a gold standard for the query: "+query); 
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
			System.out.println("ERROR: failed to parse file: "+GOLD_FILE);
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
	 * @param boolean find_relevant indica se voglio aggiornare i rilevanti od i parzialmente rilevanti
	 * @throws IOException
	 */
	public static void saveSingleGoldStandard(LinkedList<String> relevant, LinkedList<String> irrelevant, String query, boolean find_relevant) throws IOException
	{
		System.out.println("PRINT DI CONTROLLO: Sono in saveSingleGoldStandard della query: "+query+" e find_relevant è "+find_relevant); //TODO rimuovilo
		 
		String gs_string=query;
		String[] old_gs=findSingleGoldStandard(query,find_relevant);
		
		if (old_gs==null)
		{
			int i;
			for (i=0;i<relevant.size();i++)
			{
				gs_string=gs_string+"#"+relevant.get(i);
			}			
			saveNewGoldStandard(gs_string, find_relevant);
		}
		else
		{
			String[] gold_standard=refineGoldStandard(relevant, irrelevant, old_gs);
			int i;
			for (i=0;i<gold_standard.length;i++)
			{
				gs_string=gs_string+"#"+gold_standard[i];
			}			
			updateSingleGoldStandard(gs_string, find_relevant);
		}
	}
	
	/**
	 * Questo metodo permette di inserire un gold standard non ancora presente sul file
	 * @param String Il nuovo gold standard nel formato query#im1#im2....
	 * @param boolean save_relevant se il gold standard è dei rilevanti o dei parzialmente rilevanti
	 * @throws IOException 
	 * 
	 */
	public static void saveNewGoldStandard(String newsg, boolean save_relevant) throws IOException
	{
		//System.out.println("Sono in saveNewGoldStandard della query: "+newsg);
		String file="";
		if (save_relevant)
		{
			file=GOLD_FILE;
		}
		else
		{
			file=GOLD_FILE_PART;
		}
		
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        PrintWriter pw= new PrintWriter(bw);
        pw.println(newsg);
        pw.close();
	}
	
	/**
	 * Questo metodo si occupa di aggiornare un gold standard già presente sul file
	 * @param newgs String Il nuovo gold standard nel formato query#im1#im2....
	 * @param save_relevant boolean se il gold standard è rilevante o parzialmente rilevante
	 * @throws IOException
	 */
	public static void updateSingleGoldStandard(String newgs, boolean save_relevant) throws IOException
	{
		//System.out.println("Sono in updateGoldStandard della query: "+newgs);

		String file="";
		if (save_relevant)
		{
			file=GOLD_FILE;
		}
		else
		{
			file=GOLD_FILE_PART;
		}
		
		File tempFile = new File(GOLD_FILE_TEMP);
		File goldFile = new File(file);
        
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
		
		//Scorro la vecchia lista per eliminare eventuali doppioni presenti dovuti a modifiche manuali
		//System.out.println("PRINT DI CONTROLLO: Esamino i rilevanti"); 
		for (i=0;i<new_gs_list.size();i++)
		{
			int j;
			//System.out.println("PRINT DI CONTROLLO: Esamino l'elemento: "+new_gs_list.get(i)); 	
			for (j=i+1;j<new_gs_list.size();j++)
			{
				if (new_gs_list.get(i).equals(new_gs_list.get(j)))
				{
					//System.out.println("PRINT DI CONTROLLO: al posto "+j+" l'elemento: "+new_gs_list.get(j)+" è uguale e lo rimuovo"); 
					new_gs_list.remove(j);
					j--;
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

	/**
	 * Questo metodo si occupa di aggiornare il gs 
	 * @param gs_value il nuovo valore dei vecchi rilevanti
	 * @param gs_value_part il nuovo valore dei vecchi parzialmente rilevanti
	 * @param query la query in formato String
	 */
	public static void editGoldStandard(String[] gs_value_rel, String[] gs_value_part, String query) 
	{
		String[] old_gs_rel=findSingleGoldStandard(query,true);
		String[] old_gs_part=findSingleGoldStandard(query,false);
		LinkedList<String> relevant=new LinkedList<>();
		LinkedList<String> partially=new LinkedList<>();
		LinkedList<String> irrelevant=new LinkedList<>();
		
		int i;
		String[] old_gs=null;
		String[] gs_value=null;
		
		//Elaboro i vecchi rilevanti
		if (old_gs_rel!=null)
		{
			old_gs=old_gs_rel;
			gs_value=gs_value_rel;
			for (i=0;i<old_gs.length;i++)
			{
				if (gs_value[i].equals("relevant"))
				{
					relevant.add(old_gs[i]);
				}
				else if (gs_value[i].equals("irrelevant"))
				{
					irrelevant.add(old_gs[i]);
				}
				else if (gs_value[i].equals("partially"))
				{
					partially.add(old_gs[i]);
				}
				else
				{
					System.out.println("ERROR: for element: "+old_gs[i]+" value is: "+gs_value[i]);
				}
			}
		}
		
		//Elaboro i vecchi parzialmente rilevanti
		if (old_gs_part!=null)
		{
			old_gs=old_gs_part;
			gs_value=gs_value_part;		
			for (i=0;i<old_gs.length;i++)
			{
				if (gs_value[i].equals("relevant"))
				{
					relevant.add(old_gs[i]);
				}
				else if (gs_value[i].equals("irrelevant"))
				{
					irrelevant.add(old_gs[i]);
				}
				else if (gs_value[i].equals("partially"))
				{
					partially.add(old_gs[i]);
				}
				else
				{
					System.out.println("ERROR: for element: "+old_gs[i]+" value is: "+gs_value[i]);
				}
			}
		}
		
		//Elimino eventuali doppioni dagli irrilevanti
		for (i=0;i<irrelevant.size();i++)
		{
			String element=irrelevant.get(i);
			int j;
			boolean removed=false;
			for (j=0;j<partially.size();j++)
			{
				if (element.equals(partially.get(j)))
				{
					irrelevant.remove(i);
					i--;
					removed=true;
					break;
				}
			}
			if (!removed) //Se l'ho già rimosso non serve che controllo sui rilevanti
			{
				for (j=0;j<relevant.size();j++)
				{
					if (element.equals(partially.get(j)))
					{
						irrelevant.remove(i);
						i--;
						break;
					}
				}
			}
		}
		//Elimino eventuali doppioni dai parzialmente rilevanti
		for (i=0;i<partially.size();i++)
		{
			String element=partially.get(i);
			int j;
			for (j=0;j<relevant.size();j++)
			{
				if (element.equals(relevant.get(j)))
				{
					partially.remove(i);
					i--;
					break;
				}
			}	
		}
		
		//Elimino eventuali ripetizioni dalle liste da salvare
		for (i=0;i<relevant.size();i++)
		{
			int j;
			for (j=i+1;j<relevant.size();j++)
			{
				if (relevant.get(i).equals(relevant.get(j)))
				{
					relevant.remove(j);
					j--;
				}
			}
		}
		for (i=0;i<partially.size();i++)
		{
			int j;
			for (j=i+1;j<partially.size();j++)
			{
				if (partially.get(i).equals(partially.get(j)))
				{
					partially.remove(j);
					j--;
				}
			}
		}
		
		//Salvo di dati
		try 
		{
			saveSingleGoldStandard(relevant,irrelevant,query,true);
			saveSingleGoldStandard(partially,irrelevant,query,false);
		} catch (IOException e) 
		{
			System.out.println("ERROR: failed to save the gold standard for the query: "+query);
			e.printStackTrace();
		}
	}
}