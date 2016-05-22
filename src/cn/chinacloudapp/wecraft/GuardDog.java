package cn.chinacloudapp.wecraft;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

/**
 * Servlet implementation class GuardDog
 */
@WebServlet("/GuardDog")
public class GuardDog extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GuardDog() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String sourceType = "", sourceId = "", targetType = "", targetId = "";
		PathFetch path = new PathFetch();
		try {
			sourceId = request.getParameter("id1");
			targetId = request.getParameter("id2");
			sourceType = path.confirmType(sourceId);
			targetType = path.confirmType(targetId);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		
		System.out.println(sourceType + sourceId);
		System.out.println(targetType + targetId);
	  	List<ArrayList<String>> ret = path.doDistribute(sourceType, sourceId, targetType, targetId);
	  	long end = System.currentTimeMillis();	
	 	System.out.println("timeout:" + Long.toString(end - start));
	 	System.out.println("api_count: " + PathFetch.api_count);
	 	System.out.println("path_count: " + PathFetch.count);
	 	
	 	ret  = new ArrayList(new HashSet(ret));
	 	JSONArray arrayObj = new JSONArray(ret);
	 	out.print(arrayObj);
	 	out.flush();
	 	out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request, response);
	}


}
