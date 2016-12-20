package persistent;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

import control.ImResizer;
import control.WordMatrixControl;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import entities.Category;
import entities.Constants;
import entities.ImageCategory;

public class LuceneController extends LuceneCommon
{
	private String dataDirectoryPath=Constants.getDATADIRECTORYPATH();
	
	private LinkedList<String> image_id_caption = new LinkedList<>();
	private LinkedList<String> id_caption = new LinkedList<>();
	private LinkedList<String> Captions = new LinkedList<>();
	private LinkedList<Category> categoryList= new LinkedList<>();
	private LinkedList<LinkedList<ImageCategory>> bigImageCategoryList= new LinkedList<>();
	private String[][] word_matrix;
	
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
        Directory index = FSDirectory.open(indexDirectoryPath); //Creo un index di captions nel file system
       
        IndexWriterConfig configCapt = new IndexWriterConfig(analyzer);
        configCapt.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        IndexWriterConfig configInst = new IndexWriterConfig(analyzer);
        configInst.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        if (!DirectoryReader.indexExists(index)) //Lo riempio solo se non esiste già
        {
        	 System.out.println("L'indice non esiste, verrà quindi ricreato");
        	 FillIndex(index, configCapt); //Riempio l'indice
        	 ImResizer.resizeAllImages();
        	 //System.out.println("Creo le thumbnails, risultato: "+ImResizer.resizeAllImages());
        }
        else
        {
        	System.out.println("Carico solo le categorie dal file");
        	loadCategory();
        }
        
        IndexReader readerCaptions = DirectoryReader.open(index);
        this.indexSearcher = new IndexSearcher(readerCaptions);
	}
	
	
	/**
	 * Questo metodo si occupa di riempire l'index delle Captions
	 * la struttura dei file json è questa qui http://mscoco.org/dataset/#download
	 * per i test posso creare dei file qui http://www.jsoneditoronline.org/
	 * @param index
	 * @param config
	 * @throws IOException
	 */
	private void FillIndex(Directory index, IndexWriterConfig config) throws IOException 
	{
		//System.out.println("Sono in FillIndexCaptions");
		IndexWriter w = new IndexWriter(index, config);
	    File[] files = new File(dataDirectoryPath).listFiles(); //get all files in the data directory
	      
	    if (files!=null)
	    {
	    	//Scorro tutti i file e recupero i dati 
	    	for (File file : files) 
	    	{
	    		//System.out.println("Esamino il file: "+file.getName());
	    		if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && fileFilterCaptions(file))
	    		{
	    			//System.out.println("Il file: "+file.getName()+" è un file di caption");
	    			ParseJSONCaptions(file.getPath()); //estrapolo i dati dal file
	    		}    
	    		if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && fileFilterInstances(file))
	    		{
	    			//System.out.println("Il file: "+file.getName()+" è un file di instances");
	    			ParseJSONInstances(file.getPath()); //estrapolo i dati dal file
	    		}   
	    	}
	    	//Carico la words matrix
	    	this.word_matrix=WordMatrixControl.matrixMaker();
	    	
	    	int i;
	    	for (i=0;i<id_caption.size();i++)
		   {
	    		addDoc(w,image_id_caption.get(i),id_caption.get(i),Captions.get(i)); //aggiungo i dati all'indice
		   }
	    }
	    else
	    {
	    	System.out.println("ATTENZIONE: non ci sono files nella directory: "+dataDirectoryPath ); 
	    }
        w.close();	
	}
	
	/**
	 * Questo metodo restituisce true se il file è un .json ed inizia con caption
	 * 
	 * @param pathname il percorso del File
	 * @return boolean
	 */
	private boolean fileFilterCaptions (File pathname)
	{
		return pathname.getName().toLowerCase().endsWith(".json") && pathname.getName().toLowerCase().startsWith("captions");
	}
	
	/**
	 * Questo metodo restituisce true se il file è un .json ed inizia con instances
	 * 
	 * @param pathname il percorso del File
	 * @return boolean
	 */
	private boolean fileFilterInstances (File pathname)
	{
		return pathname.getName().toLowerCase().endsWith(".json") && pathname.getName().toLowerCase().startsWith("instances");
	}
	
	/**
	 * Questo metodo recupera l'id, l'image_id e la caption dal file e li inserisce nelle liste attributi della classe
	 * @param file l'url del  file da esaminare
	 * @throws IOException
	 */
	private void ParseJSONCaptions(String file) throws IOException 
	{
		System.out.println("Sono in ParseJSONCaptions");
		
		String fileContent=readFile(file);
		String[] filepart=fileContent.split("], "); //Separo le categorie presenti nel file
				
		int i;
		for (i=0;i<filepart.length;i++) //LAVORO SULLE CATEGORIE
		{
			String categoria=filepart[i];
			//System.out.println("La categoria "+i+" inizia con "+categoria.substring(0, 30));
			if (categoria.startsWith("\"annotations\""))
			{
				//System.out.println("Ho trovato la categoria annotations");
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
							image_id_caption.add(dati[k+1]);
							k++;
						}
						else if (dati[k].equals("id\""))
						{
							System.out.println("Ho trovato l'id: "+dati[k+1]);
							id_caption.add(dati[k+1]);
							k++;
						}
						else if (dati[k].equals("caption\""))
						{
							System.out.println("Ho trovato la caption: "+dati[k+1]);
							Captions.add(dati[k+1]);
							k++;
						}
						else
						{
							System.out.println("ERRORE in ParseJSONCaptions: Elemento: "+dati[k]+" non riconosciuto, sono in posizione "+k);
							if (k==6 && dati.length==7) //Il caso più comune è quando il separatore compare nella caption stessa
							{
								System.out.println("E' l'ultimo elemento ed è dopo la caption, presuppongo quindi che faccia parte della stessa");
								String newCapt=Captions.getLast()+", \""+dati[k];
								Captions.removeLast();
								Captions.add(newCapt);
								System.out.println("La caption diventa quindi: "+Captions.getLast());
							}
						}
					}	
				}
			}
		}
		System.out.println("Ho finito ParseJSONCaptions");
	}
	
	/**
	 * Questo metodo recupera l'id, l'image_id la bounding box il nome della categoria e la sua supercategoria dal file e li inserisce nelle liste attributi della classe
	 * @param file l'url del  file da esaminare
	 * @throws IOException
	 */
	private void ParseJSONInstances(String file) throws IOException 
	{
		//System.out.println("Sono in ParseJSONInstances");
		
		String fileContent=readFile(file);
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
		
		//LAVORO SULLE IMMAGINI
		String[] immData=filedata[0].split("}, \\{");
		/*
		 fatte così:
		 "segmentation": [[239.97, 260.24, 222.04, 270.49, 199.84, 253.41, 213.5, 227.79, 259.62, 200.46, 274.13, 202.17, 277.55, 210.71, 249.37, 253.41, 237.41, 264.51, 242.54, 261.95, 228.87, 271.34]], "area": 2765.1486500000005, "iscrowd": 0, "image_id": 558840, "bbox": [199.84, 200.46, 77.71, 70.88], "category_id": 58, "id": 156
		 */
		//System.out.println("Sono in ParseJSONInstances e sto iniziando a recuperare i dati degli oggetti presenti nelle immagini");
		
		//Non avendo ancora immagini categorizzate creo la prima lista
		LinkedList<ImageCategory> imageCategoryList1= new LinkedList<>();
		bigImageCategoryList.add(imageCategoryList1);
		LinkedList<ImageCategory> imageCategoryList= imageCategoryList1;
		
		int i;
		for (i=0;i<immData.length;i++) 
		{
			if (i%500==0)
			{
				int numb=i/5000;
				//System.out.println("Eseguiti: "+i+" lista attualmente utilizzata: "+numb);
				
				//Evito di avere linked list troppo grandi che rallentano l'operazione	
				if (numb>bigImageCategoryList.size()-1)
				{
					//System.out.println("Creo una nuova sottolista visto che la precedente contiene già 4999 elementi");
					LinkedList<ImageCategory> newImageCategoryList= new LinkedList<>();
					bigImageCategoryList.add(newImageCategoryList);
					imageCategoryList= newImageCategoryList;
				}			
			}
			
			String[] elements=immData[i].split("\"image_id\": |, \"bbox\": |, \"category_id\": |\"id\": ");
			String im_id=elements[1];
			String bbox=elements[2];
			String category_id=elements[3].substring(0, elements[3].length()-2);
			//String id=elements[4];
			//System.out.println("la category id è "+category_id);
			
			//Cerco l'immagine corrispondente 
			ImageCategory ic = null;
			boolean present=false;
			
			int n;
			for (n=0;n<bigImageCategoryList.size();n++)
			{
				LinkedList<ImageCategory> scanImageCategoryList=bigImageCategoryList.get(n);
				
				int k;
				for (k=0;k<scanImageCategoryList.size();k++)
				{
					if(scanImageCategoryList.get(k).getIdImage().equals(im_id)) //Avevo già inserito categorie nell'immagine, non devo inserirla nuovamente nella lista
					{
						ic=scanImageCategoryList.get(k);
						//System.out.println("L'immagine con id "+im_id+" è già presente nella lista, la aggiorno");
						present=true;
						break;
					}
				}
				if (present)
				{
					break;
				}
			}			
			if (!present)
			{
				ic=new ImageCategory(im_id);
				imageCategoryList.add(ic);
				//System.out.println("L'immagine con id "+im_id+" non è presente nella lista, la aggiungo");
			}
			
			//Trovo la categoria individuata nell'immagine
			Category cat = null;
			boolean unknow=true;
			int k;
			for (k=0;k<categoryList.size();k++)
			{
				//System.out.println("Confronto con l'id: "+categoryList.get(k).getId()+" alla ricerca dell id: "+category_id);
				if (categoryList.get(k).getId().equals(category_id))
				{
					cat=categoryList.get(k);
					//System.out.println("Ho trovato la category con id "+category_id+" ed è "+cat.getName());
					unknow=false;
					break;
				}
			}
			if (unknow)
			{
				System.out.println("ERRORE: NON E' STATA TROVATA LA CATEGORIA CORRISPONDENTE ALL'ID: "+category_id);
			}
			ic.addCategory(cat, bbox);
		}
		//System.out.println("Ho finito ParseJSONInstances");
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
        
        //Calcolo il vettore e lo salvo
        int[] vector=WordMatrixControl.calculatePhraseVector(caption, word_matrix);
        String vector_string=WordMatrixControl.createStringVector(vector);
        
        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("vector", vector_string, Field.Store.YES));
        
        //tokenized
        doc.add(new TextField("image_id", image_id, Field.Store.YES));
        
        //Recupero i dati salvati in imageCategoryList
        //System.out.println("addDoc: Inizio la ricerca degli oggetti corrispondenti in imageCategoryList");
        int i;
        String bboxString="";
		String catString="";
        boolean finded=false; //di verifica
        
        int k;
        for (k=0;k<bigImageCategoryList.size();k++) //Scorro tutte le sottoliste
        {
        	LinkedList<ImageCategory> imageCategoryList=bigImageCategoryList.get(k);
        	
        	for (i=0;i<imageCategoryList.size();i++)
            {
            	if (i%500==0)
            	{
            		System.out.println("Ciclo: "+k+" Esaminati: "+i);
            	}
            	
            	if(imageCategoryList.get(i).getIdImage().equals(image_id))
            	{
            		finded=true;
            		LinkedList<String> bboxList=imageCategoryList.get(i).getBboxList();
            		LinkedList<Category> catList=imageCategoryList.get(i).getCatList();		
            		int j;
            		for (j=0;j<bboxList.size();j++)
                    {
                    	bboxString=bboxString+bboxList.get(j)+"#";
                    }
                    bboxString=bboxString.substring(0, bboxString.length()-1); //Rimuovo l'ultimo #
                    
                    // use a string field for id because we don't want it tokenized
                    doc.add(new StringField("bboxList", bboxString, Field.Store.YES));
                    
                    for (j=0;j<catList.size();j++)
                    {
                    	catString=catString+catList.get(j).getName()+" # ";
                    }
                    catString=catString.substring(0, catString.length()-1); //Rimuovo l'ultimo spazio
                         
                    //tokenized	
                    doc.add(new TextField("catList", catString, Field.Store.YES));
            		break;
            	}
            }
        	if (finded)
        	{
        		break;
        	}
        }              
        if(!finded)
        {
        	System.out.println("ATTENZIONE addDoc: non sono stati trovate le categorie degli oggetti presenti nell'immagine"); 
        }
           
        w.addDocument(doc);
        //System.out.println("Sono in addDoc ed ho aggiunto il documento con id "+id+" image_id "+image_id+" caption "+caption+" bboxList "+bboxString+" catList "+catString);
    }
       
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo delle captions
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchCaptions(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchCaptions");
    	// the "caption" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("caption", analyzer).parse(querystr); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo delle captions tenendo conto anche del rapporto tra le categorie e le supercategorie
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchCaptionsPlus(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchCaptionsPlus");
    	// the "caption" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("caption", analyzer).parse(ElaborateUnderCategories(querystr)); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di riconoscere le sottocategorie delle categorie presenti nella query e ad inserirle nella ricerca
     * 
     * @param oldQuery la query originale
     * @return String la query con anche le sottocategorie
     */
    private String ElaborateUnderCategories(String oldQuery)
    {
    	//System.out.println("Sono in ElaborateUnderCategories");
    	String elements[]= oldQuery.split(" "); //Separo le singole parole
    	String undercat=" ";
    	String UNDERCATWEIGHT=Constants.getUNDERCATWEIGHT(); //Peso dato alle sottocategorie di quella cercata 
    	
    	int i;
    	for (i=0;i<elements.length;i++)
    	{
    		//System.out.println("Esamino questo elemento della query: "+elements[i]);
    		
    		int j;
    		for (j=0;j<categoryList.size();j++)
    		{
    			//System.out.println("Lo confronto con questa categoria: "+categoryList.get(j).getName()+" che ha come supercategoria "+categoryList.get(j).getSupercategory());
    			if (elements[i].equals(categoryList.get(j).getSupercategory())) //Se la parola cercata è la supercategoria di una categoria allora la categoria fa parte della ricerca
    			{
    				undercat=undercat+categoryList.get(j).getName()+UNDERCATWEIGHT;
    				//System.out.println("E' una di quelle che cercavo, la stringa diventa quindi: "+undercat);
    			}
    		}
    	}
    	//System.out.println("La query è: "+oldQuery+undercat);
    	return oldQuery+undercat;
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo  delle category
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchCategory(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchCategory");
    	// the "catList" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("catList", analyzer).parse(querystr); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo  delle category tenendo conto anche del rapporto tra le categorie e le supercategorie
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchCategoryPlus(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchCategoryPlus");
    	// the "catList" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("catList", analyzer).parse(ElaborateUnderCategories(querystr)); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo delle category e delle caption
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchBothCatCap(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchBothCatCap");
    	
    	//Aggiungo il tag per fare la ricerca anche nelle Caption
    	String complexQuerystr="caption:("+querystr+") OR "+querystr;
    	//System.out.println("La query complessa è: "+complexQuerystr);
    	
    	// the "catList" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("catList", analyzer).parse(complexQuerystr); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di eseguire la ricerca sul campo delle category e delle caption tenendo conto anche del rapporto tra le categorie e le supercategorie
     * @param search la query
     * @return String[][] formato dagli array resultID e resultSentence
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] searchBothCatCapPlus(String querystr) throws IOException, ParseException
    {
    	//System.out.println("Sono in searchBothCatCapPlus");
    	querystr=ElaborateUnderCategories(querystr); //Aggiorno la query con le sottocategorie
    	
    	//Aggiungo il tag per fare la ricerca anche nelle Caption
    	String complexQuerystr="caption:("+querystr+") OR "+querystr;
    	//System.out.println("La query complessa è: "+complexQuerystr);
    	
    	// the "catList" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("catList", analyzer).parse(complexQuerystr); //Crea una query Lucene
        return searcher(q);
    }
    
    /**
     * Questo metodo si occupa di eseguire la query richiesta e restituisce i risultati
     * @param q la query
     * @return result={resultID,resultCaptions,resultImage_ID,resultCategory,resultBBox}
     * @throws IOException
     * @throws ParseException 
     */
    private String[][] searcher(Query q) throws IOException, ParseException
    {
    	//System.out.println("Sono in searcher e la query è: "+q.toString());
    	// 3. search
        int HITSFORPAGE = 100; 
        
        TopDocs docs = indexSearcher.search(q, HITSFORPAGE);
        ScoreDoc[] hits = docs.scoreDocs;

        String[] resultID= new String[hits.length];
        String[] resultImage_ID= new String[hits.length];
        String[] resultCategory= new String[hits.length];
        String[] resultCaptions= new String[hits.length];
        String[] resultBBox= new String[hits.length];
        
        // 4. display results 
        //System.out.println("Found " + hits.length + " hits.");
        
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = indexSearcher.doc(docId);
            //System.out.println((i + 1) +") ID: " + d.get("id") + "\t"+" Image_ID: "+d.get("image_id")+"\t"+"Caption: "+ d.get("caption")+"\t"+"Categories: "+d.get("catList")+"\t"+"Bonding Boxes: "+d.get("bboxList"));
            resultID[i]=d.get("id");
            resultCategory[i]=d.get("catList");
            resultCaptions[i]=d.get("caption");
            resultImage_ID[i]=d.get("image_id");
            resultBBox[i]=d.get("bboxList");
        }
        String[][] result={resultID,resultCaptions,resultImage_ID,resultCategory,resultBBox};
        return searchFilter(result);
    }
	
    /**
     * Questo metodo si occupa di caricare la lista delle categorie dal file nel caso in cui l'indice fosse già presente
     * @throws IOException
     */
    private void loadCategory() throws IOException
    {
    	//System.out.println("Sono in loadCategory");
    	
    	File[] files = new File(dataDirectoryPath).listFiles(); //get all files in the data directory
	      
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
    }
}
