package lucene;


import java.io.IOException;

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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneController 
{

	private IndexSearcher indexSearcher;
	private StandardAnalyzer analyzer;
	
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
        Directory index = new RAMDirectory(); //Semplice indice in memoria

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);
        
        addDoc(w, "Lucene in Action", "193398817");
        addDoc(w, "Lucene for Dummies", "55320055Z");
        addDoc(w, "Managing Gigabytes", "55063554A");
        addDoc(w, "The Art of Computer Science", "9900333X");
        w.close();
        
        IndexReader reader = DirectoryReader.open(index);
        this.indexSearcher = new IndexSearcher(reader);
		
	}
	
	/**
     * Crea un Document Lucene nell'Index a cui fa riferimento l'IndexWriter, la frase viene "tokenizzata" ed utilizzata per le ricerche mentre l'id no
     * @param w l'IndexWriter
     * @param sentence
     * @param id
     * @throws IOException
     */
    private static void addDoc(IndexWriter w, String sentence, String id) throws IOException 
    {
        Document doc = new Document(); //Lucene Document
        doc.add(new TextField("sentence", sentence, Field.Store.YES));

        // use a string field for id because we don't want it tokenized
        doc.add(new StringField("id", id, Field.Store.YES));
        w.addDocument(doc);
    }
    
    /**
     * 
     * @param search
     * @return
     * @throws IOException 
     * @throws ParseException 
     */
    public String[][] search(String search) throws IOException, ParseException
    {
    	// 2. query
        String querystr;
        if (search.length()>0)
        {
        	querystr =search;
        }
        else 
        {
        	querystr ="lucene";
        }

        // the "sentence" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("sentence", analyzer).parse(querystr); //Crea una query Lucene

        // 3. search
        int hitsPerPage = 10;
        TopDocs docs = indexSearcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        String[] resultID= new String[hits.length];
        String[] resultSentence= new String[hits.length];
        // 4. display results di controllo
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = indexSearcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("sentence"));
            resultID[i]=d.get("id");
            resultSentence[i]=d.get("sentence");
        }
        String[][] result={resultID,resultSentence};
        return result;
    }
	
}
