package persistent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;

import entities.Constants;

public abstract class LuceneCommon 
{
	protected IndexSearcher indexSearcher;
	protected StandardAnalyzer analyzer;
	
	protected Path indexDirectoryPath=Paths.get(Constants.getINDEX());
	
	/**
     * Questo metodo si occupa di filtrare i risultati e rimuovere i doppioni lasciando solo quello nella posizione più alta
     * @param resultOld
     * @return String[][] result
     * @throws IOException
     * @throws ParseException
     */
	protected String[][] searchFilter(String[][] resultOld) throws IOException, ParseException
    {
    	//Recupero i singli array
    	String[] resultID=resultOld[0];
		String[] resultSentence=resultOld[1];
		String[] resultImageID=resultOld[2];
    	
		//preparo delle liste per eseguire la rimozione dei doppioni
    	LinkedList<String> resultIDList=new LinkedList<>();
    	LinkedList<String> resultImageIDList=new LinkedList<>();
    	LinkedList<String> resultSentenceList=new LinkedList<>();
    	
    	int i;
    	for (i=0;i<resultID.length;i++) //Ciclo sui risultati ed elimino i doppioni
    	{
    		String imageid=resultImageID[i];
    		boolean present=false;
    		
    		int j;
    		for (j=0;j<resultImageIDList.size();j++)
    		{
    			if (imageid.equals(resultImageIDList.get(j))) //Doppione!
    			{
    				present=true;
    				//Inserisco anche la seconda caption nel risultato
    				String oldSentence=resultSentenceList.get(j);
    				String newSentence=oldSentence+"#"+resultSentence[i];
    				resultSentenceList.remove(j);
    				resultSentenceList.add(j, newSentence);
    				//Inserisco anche il secondo id nel risultato
    				String oldID=resultIDList.get(j);
    				String newID=oldID+"#"+resultID[i];
    				resultIDList.remove(j);
    				resultIDList.add(j, newID);
    				break;
    			}
    		}
    		if (!present) //Se non è un doppione lo aggiungo
    		{
    			resultImageIDList.add(imageid);
    			resultIDList.add(resultID[i]);
    			resultSentenceList.add(resultSentence[i]);
    		}
    	}
    	
    	//Ricreo gli array 
    	String[] newresultID= new String[resultIDList.size()];
    	String[] newresultSentence= new String[resultIDList.size()];
    	String[] newresultImageID= new String[resultIDList.size()];
    	
    	for (i=0;i<resultIDList.size();i++)
    	{
    		newresultID[i]= resultIDList.get(i);
    		newresultSentence[i]= resultSentenceList.get(i);
    		newresultImageID[i]= resultImageIDList.get(i);
    	}

    	  String[][] result={newresultID,newresultSentence,newresultImageID,resultOld[3],resultOld[4]};
    	  return result;
    }

}
