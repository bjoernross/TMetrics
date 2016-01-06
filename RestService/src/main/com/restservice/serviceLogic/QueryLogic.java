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
package com.restservice.serviceLogic;

import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.restservice.database.Transactor;
import com.restservice.dto.Envelope;
import com.restservice.dto.Query;
import com.restservice.dto.DaemonStatus;
import com.restservice.dto.QueryMetadata;
import com.restservice.dto.QueryWithOccurence;

/**
 * Service logic handling search term related actions and requests
 * 
 * @author
 */
public class QueryLogic {

	private Transactor transactor;
	
	public QueryLogic() {
		transactor = new Transactor();
	}
	
	public QueryLogic(String propertiesPath) {
		transactor = new Transactor(propertiesPath);
	}

	/**
	 * Request handler for adding new search terms
	 * 
	 * @param squery
	 *            search term string
	 * @return envelope containing a status message
	 * @throws SQLException 
	 */
	public Envelope postTwitterQuery(String squery) throws SQLException {
		Envelope env = new Envelope();

			transactor.saveSearchTerm(squery);
		
		return env;
	}

	/**
	 * Request handler for getting a search term
	 * 
	 * @param queryString
	 *            search term string
	 * @return envelope containing a status message and a search term DTO
	 * @throws SQLException 
	 */
	public Envelope getQueries(String queryString) throws SQLException {
		Query query;
		Envelope env = new Envelope();

			query = transactor.getSearchTerms(queryString);
			env.setData(query);
		
		return env;
	}

	/**
	 * Request handler for getting a search term
	 * 
	 * @param id
	 *            search term index
	 * @return envelope containing a status message and a search term DTO
	 * @throws SQLException 
	 */
	public Envelope getQueries(Long id) throws SQLException {
		Query query;
		Envelope env = new Envelope();
			query = transactor.getSearchTerms(id);
			env.setData(query);
		

		return env;
	}

	/**
	 * Request handler for checking existence of search term strings
	 * 
	 * @param squery
	 *            search term string
	 * @return envelope containing a status message and a boolean, that is true
	 *         if the search term string exits in the database
	 * @throws SQLException 
	 */
	public Envelope containsQuery(String squery) throws SQLException {
		Envelope env = new Envelope();

		
			boolean bContains = transactor.containsSearchTerm(squery);

			env.setData(bContains);
		

		return env;
	}

	/**
	 * Request handler for getting search terms and their search result count
	 * 
	 * @param squery
	 *            search term string
	 * @return envelope containing a status message and a DTO
	 * @throws SQLException 
	 */
	public Envelope getMatchingQueries(String squery) throws SQLException {
		Envelope env = new Envelope();

			List<QueryWithOccurence> queries = transactor
					.getMatchingSearchTerms(squery);

			env.setData(queries);
		

		return env;
	}

	/**
	 * Request handler for getting search term meta information
	 * 
	 * @param id
	 *            search term index
	 * @return envelope containing a status message and a search term meta
	 *         information DTO
	 * @throws SQLException 
	 */
	public Envelope getMetadata(Long id, String lang) throws SQLException
	{
		Envelope env = new Envelope();

		
		QueryMetadata query = transactor.getMetadataForQuery(id, lang);
			env.setData(query);
		

		return env;

	}

	/**
	 * Request handler for getting the daemon status
	 * 
	 * @return envelope containing a status message and a daemon status DTO
	 * @throws SQLException 
	 */
	public Envelope getDaemonStatus() throws SQLException {
		Envelope env = new Envelope();
		
		
			DaemonStatus status = transactor.getDaemonStatus();
			env.setData(status);
		

		return env;
	}

	/**
	 * Request handler for adding new priority for a search term
	 * 
	 * @param id search term id
	 * @param newPriority new priority to be saved for the given search term id
	 * 
	 * @return envelope containing a status message
	 * @throws SQLException 
	 */
	public Envelope postPriority(Integer id, Integer newPriority) throws SQLException {
		Envelope env = new Envelope();

		
			//Priority needs to be an integer between -2 and 2 (inclusive)
			if (newPriority < -2 || newPriority > 2) {
				
				throw new WebApplicationException(Response
						.status(HttpURLConnection.HTTP_BAD_REQUEST)
						.entity("The new priority (" + newPriority + ") is not valid. "
						+ "Only the following values are accepted: -2, -1, 0, 1, 2.").build());
			}
			transactor.savePriority(id, newPriority);
	

		return env;
	}

	/**
	 * Request handler for changing the active flag for a search term
	 * 
	 * @param id search term id
	 * @param newActiveFlag the new active flag saved for the given search term id
	 * 
	 * @return envelope containing a status message
	 * @throws SQLException 
	 */
	public Envelope postActiveFlag(Integer id, Boolean newActiveFlag) throws SQLException {
		Envelope env = new Envelope();

	
			transactor.saveActiveFlag(id, newActiveFlag);
		

		return env;
	}
	
	
	/**
	 * Request handler for checking if the daemon already fetched something for a given search term
	 * @throws SQLException 
	 */
	public Envelope hasDaemonFetched(Integer searchTermId) throws SQLException {
		Envelope env = new Envelope();
		
		
			Boolean hasFetched = transactor.hasDaemonFetched(searchTermId);
			env.setData(hasFetched);
		

		return env;
	}
	
	public String getTimeLastFetched(Long id){
		
		try 
		{
			return transactor.getTimeLastFetched(id);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return "";
		}
		
	}

}
