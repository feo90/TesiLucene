package persistent;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneSearcher extends LuceneCommon
{
	public LuceneSearcher() throws IOException
	{
		// 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        this.analyzer = new StandardAnalyzer();

        // 1. create the index
        Directory index = FSDirectory.open(indexDirectoryPath); //Creo un index di captions nel file system
        
        if (!DirectoryReader.indexExists(index)) //Lo riempio solo se non esiste già
        {
        	 System.out.println("L'indice non esiste!");
        }

        IndexReader readerCaptions = DirectoryReader.open(index);
        this.indexSearcher = new IndexSearcher(readerCaptions);
	}

	/**
	 * Questo metodo si occupa di recuperare tutti i campi associati ad una immagine
	 * @param id: l'id dell'immagine in formato String
	 * @return result={resultID,resultCaptions,resultImage_ID,resultCategory,resultBBox}
	 * @throws ParseException
	 * @throws IOException
	 */
	public String[][] SearchById(String id) throws ParseException, IOException
	{
		// the "caption" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("image_id", analyzer).parse(id); //Crea una query Lucene
        
        int HITSFORPAGE = 100; 
        
        TopDocs docs = indexSearcher.search(q, HITSFORPAGE);
        ScoreDoc[] hits = docs.scoreDocs;

        String[] resultID= new String[hits.length];
        String[] resultImage_ID= new String[hits.length];
        String[] resultCategory= new String[hits.length];
        String[] resultCaptions= new String[hits.length];
        String[] resultBBox= new String[hits.length];
        
        // 4. display results di controllo
        System.out.println("Found " + hits.length + " hits.");
        
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = indexSearcher.doc(docId);
            System.out.println((i + 1) +") ID: " + d.get("id") + "\t"+" Image_ID: "+d.get("image_id")+"\t"+"Caption: "+ d.get("caption")+"\t"+"Categories: "+d.get("catList")+"\t"+"Bonding Boxes: "+d.get("bboxList"));
            resultID[i]=d.get("id");
            resultCategory[i]=d.get("catList");
            resultCaptions[i]=d.get("caption");
            resultImage_ID[i]=d.get("image_id");
            resultBBox[i]=d.get("bboxList");
        }
        String[][] result={resultID,resultCaptions,resultImage_ID,resultCategory,resultBBox};
        return searchFilter(result);
	}
}
