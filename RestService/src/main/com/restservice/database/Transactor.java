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
package com.restservice.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.restservice.dto.DaemonStatus;
import com.restservice.dto.HashtagStatisticsForSearchTermId;
import com.restservice.dto.Language;
import com.restservice.dto.LanguageCount;
import com.restservice.dto.Query;
import com.restservice.dto.QueryMetadata;
import com.restservice.dto.QueryWithOccurence;
import com.restservice.dto.SearchTermStatus;
import com.restservice.dto.SearchTermsPerQueryPerDate;
import com.restservice.dto.Sentiment;
import com.restservice.dto.SentimentData;
import com.restservice.dto.SentimentPerQueryPerDate;
import com.restservice.dto.Tweet;
import com.restservice.dto.TweetBasic;
import com.restservice.dto.TweetTexts;
import com.restservice.dto.TweetWithUser;
import com.restservice.dto.TwitterUser;
import com.restservice.dto.TwitterUserBasic;
import com.restservice.util.RestUtil;

/**
 * Communication with the database is handled by this class.
 * 
 * @author
 */
public class Transactor {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement prepStatement = null;
	private PreparedStatement readStatement = null;
	private ResultSet resultSet = null;
	private String dbUrl = "";
	private String readQuery = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED ;";

	// Infrastructure
	
	// Use default database properties file
	public Transactor() {
		this(System.getProperty("user.home") + "/database.properties");
	}

	public Transactor(String propertiesPath) {
		FileInputStream fis = null;
		try {
			Properties props = new Properties();
			fis = new FileInputStream(propertiesPath);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));

			dbUrl = props.getProperty("javabase.jdbc.url") + 
					props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * closes the database connection
	 */
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (connect != null) {
				connect.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (prepStatement != null) {
				prepStatement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResultSet executeQuery(String query) throws SQLException {
		connect = DriverManager.getConnection(dbUrl);
		prepStatement = connect.prepareStatement(query);

		return prepStatement.executeQuery();
	}

	// Result Logic

	/**
	 * Returns the Number of positive, neutral and negative tweets associated
	 * with a specified search term as a DTO. (Sentiments are now floats.
	 * The borders between positive, neutral and negative are specified by
	 * RestUtil.SENTIMENT_UPPER_BORDER and RestUtil.SENTIMENT_LOWER_BORDER)
	 * 
	 * @param id
	 *            specified search term
	 * @return count of positive, neutral an negative tweets as DTO
	 * @throws SQLException
	 *             thrown if an SQL-Error occurs
	 */
	public SentimentData getSentimentData(long id, String lang) throws SQLException
	{
		long positive = 0;
		long neutral = 0;
		long negative = 0;

		try
		{
			 	
		
			
			String query = "select sum(if(sentiment >= " + RestUtil.SENTIMENT_UPPER_BORDER + ", 1, 0)) as positive, "
					     + "sum(if(sentiment < " + RestUtil.SENTIMENT_UPPER_BORDER + " && sentiment > " + RestUtil.SENTIMENT_LOWER_BORDER + ", 1, 0)) as neutral, "
					     + "sum(if(sentiment <= " + RestUtil.SENTIMENT_LOWER_BORDER + ", 1, 0)) as negative "
					     + "from tweets_has_search_terms "
					     + "where search_terms_id = ? "
					     + ((lang != null) ? "and iso_language_code = ? " : "")
					     + ";";
			
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(query);
			
			prepStatement.setLong(1, id);
			
			if (lang != null)
			{
				prepStatement.setString(2, lang);
			}

			ResultSet results = prepStatement.executeQuery();

			if (results.first())
			{
				positive = results.getInt("positive");
				neutral = results.getInt("neutral");
				negative = results.getInt("negative");
			}
			
			results = null;
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}

		return new SentimentData(id, positive, neutral, negative);
	}


    public TweetTexts getTagCloud(Long id, String lang, Long count) throws SQLException {
               return getTagCloud(id, lang, count, null, true);
       }
       
    public TweetTexts getTagCloud(Long id, String lang, Long count, String start, Boolean order) throws SQLException {

		TweetTexts result = new TweetTexts();

		try 
		{
			
			
			String query = "SELECT tweets.text " + "FROM ("
					+ "SELECT tweets_id "
					+ "FROM tweets_has_search_terms AS thst "
					+ "WHERE thst.search_terms_id = ? "
					+ "AND thst.is_retweet_of_id IS NULL "
					+ ((lang != null) ? "AND thst.iso_language_code = ? " : "")
					+ ((start != null) ? "AND thst.created_at BETWEEN ? AND ADDTIME( ? , '1 0:0:0' ) " : "")
					+ (order ? "ORDER BY thst.retweet_count DESC " : "")
					+ "LIMIT ?) AS ids " + "INNER JOIN tweets "
					+ "ON tweets.id = ids.tweets_id";
			
//			String query = "select tweets.text "
//					+ "from tweets "
//					+ "inner join tweets_has_search_terms on tweets.id = tweets_has_search_terms.tweets_id "
//					+ "where tweets_has_search_terms.search_terms_id = ? "
//					+ "and tweets.is_retweet_of_id is null "
//					+ ((lang != null) ? "and tweets.iso_language_code = ? " : "")
//					 + ((start != null) ? "and tweets.created_at BETWEEN ? AND ADDTIME( ? , '1 0:0:0' ) " : "")
//					+ (order?"order by tweets.retweet_count desc ":"") 
//					+ "limit ?;";
			
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(query);
			prepStatement.setLong(1, id);
			int startindex = 2 + ((lang != null) ? 1 : 0);
			int countindex = startindex + ((start != null) ? 2 : 0);
			if (lang != null) {
				prepStatement.setString(2, lang);
			}
			if (start != null) {
				prepStatement.setString(startindex, start);
				prepStatement.setString(startindex + 1, start);
			}
			prepStatement.setLong(countindex, count);

			ResultSet results = prepStatement.executeQuery();

			int iNumRows = 0;
			String text = "";

			while (results.next())
			{
				text += results.getString("text") + " ";
				iNumRows++;
			}
			
			results = null;

			result.setText(text);
			result.setCount(Math.min(iNumRows, count));
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}

		return result;
	}

	// Query Logic

	/**
	 * Adds a search term to the database, to be processed by the daemon.
	 * 
	 * @param term
	 *            String that should be looked for
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public void saveSearchTerm(String term) throws SQLException
	{
		try
		{
	
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			
			prepStatement = connect
					.prepareStatement("insert into search_terms (term, active, current_start, old_start, interval_length, time_last_fetched, last_fetched_tweet_id, last_fetched_tweet_count, when_created)"
							+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?);");
			
			DateTime now = new DateTime(DateTimeZone.UTC);
			
			String sNow = now.toString();
			sNow = sNow.replace("T", " ");
			if (sNow.indexOf(".") > -1)
				sNow = sNow.substring(0, sNow.indexOf("."));
			
			//System.out.println("SQL UTC time: " + Timestamp.valueOf(sNow).toString());
			
			prepStatement.setString(1, term);
			prepStatement.setBoolean(2, true);
			prepStatement.setTimestamp(3, Timestamp.valueOf(sNow));
			prepStatement.setNull(4, java.sql.Types.NULL);
			prepStatement.setTime(5, Time.valueOf("00:15:00"));
			prepStatement.setNull(6, java.sql.Types.NULL);
			prepStatement.setNull(7, java.sql.Types.NULL);
			prepStatement.setNull(8, java.sql.Types.NULL);
			prepStatement.setTimestamp(9, Timestamp.valueOf(sNow));

			prepStatement.execute();
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Checks if a specific search term already exists.
	 * 
	 * @param term
	 *            String that should be looked for
	 * @return Boolean, true if the search term already exists in the database
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public boolean containsSearchTerm(String term) throws SQLException {
		boolean bContains = false;

		try {
			ResultSet results = null;
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			
			prepStatement = connect
					.prepareStatement("select count(*) as count from search_terms where term = ?;");
			prepStatement.setString(1, term);

			results = prepStatement.executeQuery();
			results.first();
			bContains = (results.getInt("count") > 0);
			
			results = null;
		} catch (SQLException e) {
			// e.printStackTrace();
			throw e;
		} finally {
			close();
		}

		return bContains;
	}

	/**
	 * Returns at most 5 search terms and the number of associated tweets
	 * matching the specified string.
	 * 
	 * @param term
	 *            specified string to be matched
	 * @return List of search terms
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public List<QueryWithOccurence> getMatchingSearchTerms(String term)
			throws SQLException {
		return getMatchingSearchTerms(term, 5);
	}

	/**
	 * Returns a list of search terms and the number of associated tweets
	 * matching the specified string. The number of list items is limited by a
	 * specified number.
	 * 
	 * @param term
	 *            specified string to be matched
	 * @param iNumMatches
	 *            item limit
	 * @returnList of search terms
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public List<QueryWithOccurence> getMatchingSearchTerms(String term,
			int iNumMatches) throws SQLException {
		ArrayList<QueryWithOccurence> searchTerms = new ArrayList<QueryWithOccurence>();

		try {
			
			
			ResultSet results = null;
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("select search_terms.term, search_terms.id, count(*) as count from tweets_has_search_terms, search_terms "
							+ "where tweets_has_search_terms.search_terms_id = search_terms.id and locate(?, search_terms.term) = 1 "
							+ "group by search_terms.term order by count desc, search_terms.term asc;");
			prepStatement.setString(1, term);

			results = prepStatement.executeQuery();

			if (results.first()) {
				for (int i = 0; i < iNumMatches; i++) {
					searchTerms.add(new QueryWithOccurence(results
							.getLong("id"), results.getString("term"), results
							.getInt("count")));

					if (!results.next()) {
						break;
					}
				}
			}
			
			results = null;

		} catch (SQLException e) {
			// e.printStackTrace();
			throw e;
		} finally {
			close();
		}

		return searchTerms;
	}

	/**
	 * Returns a single search term matching a specified string
	 * 
	 * @param param
	 *            specified String
	 * @return search term
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public Query getSearchTerms(String param) throws SQLException {

		Query query = new Query();

		try {
			
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("select id, term from search_terms where term = ?");
			prepStatement.setString(1, param);
			ResultSet resultSetQueries = prepStatement.executeQuery();

			while (resultSetQueries.next()) {
				query.setId(resultSetQueries.getLong("id"));
				query.setString(resultSetQueries.getString("term"));
			}
			
			resultSetQueries = null;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

		return query;
	}

	/**
	 * Returns a single search term matching a specified index
	 * 
	 * @param param
	 *            specified index
	 * @return search term
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public Query getSearchTerms(Long param) throws SQLException {

		Query query = new Query();

		try {
			
			
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("select id, term from search_terms where id = ?");
			prepStatement.setLong(1, param);
			ResultSet resultSetQueries = prepStatement.executeQuery();

			while (resultSetQueries.next()) {
				query.setId(resultSetQueries.getLong("id"));
				query.setString(resultSetQueries.getString("term"));
			}
			
			resultSetQueries = null;

		} catch (SQLException e) {
			throw e;
		} finally {
			close();
		}

		return query;
	}

	/**
	 * Returns the count per Date for a specified search term
	 * 
	 * @param id specified search term index
	 * @param lang just select languages with this iso language code. All languages are selected if this parameter is null.
	 * @param sentiment Just select given sentiment. Should be either "positive", "neutral" or "negative". All sentiments are selected if this parameter is null.
	 * @return DTO containing a list of dates and a list of counts
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public SearchTermsPerQueryPerDate getCountPerHour(Long id, String lang) throws SQLException {

		SearchTermsPerQueryPerDate result = new SearchTermsPerQueryPerDate();

		try
		{
			
			
			String sqlQuery = "SELECT COUNT(tweets_id) AS count, created_at AS moment "
							+ "FROM  tweets_has_search_terms "
							+ "WHERE search_terms_id = ? "
							+ ((lang != null) ? "AND iso_language_code = ? " : "")
							+ "GROUP BY YEAR(moment), MONTH(moment), DAY(moment), HOUR(moment) "
							+ "ORDER BY YEAR(moment), MONTH(moment), DAY(moment), HOUR(moment);";
			
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(sqlQuery);
			prepStatement.setLong(1, id);
			
			if (lang != null)
			{
				prepStatement.setString(2, lang);
			}
			
			ResultSet resultSet = prepStatement.executeQuery();

			ArrayList<Integer> counts = new ArrayList<Integer>();
			ArrayList<LocalDateTime> dates = new ArrayList<LocalDateTime>();
			Query query = new Query();

			if (resultSet.first())
			{
				counts.add(resultSet.getInt("count"));
				
				java.sql.Timestamp tempSQLDate = resultSet.getTimestamp("moment");
				LocalDateTime date = new LocalDateTime(tempSQLDate.getTime());

				dates.add(date);

				while (resultSet.next())
				{
					counts.add(resultSet.getInt("count"));
					tempSQLDate = resultSet.getTimestamp("moment");

					date = new LocalDateTime(tempSQLDate.getTime());

					dates.add(date);
				}
				result.setCounts(counts);
				result.setDates(dates);

			}
			
			resultSet = null;
			
			prepStatement = connect
					.prepareStatement("select id, term from search_terms where id = ?");
			prepStatement.setLong(1, id);
			ResultSet resultSetQueries = prepStatement.executeQuery();

			if(resultSetQueries.first()) {
				query.setId(resultSetQueries.getLong("id"));
				query.setString(resultSetQueries.getString("term"));
				result.setQuery(query);
			}
			else
			{
				result.setQuery(new Query(id, null));
			}
			
			resultSetQueries = null;
			
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}

		return result;
	}
	
	/**
	 * Returns count per Hour for positive and negative tweets seperately. Ignores neutral tweets.
	 * 
	 * @param id specified search term index
	 * @param lang iso language code of the language (all languages are selected if this parameter is null)
	 * @return DTO containing a list of dates and a list of counts
	 * @throws SQLException thrown if a SQL-Error occurs
	 */
	public SentimentPerQueryPerDate getSentimentPerHour(Long id, String lang) throws SQLException {
		SentimentPerQueryPerDate result = new SentimentPerQueryPerDate();
		SearchTermsPerQueryPerDate positiveResults = new SearchTermsPerQueryPerDate();
		SearchTermsPerQueryPerDate negativeResults = new SearchTermsPerQueryPerDate();
		
		try
		{
			
			
			String sqlQuery = "select sum(if(tweets_has_search_terms.sentiment >= ?, 1, 0)) as positive, "
					        + "sum(if(tweets_has_search_terms.sentiment <= ?, 1, 0)) as negative, "
					        + "created_at as moment, "
					        + "search_terms_id "
					        + "from tweets_has_search_terms "
					        + "where search_terms_id = ? "
					        + ((lang != null) ? "and tweets_has_search_terms.iso_language_code = ? " : "")
					        + "group by year(moment), month(moment), day(moment), hour(moment) "
					        + "order by year(moment), month(moment), day(moment), hour(moment);";
			
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(sqlQuery);
			
			prepStatement.setFloat(1, RestUtil.SENTIMENT_UPPER_BORDER);
			prepStatement.setFloat(2, RestUtil.SENTIMENT_LOWER_BORDER);
			prepStatement.setLong(3, id);
			
			if (lang != null)
			{
				prepStatement.setString(4, lang);
			}
			
			ResultSet resultSet = prepStatement.executeQuery();

			ArrayList<Integer> positiveCounts = new ArrayList<Integer>();
			ArrayList<Integer> negativeCounts = new ArrayList<Integer>();
			ArrayList<LocalDateTime> dates = new ArrayList<LocalDateTime>();
			Query query = new Query();
			
			if (resultSet.first())
			{
				positiveCounts.add(resultSet.getInt("positive"));
				negativeCounts.add(resultSet.getInt("negative"));
				
				java.sql.Timestamp tempSQLTimestamp = resultSet.getTimestamp("moment");
				LocalDateTime date = new LocalDateTime(tempSQLTimestamp.getTime());
				
				dates.add(date);
				
				while (resultSet.next())
				{
					positiveCounts.add(resultSet.getInt("positive"));
					negativeCounts.add(resultSet.getInt("negative"));
					
					tempSQLTimestamp = resultSet.getTimestamp("moment");
					date = new LocalDateTime(tempSQLTimestamp.getTime());
					
					dates.add(date);
				}
				
				positiveResults.setCounts(positiveCounts);
				positiveResults.setDates(dates);
				
				negativeResults.setCounts(negativeCounts);
				negativeResults.setDates(dates);
			}
			
			result.setPositiveCounts(positiveResults);
			result.setNegativeCounts(negativeResults);
			
			resultSet = null;
			
			prepStatement = connect
					.prepareStatement("select id, term from search_terms where id = ?");
			prepStatement.setLong(1, id);
			ResultSet resultSetQueries = prepStatement.executeQuery();

			if(resultSetQueries.first()) {
				query.setId(resultSetQueries.getLong("id"));
				query.setString(resultSetQueries.getString("term"));
				positiveResults.setQuery(query);
				negativeResults.setQuery(query);
			}
			else
			{
				query = new Query(id, "");
				positiveResults.setQuery(query);
				negativeResults.setQuery(query);
			}
			
			resultSetQueries = null;
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}

		return result;
	}

	/**
	 * Returns some meta information regarding a specified search term.
	 * 
	 * @param id
	 *            specified search term index
	 * @return DTO containing the maximum and minimum dates and the count of
	 *         search results connected to the specified search term.
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public QueryMetadata getMetadataForQuery(Long id, String lang) throws SQLException
	{
		QueryMetadata result = new QueryMetadata();
		result.setQuery(new Query(id, null));
		
		if (lang != null)
		{
			result.setLanguage(lang);
		}

		try
		{

			
			String queryString = "select count(*) as count, max(created_at) as newest, min(created_at) as oldest "
					     + "from tweets_has_search_terms "
					     + "where search_terms_id = ? "
					     + ((lang != null) ? "and iso_language_code = ?" : "")
					     + ";";
			
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(queryString);

			prepStatement.setLong(1, id);
			
			if (lang != null)
			{
				prepStatement.setString(2, lang);
			}
			
			ResultSet resultSet = prepStatement.executeQuery();
			
			if (resultSet.first())
			{
				int iCount = resultSet.getInt("count");
				
				if (iCount > 0)
				{
					result.setOccurence(iCount);
					result.setNewestTweet(resultSet.getDate("newest"));
					result.setOldestTweet(resultSet.getDate("oldest"));
				}
			}
			
			resultSet = null;
			
			prepStatement = connect
					.prepareStatement("select id, term from search_terms where id = ?");
			prepStatement.setLong(1, id);
			ResultSet resultSetQueries = prepStatement.executeQuery();
			Query query = new Query();
			
			if(resultSetQueries.first()) {
				query.setId(resultSetQueries.getLong("id"));
				query.setString(resultSetQueries.getString("term"));
				result.setQuery(query);
			}
			else
			{
				query = new Query(id, null);
				result.setQuery(query);
			}
			
			resultSetQueries = null;
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}

		return result;
	}

	/**
	 * Returns hashtags that were used by authors of the tweets and their
	 * frequency of occurance.
	 * 
	 * @param id
	 *            Search term id
	 * @return Hashtags that were used by authors of the tweets and their
	 *         frequency of occurance
	 */
	public HashtagStatisticsForSearchTermId getHashtagStatisticsForSearchTermId(Long id, String lang, Long limit) throws SQLException {
		HashtagStatisticsForSearchTermId result = new HashtagStatisticsForSearchTermId();
		result.setSearchTermId(id);

		try {
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			String searchTerm = null;

			//read the search
			prepStatement = connect.prepareStatement(
					"SELECT "
                  + "s.term "
                  + "FROM "
                  + "search_terms AS s "
                  + "WHERE s.id = ? ;");
			
			prepStatement.setLong(1, id);
			ResultSet resultSet = prepStatement.executeQuery();

			if (resultSet.next()) {
				searchTerm = resultSet.getString(1);
			}

			/*
			// read hashtag statistic
			prepStatement = connect
					.prepareStatement("SELECT "
							+ "thh.hashtags_id, "
							+ "h.text, "
							+ "COUNT(*) AS count "
							+ "FROM tweets_has_search_terms  AS t "
							+ "LEFT JOIN tweets_has_hashtags AS thh ON thh.tweets_id = t.tweets_id "
							+ "RIGHT JOIN hashtags           AS h   ON h.id = thh.hashtags_id "
							+ "WHERE t.search_terms_id = ? "
							+ "GROUP BY thh.hashtags_id "
							+ "ORDER BY count DESC;");

			}*/
			
			/* POTENTIAL VIEW
			* CREATE VIEW hashtag_tweets
			* AS SELECT thh.hashtags_id, h.text, 
			* FROM tweets_has_search_terms AS ths
			* INNER JOIN tweets_has_hashtags AS thh ON thh.tweets_id = ths.tweets_id
			* INNER JOIN hashtags AS h ON h.id = thh.hashtags_id
			* 
			*/
			
			//read hashtag statistic
			prepStatement = connect.prepareStatement(
					"SELECT "
                  + "thh.hashtags_id, "
                  + "h.text, "
                  + "COUNT(*) AS count "
                  + "FROM tweets_has_search_terms  AS ths "
                  + "INNER JOIN tweets_has_hashtags AS thh ON thh.tweets_id = ths.tweets_id "
                  + "INNER JOIN hashtags           AS h   ON h.id = thh.hashtags_id "
                  + ((lang != null) ? "INNER JOIN tweets AS t ON ths.tweets_id = t.id " : "")
                  + "WHERE ths.search_terms_id = ? AND h.text != ? AND h.text != ? " 
                  + ((lang != null) ? "AND t.iso_language_code = ? " : "")
                  + "GROUP BY thh.hashtags_id " 
                  + "ORDER BY count DESC "
                  + "LIMIT ?;");
			
			prepStatement.setLong(1, id);
			prepStatement.setString(2, searchTerm);
			prepStatement.setString(3, searchTerm.replace("#", ""));
			
			if (lang != null)
			{
				prepStatement.setString(4, lang);
				prepStatement.setLong(5, limit);
			}
			else
			{
				prepStatement.setLong(4, limit);
			}
			
			resultSet = prepStatement.executeQuery();

			ArrayList<String> hashtagIds = new ArrayList<String>();
			ArrayList<String> hashtagTexts = new ArrayList<String>();
			ArrayList<Integer> counts = new ArrayList<Integer>();

			while (resultSet.next()) {
				hashtagIds.add(resultSet.getString(1));
				hashtagTexts.add(resultSet.getString(2));
				counts.add(resultSet.getInt(3));
			}

			result.setHashtagIds(hashtagIds);
			result.setHashtagTexts(hashtagTexts);
			result.setCounts(counts);
			
			resultSet = null;

		} catch (SQLException e) {
			throw e;
		} finally {
			close();
		}

		return result;
	}

	/**
	 * Returns a list of at most 5 tweet ids of tweets connected to
	 * 
	 * @param search
	 *            term id
	 * @return search term tweets DTO
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public ArrayList<TweetWithUser> getSearchTermTweets(Long id, float sentTop, float sentBottom, String start, String end, String lang, Long hashTagId, int limit) throws SQLException
	{
		ArrayList<TweetWithUser> tweets = new ArrayList<TweetWithUser>();
		
		int index = 1;
		
		try 
		{
			String query = "select search_terms_id, tweets.id as tweet_id, coordinates_longitude, coordinates_latitude, tweets.iso_language_code, tweets.retweet_count, tweets.sentiment as sentiment, tweets.created_at as tweet_created_at, tweets.text, users.id as user_id, name, screen_name "
						 + "from tweets_has_search_terms "
						 + "inner join tweets on tweets.id = tweets_has_search_terms.tweets_id "
						 + "inner join users on users.id = tweets.users_id "
						 + ((hashTagId != null) ? "inner join tweets_has_hashtags on tweets.id = tweets_has_hashtags.tweets_id " : "")
						 + "where search_terms_id = ? "
						 + "and tweets.is_retweet_of_id is null "
						 + ((sentTop < 1 || sentBottom > -1) ? "and tweets.sentiment >= ? and tweets.sentiment <= ? " : "")
						 + ((end != null) ? "and tweets.created_at <= ? " : "")
						 + ((start != null) ? "and tweets.created_at >= ? " : "")
						 + ((lang != null) ? "and tweets.iso_language_code = ? " : "")
						 + ((hashTagId != null) ? "and tweets_has_hashtags.hashtags_id = ? " : "")
						 + "order by tweets.retweet_count desc "
						 + "limit ?";
					
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement(query);
			
			prepStatement.setLong(index++, id);

			if (sentTop < 1 || sentBottom > -1)
			{
				prepStatement.setFloat(index++, sentBottom);
				prepStatement.setFloat(index++, sentTop);
			}
			
			if (end != null)
			{
				prepStatement.setString(index++, end);
			}
			
			if (start != null)
			{
				prepStatement.setString(index++, start);
			}
			
			if (lang != null)
			{
				prepStatement.setString(index++, lang);
			}
			
			if(hashTagId != null)
			{
				prepStatement.setLong(index++, hashTagId);
			}
			
			prepStatement.setInt(index++, limit);
			
			ResultSet resultSetQueries = prepStatement.executeQuery();

			while (resultSetQueries.next()) 
			{		
				TweetWithUser tweetWithUser = new TweetWithUser();
				TweetBasic tweet = new TweetBasic();
				TwitterUserBasic user = new TwitterUserBasic();
				
				Language language = new Language(resultSetQueries.getString("iso_language_code"));
				language.setOutputString(RestUtil.getEnglishLanguageString(resultSetQueries.getString("iso_language_code")));
				Sentiment sentiment = new Sentiment(resultSetQueries.getFloat("sentiment"));
				sentiment.setOutputString(RestUtil.getSentimentString(resultSetQueries.getFloat("sentiment")));
				
				tweet.setId(resultSetQueries.getString("tweet_id"));
				tweet.setCoordinateLongitude(resultSetQueries.getFloat("coordinates_longitude"));
				tweet.setCoordinateLatitude(resultSetQueries.getFloat("coordinates_latitude"));
				tweet.setCreatedAt(resultSetQueries.getString("tweet_created_at"));
				tweet.setText(resultSetQueries.getString("text"));
				tweet.setLang(language);
				tweet.setRetweetCount(resultSetQueries.getInt("retweet_count"));
				tweet.setSentiment(sentiment);
				
				user.setId(resultSetQueries.getString("user_id"));
				user.setName(resultSetQueries.getString("name"));
				user.setScreenName(resultSetQueries.getString("screen_name"));

				tweetWithUser.setTweet(tweet);
				tweetWithUser.setUser(user);

				tweets.add(tweetWithUser);		
			}

			resultSetQueries = null;
		} 
		catch (Exception e)
		{
			throw e;
		} 
		finally
		{
			close();
		}

		return tweets;
	}
	
	/**
	 * Returns tweet specified by index
	 * 
	 * @param tweet
	 *            id
	 * @return tweet DTO
	 * @throws SQLException
	 *             thrown if a SQL-Error occurs
	 */
	public Tweet getTweet(String id) throws SQLException {
		Tweet tweet = null;
		try {
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("select * from tweets where id = ? LIMIT 1;");
			prepStatement.setString(1, id);
			ResultSet result = prepStatement.executeQuery();

			if(result.first())
			{
				tweet = new Tweet();
				
				tweet.setId(id);
				tweet.setCoordinateLongitude(result.getFloat("coordinates_longitude"));
				tweet.setCoordinateLatitude(result.getFloat("coordinates_latitude"));
				tweet.setCreatedAt((new LocalDate(result.getDate("created_at").getTime())).toString());
				tweet.setText(result.getString("text"));
				tweet.setLang(new Language(result.getString("iso_language_code")));
				tweet.setRetweetCount(result.getInt("retweet_count"));
				tweet.setSentiment(new Sentiment(result.getFloat("sentiment")));
				tweet.setUserId(result.getString("users_id"));
				tweet.setRetweetId(result.getString("is_retweet_of_id"));
				tweet.setReplyId(result.getString("is_reply_to_status_id"));
				tweet.setSource(result.getString("source"));
			}
			
			result = null;
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
		return tweet;
	}

	public TwitterUser getUser(String id) throws SQLException {
		TwitterUser user = null;
		try {
			connect = DriverManager.getConnection(dbUrl);
			
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("select * from users where id = ? LIMIT 1;");
			prepStatement.setString(1, id);
			ResultSet result = prepStatement.executeQuery();

			if (result.first()) {
				user = new TwitterUser();
				
				user.setId(result.getString("id"));
				user.setName(result.getString("name"));
				user.setScreenName(result.getString("screen_name"));
				user.setProfileImageUrl(result.getString("profile_image_url"));
				user.setLocation(result.getString("location"));
				user.setUrl(result.getString("url"));
				user.setLang(result.getString("lang"));
				user.setFollowersCount(result.getInt("followers_count"));
				user.setVerified(result.getInt("verified"));
				user.setTimeZone(result.getString("time_zone"));
				user.setDescription(result.getString("description"));
				user.setStatusesCount(result.getInt("statuses_count"));
				user.setFriendsCount(result.getInt("friends_count"));
				user.setCreatedAt(result.getString("created_at"));
			}
			
			result = null;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
		return user;
	}

	public ArrayList<LanguageCount> getLanguages(Long id) throws SQLException
	{
		ArrayList<LanguageCount> result = new ArrayList<LanguageCount>();
		
		try
		{
			String query = "select iso_language_code, count(*) as count "
					 	 + "from tweets_has_search_terms "
					 	 + "where tweets_has_search_terms.search_terms_id = ? "
					 	 + "group by iso_language_code "
					 	 + "order by count desc ";
			
			connect = DriverManager.getConnection(dbUrl);
			prepStatement = connect.prepareStatement(query);
			
			prepStatement.setLong(1, id);
			
			ResultSet results = prepStatement.executeQuery();
			
			while (results.next())
			{
				LanguageCount languageCount = new LanguageCount();
				
				languageCount.setIsoCode(results.getString("iso_language_code"));
				languageCount.setCount(results.getInt("count"));
				
				result.add(languageCount);
			}
			
			results = null;
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}
		
		return result;
	}

	/**
	 * Returns some status information from the search terms table. See DTO DaemonStatus for more information.
	 * 
	 * @return DaemonStatus DTO
	 * @throws SQLException thrown if an SQL error occurs
	 */
	public DaemonStatus getDaemonStatus() throws SQLException
	{
		DaemonStatus status = new DaemonStatus();
		try {
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
				.prepareStatement("SELECT COUNT(id) AS totalCount, SUM(active) AS activeCount FROM search_terms LIMIT 1;");
			ResultSet result = prepStatement.executeQuery();

			if (result.first()) {
				status.setTotalCount(result.getInt("totalCount"));
				status.setActiveCount(result.getInt("activeCount"));
			}
			
			prepStatement = connect
				.prepareStatement("SELECT id, term, active, priority, interval_length,"
						+ " time_last_fetched, when_created, last_fetched_tweet_id IS NOT NULL AS in_iteration"
						+ " FROM search_terms;");
			result = prepStatement.executeQuery();

			ArrayList<SearchTermStatus> terms = new ArrayList<>();
			
			while (result.next())
			{
				SearchTermStatus curTerm = new SearchTermStatus();
				
				curTerm.setId(result.getInt("id"));
				curTerm.setName(result.getString("term"));
				curTerm.setActive(result.getBoolean("active"));
				curTerm.setPriority(result.getInt("priority"));
				curTerm.setTimeLastFetched(result.getString("time_last_fetched"));
				curTerm.setCreatedAt(result.getString("when_created"));
				//this fucking shit is needed because jdbc implicitly tries to convert a string to time when calling getString().....
				curTerm.setIntervalLength(new String(result.getBytes("interval_length"))); 
				curTerm.setInIteration(result.getBoolean("in_iteration"));
				terms.add(curTerm);
			}
			
			result = null;
			
			status.setSearchTerms(terms);
		} finally {
			close();
		}
		return status;
	}

	/**
	 * Set a new user priority for a given search term 
	 * 
	 * @param id search term id
	 * @param newPriority new priority to be saved for the given search term id
	 * @throws SQLException thrown if an SQL error occurs
	 */
	public void savePriority(Integer id, Integer newPriority) throws SQLException 
	{
		try
		{
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("UPDATE search_terms SET priority = ? WHERE id = ? LIMIT 1;");
			
			prepStatement.setInt(1, newPriority);
			prepStatement.setInt(2, id);

			prepStatement.execute();
		}
		finally
		{
			close();
		}
	}

	/**
	 * Activates or deactivates a search term for the daemon by setting the active flag for this search term to true or false 
	 * 
	 * @param id search term id
	 * @param newActiveFlag the new active flag saved for the given search term id
	 * @throws SQLException thrown if an SQL error occurs
	 */
	public void saveActiveFlag(Integer id, Boolean newActiveFlag) throws SQLException {
		try
		{
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("UPDATE search_terms SET active = ? WHERE id = ? LIMIT 1;");
			
			prepStatement.setBoolean(1, newActiveFlag);
			prepStatement.setInt(2, id);

			prepStatement.execute();
		}
		finally
		{
			close();
		}
	}

	/**
	 * @return Boolean. True if the Daemon already fetched this search term at least once. False otherwise.
	 * @throws SQLException thrown if an SQL error occurs
	 */
	public Boolean hasDaemonFetched(Integer id) throws SQLException {
		try
		{
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect
					.prepareStatement("SELECT old_start, last_fetched_tweet_id FROM search_terms WHERE id = ? LIMIT 1;");
			
			prepStatement.setInt(1, id);

			ResultSet result = prepStatement.executeQuery();
			
			if (result.first()) {
				//Check whether last_fetched_tweet_id was null
				result.getString("last_fetched_tweet_id");
				if (result.wasNull()) {
					//Check whether old_start was null
					result.getTimestamp("old_start");
					if (result.wasNull()) {
						//If both were null the daemon didn't fetch yet
						result = null;
						return false;
					}
				}
				
				//If one of the checked parameters was NOT null the daemon did its job!
				result = null;
				return true;
			}
			
			result = null;
			
			return null;
		}
		finally
		{
			close();
		}
	}
	
	/**
	 * @return String. Timestamp of the last time the Daemon has fetched tweets from Twitter
	 * @throws SQLException if an SQL error occurs
	 */
	public String getTimeLastFetched(Long id) throws SQLException
	{
		try
		{
			connect = DriverManager.getConnection(dbUrl);
			readStatement = connect.prepareStatement(readQuery);
			readStatement.execute(); 
			
			prepStatement = connect.prepareStatement("SELECT time_last_fetched FROM search_terms WHERE id = ?;");
			
			prepStatement.setLong(1, id);
			
			ResultSet result = prepStatement.executeQuery();
			
			if (result.first())
			{
				//String string = result.getString("time_last_fetched");
				
				if (result.getTimestamp("time_last_fetched") != null) {
					Date date = result.getTimestamp("time_last_fetched");
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss z");
					dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
					String string = dateFormat.format(date);
					if (string != null) {
						return string;
					}
				}
			}
			
			result = null;
			
			return "";
		}
		finally
		{
			close();
		}
	}
	
}
