/*******************************************************************************
 * This file is part of Tmetrics.
 *
 * Tmetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tmetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tmetrics. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ajax;

import java.io.*;

import javax.servlet.http.*;
import javax.servlet.*;

/*
 * codes for action
 * 1  : is item in database?
 * 2  : insert item into database
 * 3  : get database statistics
 * 4  : get sentiment for item
 */

public class MainServlet extends HttpServlet {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public void doGet (HttpServletRequest req,
                     HttpServletResponse response)
    throws ServletException, IOException
    {
	response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    int action = Integer.parseInt(req.getParameter("action"));
    switch (action) {
    	case 1: //Dummy Output for MetaData
	        out.println("{");
	        out.println("\"item\": \""+req.getParameter("item")+"\",");
	        out.println("\"oldest\": \"14-11-2013\",");
	        out.println("\"newest\": \"17-11-2013\",");
	        out.println("\"count\": "+(int) req.getParameter("item").charAt(0));
	        out.println("}");
	        break;
    	case 2: //insert item into database
    		break;
//    	case 3: //get database statistics for item
//    		break;
//    	case 4: //get sentiment for item
//    		//simulate hit in database
//	        if (req.getParameter("item").equalsIgnoreCase("Merkel")) {
//	        	out.println("{");
//	        	out.println("  \"item\" : \"Merkel\",");
//	        	out.println("  \"positiv\" : \"100000\",");
//	        	out.println("  \"neutral\" : \"5000\",");
//	        	out.println("  \"negativ\" : \"90000\",");
//	        	out.println("}");
//	        }
//	        //simulate miss in database
//	        else {
//	        	throw new ServletException("There is no database entry for "+ req.getParameter("item"));
//	        }
//    		break;
        default:
        	out.println("{");
	        out.println("}");
	        break;
    }
    out.close();
  }
  
  public void doPost (HttpServletRequest req,
                     HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        int action = Integer.parseInt(req.getParameter("action"));
	    switch (action) {
	    	case 1: //is item in database?
		        out.println("{");
		        if (req.getParameter("item").equalsIgnoreCase("Merkel")) {
		        	out.println("  \"isInDatabase\" : \"true\"");
		        }
		        else {
		        	out.println("  \"isInDatabase\" : \"false\"");
		        }
		        out.println("}");
		        break;
	    	case 2: //insert item into database
	    		out.println("{");
		        out.println("}");
	    		break;
	    	case 3: //get database statistics for item
	    		if (req.getParameter("item").equalsIgnoreCase("Merkel")) {
		        	out.println("{");
		        	out.println("  \"item\" : \"Merkel\",");
		        	out.println("  \"oldestTweet\" : \"100000\",");
		        	out.println("  \"newestTweet\" : \"5000\",");
		        	out.println("  \"totalCountofTweets\" : 90000");
		        	out.println("}");
		        }
		        //simulate miss in database
		        else {
		        	out.println("{");
		        	out.println("  \"item\" : \""+req.getParameter("item")+"\",");
		        	out.println("  \"oldestTweet\" : \"unknown\",");
		        	out.println("  \"newestTweet\" : \"unknown\",");
		        	out.println("  \"totalCountofTweets\" : \"unknown\",");
		        	out.println("}");
		        }
	    		break;
	    	case 4: //get sentiment for item
	    		//simulate hit in database
		        if (req.getParameter("item").equalsIgnoreCase("Merkel")) {
		        	out.println("{");
		        	out.println("  \"item\" : \"Merkel\",");
		        	out.println("  \"positiv\" : 100000,");
		        	out.println("  \"neutral\" : 5000,");
		        	out.println("  \"negativ\" : 90000");
		        	out.println("}");
		        }
		        //simulate miss in database
		        else {
		        	out.println("{");
		        	out.println("  \"item\" : \""+req.getParameter("item")+"\",");
		        	out.println("  \"positiv\" : 0,");
		        	out.println("  \"neutral\" : 0,");
		        	out.println("  \"negativ\" : 0");
		        	out.println("}");
		        }
	    		break;
	        default:
	        	out.println("{");
		        out.println("}");
		        break;
	    }
	    out.close();
        //out.println("");
    }
}
