package servlet;

import java.io.IOException;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import control.GoldStandardControl;

/**
 * Servlet implementation class ExportGS
 */
@WebServlet("/ExportGS")
public class ExportGSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExportGSServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//Recupero la query ed i risultati
		HttpSession session = request.getSession();
		String query=(String) session.getAttribute("query");
		String[] resImId= (String[]) session.getAttribute("resultImID");
		//System.out.print("PRINT DI CONTROLLO: Sono in ExportGSServlet in doGet e la query è "+query+" \n");
		
		LinkedList<String> relevant=new LinkedList<>();
		LinkedList<String> irrelevant=new LinkedList<>();	
		
		if ((query!=null)&&(query.length()>1)) //Mi assicuro che sia stata scritta una query
		{
			int i;
			for (i=0;i<resImId.length;i++)
			{
				String cb="cb"+i;
				
				String value=(String) request.getParameter(cb);
				//System.out.println("PRINT DI CONTROLLO: L'immagine in posizione "+num+" ha come valore "+value);
				
				if (value.equals("relevant"))
				{
					relevant.add(resImId[i]);
				}
				else if(value.equals("irrelevant"))
				{
					irrelevant.add(resImId[i]);
				}
				else
				{
					System.out.println("ERROR: In rank "+i+" for the element "+resImId[i]+" with the associate value of: "+value);
				}
			}		
			//Salvo i dati
			GoldStandardControl.saveGoldStandard(relevant, irrelevant,query);
		}
		else
		{
			System.out.print("ERROR: Can't save, query: "+query);
		}
		
		// Refresh della pagina
		String PAGE = "Intro.jsp";
		response.sendRedirect(PAGE);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
