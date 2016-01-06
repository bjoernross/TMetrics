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
package com.restservice.rs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.restservice.dto.Tweet;

@Path("/rest")
public class Service
{
	@GET
	@Path("/tweets")
	@Produces(APPLICATION_JSON)
	public Response getTweets(@QueryParam("id") final int searchTermID,
							   @DefaultValue("100") @QueryParam("limit") final int limit,
							   @HeaderParam("If-Modified-Since") final String modified)
	{
		List<Tweet> tweets = getTweetsFromDB(searchTermID, limit);
		
		return Response.status(HttpURLConnection.HTTP_OK).entity(tweets).build();
	}	
	
	
	public List<Tweet> getTweetsFromDB(int id, int limit)
	{
		return new ArrayList<Tweet>();
	}
}