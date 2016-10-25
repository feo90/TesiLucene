package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import control.RankingControl;

/**
 * Servlet implementation class RefineServlet
 */
@WebServlet("/RefineServlet")
public class RefineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RefineServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		//Recupero la query ed i risultati
		HttpSession session = request.getSession();
		String query=(String) session.getAttribute("query");
		String[][] result = (String[][]) session.getAttribute("result");
		String[][] newresult=RankingControl.rank(result, query);
		session.setAttribute("result",newresult);
		session.setAttribute("query", query);
		
		// Refresh della pagina
		String PAGE = "Intro.jsp";
		response.sendRedirect(PAGE);
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
