package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.lucene.queryparser.classic.ParseException;

import control.GoldStandardControl;
import lucene.LuceneController;

/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private LuceneController lucCon;
	
       
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public SearchServlet() throws IOException {
        super();
        lucCon= new LuceneController();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in SearchServlet in doGet");
		String search=request.getParameter("search");
		
		if (request.getParameter("start_search") != null) 
		{
			String mode=request.getParameter("mode");
	    	System.out.println("Modalità selezionata: "+mode);
	    	String[][] result=null;
	    	try {
	    		if (mode.equals("cap"))
	    		{
	    			result=lucCon.searchCaptions(search);
	    		}
	    		else if (mode.equals("cat"))
	    		{
	    			result=lucCon.searchCategory(search);
	    		}
	    		else if (mode.equals("both"))
	    		{
	    			result=lucCon.searchBothCatCap(search);
	    		}
	    		else if (mode.equals("capplus"))
	    		{
	    			result=lucCon.searchCaptionsPlus(search);
	    		}
	    		else if (mode.equals("catplus"))
	    		{
	    			result=lucCon.searchCategoryPlus(search);
	    		}
	    		else if (mode.equals("bothplus"))
	    		{
	    			result=lucCon.searchBothCatCapPlus(search);
	    		}
	    		else
	    		{
	    			System.out.println("ERRORE: La modalità selezionata: "+mode+" non è riconosciuta");
	    		}
	    		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	// Creo la sessione del client
			HttpSession session = request.getSession();
			session.setAttribute("result",result);
			session.setAttribute("query", search);

			// Refresh della pagina
			String PAGE = "Intro.jsp";
			response.sendRedirect(PAGE);
			return;
		} 
		else if (request.getParameter("showGS") != null) 
		{
			System.out.println("PRINT DI CONTROLLO: Mostro il GS");
			String[] gold_standard=GoldStandardControl.findGoldStandard(search);
		    
		    // Creo la sessione del client
		    HttpSession session = request.getSession();
		    session.setAttribute("gs",gold_standard);
		    session.setAttribute("query", search);
		 
		 	// Refresh della pagina
		 	String PAGE = "ShowGoldStandard.jsp";
		 	response.sendRedirect(PAGE);
		 	return;
		}
		else
		{
			System.out.println("ERRORE: Bottone non riconosciuto!");
			// Refresh della pagina
		 	String PAGE = "Intro.jsp";
		 	response.sendRedirect(PAGE);
		 	return;
		}    	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//NULLA
	}

}
