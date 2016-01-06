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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.restservice.dto.Envelope;
import com.restservice.serviceLogic.QueryLogic;

@Path("/queries")
public class QueryService {

	private QueryLogic queryLogic;

	public QueryService() {
		queryLogic = new QueryLogic();
	}

	public QueryService(String propertiesPath) {
		queryLogic = new QueryLogic(propertiesPath);
	}

	@POST
	@Path("/post")
	@Produces(APPLICATION_JSON)
	public Envelope postTwitterQuery(@QueryParam("q") final String squery) {
		if (squery == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter q is mandatory.").build());
		}

		Envelope env = new Envelope();

		try {
			env = queryLogic.postTwitterQuery(squery);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}
		return env;
	}

	@GET
	@Path("/bystring")
	@Produces(APPLICATION_JSON)
	public Envelope getQueries(@QueryParam("q") final String squery) {
		if (squery == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter q is mandatory.").build());
		}

		Envelope env = new Envelope();

		try {
			env = queryLogic.getQueries(squery);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@GET
	@Path("/byid")
	@Produces(APPLICATION_JSON)
	public Envelope getQueries(@QueryParam("id") final Long id) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		Envelope env = new Envelope();

		try {
			env = queryLogic.getQueries(id);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@GET
	@Path("/contains")
	@Produces(APPLICATION_JSON)
	public Envelope containsQuery(@QueryParam("q") final String squery) {
		if (squery == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter q is mandatory.").build());
		}

		Envelope env = new Envelope();

		try {
			env = queryLogic.containsQuery(squery);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@GET
	@Path("/suggestions")
	@Produces(APPLICATION_JSON)
	public Envelope getMatchingQueries(@QueryParam("q") final String squery) {
		if (squery == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter q is mandatory.").build());
		}
		Envelope env = new Envelope();

		try {
			env = queryLogic.getMatchingQueries(squery);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@GET
	@Path("/metadata")
	@Produces(APPLICATION_JSON)
	public Response getMetadata(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang, 
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}
		
		Envelope env = new Envelope();
		
		String timeLastFetched = queryLogic.getTimeLastFetched(id);
		
		if(mod != null && mod.equals(timeLastFetched))
		{
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}
		
		
		try {
			env = queryLogic.getMetadata(id, lang);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK).entity(env);
		
		if(!timeLastFetched.equals("")){
			response.header("Last-Modified", timeLastFetched);
		}
		
		return response.build();
	}

	@GET
	@Path("/status")
	@Produces(APPLICATION_JSON)
	public Envelope getDaemonStatus() {
		Envelope env = new Envelope();

		try {
			env = queryLogic.getDaemonStatus();
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@POST
	@Path("/postPriority")
	@Produces(APPLICATION_JSON)
	public Envelope postPriority(@QueryParam("id") final Integer searchTermId,
			@QueryParam("p") final Integer newPriority) {
		if (searchTermId == null || newPriority == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameters id and p are mandatory.").build());
		}
		Envelope env = new Envelope();

		try {
			env = queryLogic.postPriority(searchTermId, newPriority);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			//e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@POST
	@Path("/postActiveFlag")
	@Produces(APPLICATION_JSON)
	public Envelope postActiveFlag(
			@QueryParam("id") final Integer searchTermId,
			@QueryParam("active") final Boolean newActiveFlag) {
		if (searchTermId == null || newActiveFlag == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameters id and active are mandatory.").build());
		}

		Envelope env = new Envelope();

		try {
			env = queryLogic.postActiveFlag(searchTermId, newActiveFlag);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	@GET
	@Path("/hasDaemonFetched")
	@Produces(APPLICATION_JSON)
	public Envelope hasDaemonFetched(
			@QueryParam("id") final Integer searchTermId) {
		if (searchTermId == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}
		Envelope env = new Envelope();

		try {
			env = queryLogic.hasDaemonFetched(searchTermId);
		} catch (Exception e) {
			env.setErrorCodes(e.toString());
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

}
