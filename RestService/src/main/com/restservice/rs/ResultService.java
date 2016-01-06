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
import java.util.Date;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.restservice.dto.Envelope;
import com.restservice.serviceLogic.ResultLogic;
//import com.sun.jersey.spi.resource.Singleton;
import com.restservice.util.RestUtil;

//@Singleton
/**
 * ResultService-Klasse
 * 
 * Serves as interface to the REST-Server
 * Results: content of the database that is supposed to be displayed in the frontend
 * 
 * Found on the /results path
 */
@Path("/results")
public class ResultService {

	private ResultLogic resultLogic;

	// Use the Rest Test Database when data grouping. Necessary value to
	// test source data in Deamon package with rest test database.
	private boolean useRestTestDBForDataGrouping = false;
	
	/*
	 * default caching time via expires header
	 */
	private long cachingLifetime = 90*60*1000;
	/**
	 * Standard constructor, associates a ResultLogic object with this ResultService
	 * to provide access to the database transactor
	 */
	public ResultService() {
		resultLogic = new ResultLogic();
	}

	/**
	 * 
	 * Allows the association of a specific properties file with the ResultService
	 * So that the database transactor establishes a connection with the specified database
	 * 
	 * @param propertiesPath The path to the properties file
	 */
	public ResultService(String propertiesPath) {
		resultLogic = new ResultLogic(propertiesPath);
	}

	/**
	 * Use the Rest Test Database when data grouping. Necessary value to test
	 * source data in Deamon package with rest test database.
	 * 
	 * @param value
	 *            set true if you test data grouping with rest test database.
	 */
	public void setUseRestTestDBForDataGrouping(boolean value) {
		this.useRestTestDBForDataGrouping = true;
	}

	/**
	 * 
	 * Number of tweets per hour for a specific search term
	 * Now also contains news data because they share a view in the frontend
	 * 
	 * @param id ID of the search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param mod Time of last request to this resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/countPerHour")
	@Produces(APPLICATION_JSON)
	public Response getCountAndNewsPerHour(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getCountAndNewsPerHour(id, lang);
		} catch (Exception e) {
			env.setErrorCodes(e.toString()
					+ "4243 Error while reading data for clustering (sourceData)");
			env.setStacktrace(e.getStackTrace());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * Number of positive and negative tweets per hour for a specified search term
	 * 
	 * @param id ID of the search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param mod Time of the last request to this resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/sentimentPerHour")
	@Produces(APPLICATION_JSON)
	public Response getSentimentPerHour(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getSentimentPerHour(id, lang);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * Number of positive, negative and neutral tweets for a specific search term
	 * 
	 * @param id ID of the search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param mod Time of the last request to this resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/sentiments")
	@Produces(APPLICATION_JSON)
	public Response getSentiments(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getSentiments(id, lang);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}
		
		return response.build();
	}

	/**
	 * Most used hashtags for a specified search term and the number of tweets using them
	 * 
	 * @param id ID of the search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param limit Maximum number of hashtags returned (default: 10)
	 * @param mod Time of the last request to this resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/hashtagstatistics")
	@Produces(APPLICATION_JSON)
	public Response getHashtagStatisticsForSearchTermId(
			@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@DefaultValue("10") @QueryParam("limit") final Long limit,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getHashtagStatisticsForSearchTermId(id, lang,
					limit);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * List of tweets for a specific search term
	 * 
	 * @param id ID of the search term (mandatory)
	 * @param sent Sentiment of the considered tweets (positive, negative, neutral)
	 * @param start Earliest tweet date
	 * @param end Latest tweet date
	 * @param lang Language of the considered tweets
	 * @param HashTagId ID of the hash tag of the considered tweets
	 * @param limit Maximum number of tweets returned (default: 100)
	 * @param mod Time of last request to this resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/tweets")
	@Produces(APPLICATION_JSON)
	public Response getTweets(@QueryParam("id") final Long id,
			@QueryParam("sent") final String sent,
			@QueryParam("start") final String start,
			@QueryParam("end") final String end,
			@QueryParam("lang") final String lang,
			@QueryParam("hashtag") final Long HashTagId,
			@DefaultValue("500") @QueryParam("limit") final Integer limit,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		float sentTop = 1;
		float sentBottom = -1;

		if (!(sent == null)) {
			switch (sent) {
			case "positive":
				sentTop = 1;
				sentBottom = RestUtil.SENTIMENT_UPPER_BORDER;
				break;
			case "neutral":
				sentTop = RestUtil.SENTIMENT_UPPER_BORDER;
				sentBottom = RestUtil.SENTIMENT_LOWER_BORDER;
				break;
			case "negative":
				sentTop = RestUtil.SENTIMENT_LOWER_BORDER;
				sentBottom = -1;
				break;
			}
		}
		Envelope env = new Envelope();
		try {
			env = resultLogic.getTweetsForSearchTerm(id, sentTop, sentBottom,
					start, end, lang, HashTagId, limit);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * A single tweet
	 * 
	 * @param id ID of the requested tweet (mandatory)
	 * @return
	 */
	@GET
	@Path("/tweet")
	@Produces(APPLICATION_JSON)
	public Envelope getTweet(@QueryParam("id") final String id) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getTweet(id);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		return env;
	}

	/**
	 * A single Twitter user
	 * 
	 * @param id ID of the requested user
	 * @return
	 */
	@GET
	@Path("/user")
	@Produces(APPLICATION_JSON)
	public Envelope getUser(@QueryParam("id") final String id) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getUser(id);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}
		return env;
	}

	/**
	 * Accumulated text of tweets for the specified search term
	 * to create the tag cloud on the client side
	 * 
	 * @param id ID of the specified search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param count Maximum number of considered tweets (default: 100)
	 * @param mod Time of last request to the resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/tagcloud")
	@Produces(APPLICATION_JSON)
	public Response getTagCloud(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@DefaultValue("100") @QueryParam("count") final Long count,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getTagCloud(id, lang, count);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * Information on the data groups for displaying the clustering
	 * 
	 * @param id ID of the specified search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param limit Maximum number of considered tweets (default: 100)
	 * @param mod Time of last request to the resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/getDataGroups")
	@Produces(APPLICATION_JSON)
	public Response dataGrouping(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@DefaultValue("100") @QueryParam("limit") final Integer limit,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		if (this.useRestTestDBForDataGrouping) {
			try {
				env = resultLogic.getDataGroups(id, lang, limit, "RestTest");
			} catch (Exception e) {
				env.setStacktrace(e.getStackTrace());
				env.setErrorCodes(e.toString());
				e.printStackTrace();
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
						.entity(env).build());
			}
		} else {
			try {
				env = resultLogic.getDataGroups(id, lang, limit, "Local");
			} catch (Exception e) {
				env.setStacktrace(e.getStackTrace());
				env.setErrorCodes(e.toString());
				e.printStackTrace();
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
						.entity(env).build());
			}
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * In fact, is the same method as @see
	 * com.restservice.rs.ResultService#dataGrouping . But it calls the old
	 * clustering approach.
	 */
	@GET
	@Path("/getDataGroupsAlternative")
	@Produces(APPLICATION_JSON)
	public Response dataGroupingAlternative(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@DefaultValue("100") @QueryParam("limit") final Integer limit,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		if (this.useRestTestDBForDataGrouping) {
			try {
				env = resultLogic.getDataGroupsAlternative(id, lang, limit, "RestTest");
			} catch (Exception e) {
				env.setStacktrace(e.getStackTrace());
				env.setErrorCodes(e.toString());
				e.printStackTrace();
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
						.entity(env).build());
			}
		} else {
			try {
				env = resultLogic.getDataGroupsAlternative(id, lang, limit, "Local");
			} catch (Exception e) {
				env.setStacktrace(e.getStackTrace());
				env.setErrorCodes(e.toString());
				e.printStackTrace();
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
						.entity(env).build());
			}
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * Number of tweets per language for the specified search term
	 * 
	 * @param id ID of the specified search term
	 * @param mod Time of the last request to the resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/languages")
	@Produces(APPLICATION_JSON)
	public Response getLanguages(@QueryParam("id") final Long id,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);
		
		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getLanguages(id);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * News related to a specific search term
	 * 
	 * @param id ID of the specified search term (mandatory)
	 * @param lang Language of the considered tweets
	 * @param day Day of the news publication date (mandatory)
	 * @param month Month of the news publication date (mandatory)
	 * @param year Year of the news publication date (mandatory)
	 * @param mod Time of the last request to the specified resource for caching purposes
	 * @return
	 */
	@GET
	@Path("/news")
	@Produces(APPLICATION_JSON)
	public Response getRelatedNews(@QueryParam("id") final Long id,
			@QueryParam("lang") final String lang,
			@QueryParam("day") final Integer day,
			@QueryParam("month") final Integer month,
			@QueryParam("year") final Integer year,
			@HeaderParam("If-Modified-Since") final String mod) {
		if (id == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter id is mandatory.").build());
		}
		if (day == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter day is mandatory.").build());
		}
		if (month == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter month is mandatory.").build());
		}
		if (year == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Parameter year is mandatory.").build());
		}

		String timeLastFetched = resultLogic.getTimeLastFetched(id);

		if (mod != null && mod.equalsIgnoreCase(timeLastFetched)) {
			return Response.status(HttpURLConnection.HTTP_NOT_MODIFIED).build();
		}

		Envelope env = new Envelope();
		try {
			env = resultLogic.getRelatedNews(id, lang, day, month, year);
		} catch (Exception e) {
			env.setStacktrace(e.getStackTrace());
			env.setErrorCodes(e.toString());
			e.printStackTrace();
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(env)
					.build());
		}

		ResponseBuilder response = Response.status(HttpURLConnection.HTTP_OK)
				.entity(env);
		
		response.expires(new Date(System.currentTimeMillis() + cachingLifetime));
		
		if (!timeLastFetched.equals("")) {
			response.header("Last-Modified", timeLastFetched);
		}

		return response.build();
	}

	/**
	 * Tweets that have influenced the sentiment value of a specific feature the most
	 * 
	 * @param feature The feature of the model (mandatory)
	 * @param language Language of the model
	 * @return
	 */
	@GET
	@Path("/trainingTweets")
	@Produces(APPLICATION_JSON)
	public Envelope getImportantTrainingTweets(
			@QueryParam("feature") final String feature,
			@QueryParam("language") final String language) {
		if (feature == null) {
			throw new WebApplicationException(Response
					.status(HttpURLConnection.HTTP_BAD_REQUEST)
					.entity("Feature is mandatory.").build());
		}
		// FIXME Added for compatibility reasons. Delete as soon as it is
		// ensured that the front-end will always send the tweet's language
		// along with its text
		String languageParameter = language;
		if (languageParameter == null) {
			languageParameter = "en";
		}
		return resultLogic.getImportantTrainingTweets(feature,
				languageParameter);
	}

}
