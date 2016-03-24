package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.lucene.queryparser.classic.ParseException;

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
		System.out.print("PRINT DI CONTROLLO: Sono in SearchServlet in doGet\n");
    	
    	String search=request.getParameter("search");
    	String[][] result=null;
    	try {
    		result=lucCon.search(search);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Creo la sessione del client
		HttpSession session = request.getSession();
		session.setAttribute("result",result);
		
		// Refresh della pagina
		String PAGE = "Intro.jsp";
		response.sendRedirect(PAGE);
    	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//NULLA
	}

}
