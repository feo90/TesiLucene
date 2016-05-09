package lucene;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneController 
{

	private IndexSearcher indexSearcher;
	private StandardAnalyzer analyzer;
	private Path indexDirectoryPath=Paths.get("C:\\Users\\Federico\\workspace eclipse luna\\JSPLuceneExample\\Index");
	private String dataDirectoryPath="C:\\Users\\Federico\\workspace eclipse luna\\JSPLuceneExample\\Data";
	
	private LinkedList<String> image_id = new LinkedList<>();
	private LinkedList<String> id = new LinkedList<>();
	private LinkedList<String> caption = new LinkedList<>();
	
	/**
	 * Costruttore con index
	 * @throws IOException 
	 */
	public LuceneController() throws IOException
	{
		// 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        this.analyzer = new StandardAnalyzer();

        // 1. create the index
        boolean exist=false;
        Directory index = FSDirectory.open(indexDirectoryPath); //Creo un index nel file system
        
        if (DirectoryReader.indexExists(index)) //Lo riempio solo se non esiste già
        {
        	 exist=true;
        }
  
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        if (!exist)
        {
        	FillIndex(index, config); //Riempio l'indice
        }
          
        IndexReader reader = DirectoryReader.open(index);
        this.indexSearcher = new IndexSearcher(reader);
	}
	
	/**
	 * Questo metodo si occupa di riempire l'index 
	 * la struttura dei file json è questa qui http://mscoco.org/dataset/#download
	 * per i test posso creare dei file qui http://www.jsoneditoronline.org/
	 * @param index
	 * @param config
	 * @throws IOException
	 */
	private void FillIndex(Directory index, IndexWriterConfig config) throws IOException 
	{
		IndexWriter w = new IndexWriter(index, config);
	    File[] files = new File(dataDirectoryPath).listFiles(); //get all files in the data directory
	      
	    //Scorro tutti i file e recupero i dati 
	    for (File file : files) 
	    {
	         if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && fileFilter(file))
	         {
	        	 ParseJSON(file.getPath()); //estrapolo i dati dal file
	         }
	      }
	    
	    int i;
	    for (i=0;i<id.size();i++)
	    {
	    	addDoc(w,image_id.get(i),id.get(i),caption.get(i)); //aggiungo i dati all'indice
	    }   
        w.close();	
	}
	
	/**
	 * Questo metodo restituisce true se il file è un .json 
	 * 
	 * @param pathname il percorso del File
	 * @return boolean
	 */
	private boolean fileFilter (File pathname)
	{
		return pathname.getName().toLowerCase().endsWith(".json");
	}
	
	/**
	 * Questo metodo recupera l'id, l'image_id e la caption dal file
	 * @param file l'url del  file da esaminare
	 * @return String[] image_id, caption
	 * @throws IOException
	 */
	private void ParseJSON(String file) throws IOException 
	{
		System.out.println("Sono in ParseJSON");
		
		String fileContent=readFile(file);
		String[] filepart=fileContent.split("],"); //Separo le categorie presenti nel file
		
		int i;
		for (i=0;i<filepart.length;i++) //LAVORO RIGA PER RIGA
		{
			String categoria=filepart[i];
			if (categoria.startsWith("\"annotations\""))
			{
				categoria=categoria.substring(14); //elimino l'intestazione: "annotations":
		
				String[] elementi=categoria.split("}"); //isolo i vari elementi
					
				int j;
				for (j=0;j<elementi.length;j++) //LAVORO ESAMINANDO UN ELEMENTO ALLA VOLTA
				{	
					if (elementi[j].length()<4) //Se è così corto non è un elemento valido
					{
						continue;
					}
					
					String elemento=elementi[j].substring(3); //elimino ", {"
					
					String[] dati=elemento.split(", \"|: "); //separo i vari campi 
					
					int k;
					for (k=0;k<dati.length;k++)
					{
						if (dati[k].equals("\"image_id\""))//Il dato vero e proprio è nel successivo
						{
							System.out.println("Ho trovato l'image_id: "+dati[k+1]);
							image_id.add(dati[k+1]);
							k++;
						}
						else if (dati[k].equals("id\""))
						{
							System.out.println("Ho trovato l'id: "+dati[k+1]);
							id.add(dati[k+1]);
							k++;
						}
						else if (dati[k].equals("caption\""))
						{
							System.out.println("Ho trovato la caption: "+dati[k+1]);
							caption.add(dati[k+1]);
							k++;
						}
						else
						{
							System.out.println("Elemento: "+dati[k]+" non riconosciuto, sono in posizione "+k);
						}
					}	
				}
			}
		}
		System.out.println("Ho finito ParseJSON");
	}
	
	/**
	 * Questo metodo si occupa di leggere il file contenente i dati
	 * @param fileName
	 * @return String
	 * @throws IOException
	 */
	private String readFile(String fileName) throws IOException 
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
	 * Questo metodo crea un document lucene e lo inserisce nell'indice
	 * @param w l'IndexWriter
	 * @param image_id l'id dell'immagine
	 * @param id l'id della caption
	 * @param caption la caption stessa come stringa
	 * @throws IOException
	 */
    private void addDoc(IndexWriter w,String image_id,String id, String caption ) throws IOException 
    {
    	Document doc = new Document(); //Lucene Document
        doc.add(new TextField("caption", caption, Field.Store.YES));

        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("id", id, Field.Store.YES));
        
        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("image_id", image_id, Field.Store.YES));
        w.addDocument(doc);
        System.out.println("Sono in addDoc ed ho aggiunto il documento con id "+id+" image_id "+image_id+" e caption "+caption);
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sull'indice
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] search(String querystr) throws IOException, ParseException
    {
    	System.out.println("Sono in search");
    	// the "caption" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("caption", analyzer).parse(querystr); //Crea una query Lucene

        // 3. search
        int hitsPerPage = 50; //TODO Rendilo variabile
        TopDocs docs = indexSearcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        String[] resultID= new String[hits.length];
        String[] resultImage_ID= new String[hits.length];
        String[] resultSentence= new String[hits.length];
        // 4. display results di controllo
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = indexSearcher.doc(docId);
            System.out.println((i + 1) +") ID: " + d.get("id") + "\t"+" Image_ID: "+d.get("image_id")+"\t"+"caption: "+ d.get("caption"));
            resultID[i]=d.get("id");
            resultSentence[i]=d.get("caption");
            resultImage_ID[i]=d.get("image_id");
        }
        String[][] result={resultID,resultSentence,resultImage_ID};
        return result;
    }
	
}
