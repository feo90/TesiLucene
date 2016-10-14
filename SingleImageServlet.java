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
import lucene.LuceneSearcher;

/**
 * Servlet implementation class SingleImageServlet
 */
@WebServlet("/SingleImageServlet")
public class SingleImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private LuceneSearcher luSerch;
       
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public SingleImageServlet() throws IOException 
    {
        super();
        luSerch=new LuceneSearcher();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("PRINT DI CONTROLLO: Sono in SingleImageServlet in doGet");
		HttpSession session = request.getSession();
		
		if (request.getParameter("edit") != null) //Verifico se è stata fatta la richiesta di editare il gs
		{
			String query=(String) session.getAttribute("query");
			System.out.println("PRINT DI CONTROLLO: E' stato richiesto l'editing del gs della query: "+query);
			String[] old_gs=GoldStandardControl.findGoldStandard(query);
			boolean[] gs_value=new boolean[old_gs.length];
			
			if ((query!=null)&&(query.length()>1)) //Mi assicuro che sia stata scritta una query
			{
				int i;
				for (i=0;i<old_gs.length;i++)
				{
					int num=i+1;
					String cb="cb"+i;
					
					String value=(String) request.getParameter(cb);
					System.out.println("PRINT DI CONTROLLO: L'immagine in posizione "+num+" ha come valore "+value);
					
					if (value.equals("relevant"))
					{
						gs_value[i]=true;
					}
					else if(value.equals("irrelevant"))
					{
						gs_value[i]=false;
					}
					else
					{
						System.out.println("ERROR: In rank "+i+" for the element "+old_gs[i]+" with the associate value of: "+value);
					}
				}		
				//Salvo i dati
				GoldStandardControl.editGoldStandard(gs_value,query);
				// Refresh della pagina
				String[] gold_standard=GoldStandardControl.findGoldStandard(query);
				session.setAttribute("gs",gold_standard);
				String PAGE = "ShowGoldStandard.jsp";
			    response.sendRedirect(PAGE);
			    return;
			}
			else
			{
				System.out.print("PRINT DI CONTROLLO: Non sono riuscito ad iniziare il salvataggio, la query è: "+query);
			}
		}
		else //Voglio vedere un'immagine nel dettaglio
		{
			String imID=request.getParameter("imagebt");
			System.out.println("ImID= "+imID);
			String[][] single_result = null;
			
			try 
			{
				single_result=luSerch.SearchById(imID);
			} 
			catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (single_result==null)
			{
				System.out.println("ERRORE: non sono state trovate captions per l'immagine: "+imID);
			}
					
			// Refresh della pagina
			String [][] result=(String[][]) session.getAttribute("result");
			session.setAttribute("result", result);
			session.setAttribute("sinres",single_result);

			//Verifico se arrivo o meno dal GS 
			String gs_check=request.getParameter("show");
			if (gs_check!=null)
			{
				session.setAttribute("gscheck", "true");
			}
			
		    String PAGE = "SingleImage.jsp";
		    response.sendRedirect(PAGE);
		}		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
