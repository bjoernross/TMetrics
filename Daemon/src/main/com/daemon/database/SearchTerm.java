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
package com.daemon.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import com.tmetrics.util.HashCodeUtil;
import com.tmetrics.util.Localization;

/**
 * Represents a search term to be saved to and loaded from the database for
 * the ease of use.
 * The methods for loading and inserting a search term are marked static.
 * @author Torsten, Jens
 */
public class SearchTerm {

	public static String DATABASE_PROPERTY_PATH = System.getProperty("user.home") + "/database.properties";
	
	// Used for converting SQL Time to joda-time Duration
	public static final PeriodFormatter DURATION_FORMATTER = new PeriodFormatterBuilder()
			.appendHours()
			.appendSeparator(":")
			.appendMinutes()
			.appendSeparator(":")
			.appendSeconds()
			.toFormatter();
	
	private Integer _id = null;
	private String _term = null;
	private boolean _isActive = true;
	private DateTime _current_start = null;
	private DateTime _old_start = null;
	private Duration _interval_length = null;
	private Long _lastFetchedTweetId = null;
	private Integer _priority = null;
	private DateTime _timeLastFetched = null;
	private Integer _lastFetchedTweetCount = null;
	private DateTime _whenCreated = null;
	
	/**
	 * Creates a new search term object with default values.
	 * @param name The name of the search term object.
	 * @param currentStart The date of the current start / when the search term was created.
	 */
	public SearchTerm(String name, DateTime currentStart) {
		_term = name;
		_current_start = currentStart;
		
		// Default value for interval length is 15 min.
		_interval_length = DURATION_FORMATTER.parsePeriod("00:15:00").toStandardDuration();
		// Default value for _priority is 0(normal priority)
		_priority = 0;
		
		_whenCreated = currentStart;
	}
	
	/**
	 * Creates a search term object by converting it from a result set
	 * 
	 * @param resultSet ResultSet to create search term object from
	 * @throws SQLException Thrown if there is a MySQL problem
	 */
	public SearchTerm(ResultSet resultSet) throws SQLException {
		_id = resultSet.getInt(1);
		_term = resultSet.getString(2);
		_isActive = resultSet.getBoolean(3);
		_current_start = new DateTime(resultSet.getTimestamp(4, Localization.UTC).getTime());
		Timestamp oldStart = resultSet.getTimestamp(5, Localization.UTC);
		if (resultSet.wasNull()) {
			_old_start = null;
		}
		else {
			_old_start = new DateTime(oldStart.getTime());
		}

		// Dirty hack: Because we cannot get every time via getTimestamp, we have to get the byte stream
		// and convert it into a string time.
		byte[] arr = resultSet.getBytes(6);
		String intervalLength = new String(arr);
		_interval_length = Localization.DURATION_FORMATTER.parsePeriod(intervalLength).toStandardDuration();
		
		_priority = resultSet.getInt(7);
		if (resultSet.wasNull())
			_priority = null;
		Timestamp timeLastFetched = resultSet.getTimestamp(8, Localization.UTC);
		if (resultSet.wasNull()) {
			_timeLastFetched = null;
		}
		else {
			_timeLastFetched = new DateTime(timeLastFetched.getTime());
		}
		_lastFetchedTweetId = resultSet.getLong(9);
		if (resultSet.wasNull())
			_lastFetchedTweetId = null;
		_lastFetchedTweetCount = resultSet.getInt(10);
		if (resultSet.wasNull())
			_lastFetchedTweetCount = null;
		_whenCreated = new DateTime(resultSet.getTimestamp(11, Localization.UTC).getTime());
	}
	
	/**
	 * Copy constructor.
	 */
	public SearchTerm(SearchTerm term) {
		_id = term._id;
		_term = term._term;
		_isActive = term._isActive;
		_current_start = new DateTime(term._current_start.getMillis());
		if (term._old_start != null)
			_old_start = new DateTime(term._old_start.getMillis());
		_interval_length = new Duration(term._interval_length.getMillis());
		_lastFetchedTweetId = term._lastFetchedTweetId;
		_priority = term._priority;
		if (term._timeLastFetched != null)
			_timeLastFetched = new DateTime(term._timeLastFetched.getMillis());
		_lastFetchedTweetCount = term._lastFetchedTweetCount;
		_whenCreated = new DateTime(term._whenCreated.getMillis());
	}

	/**
	 * Returns the Id of the search term object.
	 * @return Returns the Id of the search term object.
	 * @throws Exception Thrown when this object does not have an Id, yet.
	 */
	public int getId() throws Exception {
		if (_id == null) {
			throw new Exception("This search term object does not have an Id, yet.");
		}
		
		return _id;
	}
	
	/**
	 * Return the name of the search term object.
	 * @return Return the name of the search term object.
	 */
	public String getTerm() {
		return _term;
	}

	/**
	 * Sets the name of the search term object.
	 * @return Sets the name of the search term object.
	 */
	public void setTerm(String term) {
		_term = term;
	}
	
	/**
	 * Returns whether the search term is active or not.
	 * @return Returns whether the search term is active or not.
	 */
	public boolean isActive() {
		return _isActive;
	}
	
	/**
	 * Sets whether the search term shall be marked as active or not.
	 * @param isActive The activity of the search term.
	 */
	public void setActive(boolean isActive) {
		_isActive = isActive;
	}
	
	
	/**
	 * Returns the Id of the oldest fetched tweet.
	 * @return Returns the Id of the oldest fetched tweet for future searches up to this
	 * Id.
	 */
	public Long getLastFetchedTweetId() {
		return _lastFetchedTweetId;
	}
	
	/**
	 * Sets the Id of the last fetched oldest tweet for this search term.
	 * @param lastFetchedTweetId The Id of the last fetched oldest tweet.
	 */
	public void setLastFetchedTweetId(Long lastFetchedTweetId) {
		_lastFetchedTweetId = lastFetchedTweetId;
	}
	
	
	/**
	 * Gets the user priority. Influences the factor the interval length
	 * will get multiplied by. Corresponding Values are in the Daemon properties
	 * @return Returns the user-set priority. Valid Values: -2, -1, 0, 1, 2. Default: 0
	 */
	public Integer getPriority() {
		return _priority;
	}
	
	/**
	 * Sets the user priority. Influences the factor the interval length
	 * will get multiplied by. Corresponding Values are in the Daemon properties
	 * 
	 * @param priority The user set priority. Valid Values are -2, -1, 0, 1, 2,
	 * and 0 is default i.e. no special priority. 
	 */
	public void setPriority(Integer priority) {
		_priority = priority;
	}
	
	/**
	 * Returns the time we last searched for this term.
	 * @return Returns the time we last searched for this term as DateTime.
	 */
	public DateTime getTimeLastFetched() {
		return _timeLastFetched;
	}
	
	/**
	 * Sets the time we last searched for this term.
	 * @param timeLastFetched The time of our last search for this term as DateTime
	 */
	public void setTimeLastFetched(DateTime timeLastFetched) {
		_timeLastFetched = timeLastFetched;
	}
	
	/**
	 * Returns the number of tweets received within last lookup for this search term.
	 * If the count is >= 100, it means that the Daemon got to many tweets to handle and will
	 * reduce its stepwidth accordingly. Alternatively if the count is far below 100, it
	 * means the daemon can increase its stepwidth to find more tweets within one request.
	 * @return The number of tweets received within last lookup.
	 */
	public Integer getLastFetchedTweetCount() {
		return _lastFetchedTweetCount;
	}
	
	/**
	 * Sets the number of tweets received within last lookup for this search term.
	 * If the count is >= 100, it means that the Daemon got to many tweets to handle and will
	 * reduce its stepwidth accordingly. Alternatively if the count is far below 100, it
	 * means the daemon can increase its stepwidth to find more tweets within one request.
	 * @param lastFetchedTweetCount The number of tweets received within last lookup.
	 */
	public void setLastFetchedTweetCount(Integer lastFetchedTweetCount) {
		_lastFetchedTweetCount = lastFetchedTweetCount;
	}
	
	
	/**
	 * Gets the value of current_start. Not Null.
	 * First set by Rest Service, then updated by the daemon.
	 * @return _current_start Date of the current start
	 */
	public DateTime getCurrentStart() {
		return _current_start;
	}

	/**
	 * Sets the value of the current_start as DateTime
	 * @param _current_start
	 */
	public void setCurrentStart(DateTime current_start) {
		if (_current_start != null){
			this._current_start = current_start;
		}
		else {
			throw new InvalidParameterException("current_start must not be NULL");
		}
	}

	
	/**
	 * Gets the DateTime of the old_start value in the DB
	 * @return _old_start Datetime of the Value of old_start
	 */
	public DateTime getOldStart() {
		return _old_start;
	}
	
	/**
	 * Sets the Date and time (value) of the old_start as DateTime
	 */
	public void setOldStart(DateTime old_start) {
		this._old_start = old_start;
	}

	/**
	 * Gets the value of interval_length as joda Time Duration.
	 * @return interval_length
	 */
	public Duration getIntervalLength() {
		return _interval_length;
	}

	/**
	 * Sets the interval_length as Joda Time Duration
	 * @param interval_length
	 */
	public void setIntervalLength(Duration interval_length) {
		_interval_length = interval_length;
	}		
	
	
	/**
	 * Returns the value of when_created.
	 * Should only set by the Rest Service
	 * @return when_started DateTime
	 */
	public DateTime getWhenCreated() {
		return _whenCreated;
	}
	/**
	 * Sets the when_created Date for a Search Term, Expects a DateTime.
	 */
	public void setWhenCreated(DateTime whenCreated) {
		this._whenCreated = whenCreated;
	}
		
	
	/**
	 * Load a search term object identified by its Id from the database
	 * and in a java search term object. Should only be used by the Tests.
	 * Throws a SQLException if the id is not found in the Database
	 * 
	 * @param id The id identifying the search term object.
	 * @return Returns the correct search term object or null, if there is
	 * no such item identified by Id.
	 */
	public static SearchTerm load(int id) throws SQLException {
		String sqlSelect = "SELECT * FROM search_terms WHERE id = ?";
		
		Connection conn = null;
		PreparedStatement prepStatement = null;
		ResultSet resSet = null;
		
		SearchTerm searchTerm = null;
		FileInputStream fis = null;
		
		try
		{
			Properties props = new Properties();
			fis = new FileInputStream(DATABASE_PROPERTY_PATH);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			
			String dbUrl =   props.getProperty("javabase.jdbc.url")
						   + props.getProperty("database.name") + "?user=" 
						   + props.getProperty("javabase.jdbc.username") +"&password="
						   + props.getProperty("javabase.jdbc.password")
						   + "&useLegacyDatetimeCode=false"
						   + "&serverTimezone=UTC";
			conn = DriverManager.getConnection(dbUrl);
			
			// Get the row
			prepStatement = conn.prepareStatement(sqlSelect);
			prepStatement.setInt(1, id);
			prepStatement.execute();
			resSet = prepStatement.getResultSet();
			resSet.first();

			// Fill the object

			// column - name
			// 2 - term, 4 - current_start
			searchTerm = new SearchTerm(resSet.getString(2), new DateTime(resSet.getTimestamp(4, Localization.UTC).getTime()));
			//1 id
			searchTerm._id = resSet.getInt(1);
			// 3 active
			searchTerm.setActive(resSet.getBoolean(3));
			// 5 old_start
			Timestamp oldStart = resSet.getTimestamp(5, Localization.UTC);
			if (!resSet.wasNull())
				searchTerm.setOldStart(new DateTime(oldStart.getTime()));
			// 6 interval_length

			// Dirty hack: Because we cannot get every time via getTimestamp, we have to get
			// the byte stream and convert it into a string time.
			byte[] arr = resSet.getBytes(6);
			String intervalLength = new String(arr);
			searchTerm.setIntervalLength(new Duration(Localization.DURATION_FORMATTER.parsePeriod(intervalLength).toStandardDuration()));

			// 7 priority
			searchTerm.setPriority(resSet.getInt(7));
			if (resSet.wasNull())
				searchTerm.setPriority(null);
			// 8 time_last_fetched
			Timestamp lastFetched = resSet.getTimestamp(8, Localization.UTC);
			if (!resSet.wasNull())
				searchTerm.setTimeLastFetched(new DateTime(lastFetched.getTime()));
			// 9 last_fetched_tweet_id
			Long lastFetchedTweetId = resSet.getLong(9);
			if (!resSet.wasNull())
				searchTerm.setLastFetchedTweetId(lastFetchedTweetId);
			// 10 last_fetched_tweet_count
			searchTerm.setLastFetchedTweetCount(resSet.getInt(10));
			if (resSet.wasNull())
				searchTerm.setLastFetchedTweetCount(null);
			//11 when_created
			Timestamp when_created = resSet.getTimestamp(11, Localization.UTC);
			searchTerm.setWhenCreated(new DateTime(when_created.getTime()));
				
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (resSet != null)
				resSet.close();

			if (prepStatement != null)
				prepStatement.close();

			if (conn != null)
				conn.close();
		}

		return searchTerm;
	}

	/**
	 * Load a search term object identified by its Id from the database
	 * and in a java search term object. Should only be used by the Tests.
	 * Throws a SQLException if the id is not found in the Database
	 * 
	 * @param term The name identifying the search term object.
	 * @return Returns the correct search term object or null, if there is
	 * no such item identified by term.
	 */
	public static SearchTerm load(String term) throws SQLException {
		String sqlSelect = "SELECT * FROM search_terms WHERE term = ?";
		
		Connection conn = null;
		PreparedStatement prepStatement = null;
		ResultSet resSet = null;
		
		SearchTerm searchTerm = null;
		FileInputStream fis = null;
		
		try
		{
			Properties props = new Properties();
			fis = new FileInputStream(DATABASE_PROPERTY_PATH);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			
			String dbUrl =   props.getProperty("javabase.jdbc.url")
					       + props.getProperty("database.name") + "?user="
						   + props.getProperty("javabase.jdbc.username") +"&password="
						   + props.getProperty("javabase.jdbc.password")
						   + "&useLegacyDatetimeCode=false"
						   + "&serverTimezone=UTC";
			conn = DriverManager.getConnection(dbUrl);
			
			// Get the row
			prepStatement = conn.prepareStatement(sqlSelect);
			prepStatement.setString(1, term);
			prepStatement.execute();
			resSet = prepStatement.getResultSet();
			resSet.first();
			
			// Fill the object
			
			// column - name
			// 2 - term, 4 - current_start
			searchTerm = new SearchTerm(resSet.getString(2), new DateTime(resSet.getTimestamp(4, Localization.UTC).getTime()));
			//1 id
			searchTerm._id = resSet.getInt(1);
			// 3 active
			searchTerm.setActive(resSet.getBoolean(3));
			// 5 old_start
			Timestamp oldStart = resSet.getTimestamp(5, Localization.UTC);
			if (!resSet.wasNull())
				searchTerm.setOldStart(new DateTime(oldStart.getTime()));
			
			// 6 interval length
			
			// Dirty hack: Because we cannot get every time via getTimestamp, we have to get the byte stream
			// and convert them into a string time.
			byte[] arr = resSet.getBytes(6);
			String intervalLength = new String(arr);
			searchTerm.setIntervalLength(new Duration(Localization.DURATION_FORMATTER.parsePeriod(intervalLength).toStandardDuration()));
			
			// 7 priority
			searchTerm.setPriority(resSet.getInt(7));
			if (resSet.wasNull())
				searchTerm.setPriority(null);
			// 8 time_last_fetched
			Timestamp lastFetched = resSet.getTimestamp(8, Localization.UTC);
			if (!resSet.wasNull())
				searchTerm.setTimeLastFetched(new DateTime(lastFetched.getTime()));
			// 9 last_fetched_tweet_id
			Long lastFetchedTweetId = resSet.getLong(9);
			if (!resSet.wasNull())
				searchTerm.setLastFetchedTweetId(lastFetchedTweetId);
			// 10 last_fetched_tweet_count
			searchTerm.setLastFetchedTweetCount(resSet.getInt(10));
			if (resSet.wasNull())
				searchTerm.setLastFetchedTweetCount(null);
			//  11 when_created
			Timestamp when_created = resSet.getTimestamp(11, Localization.UTC);
			searchTerm.setWhenCreated(new DateTime(when_created.getTime()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (resSet != null)
				resSet.close();
			
			if (prepStatement != null)
				prepStatement.close();
			
			if (conn != null)
				conn.close();
		}
		
		return searchTerm;
	}
	
	/**
	 * Inserts the search term into the database, if the term does not already existent.
	 * During insertion, a new unique Id will be generated for this search term,
	 * which can be retrieved by the corresponding method.
	 * 
	 * @param term The term to be inserted into the database.
	 * @throws SQLException Thrown when there is a problem during insertion.
	 */
	public static void insert(SearchTerm term) throws SQLException {
		Connection conn = null;
		PreparedStatement prepStatement = null;
		ResultSet resSet = null;

		String sqlInsertTerm= "INSERT INTO search_terms ( term, active, current_start, old_start, interval_length,  priority, time_last_fetched, last_fetched_tweet_id, last_fetched_tweet_count, when_created)"
							+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		
		FileInputStream fis = null;
		
		try
		{
			Properties props = new Properties();
			fis = new FileInputStream(DATABASE_PROPERTY_PATH);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			
			String dbUrl =   props.getProperty("javabase.jdbc.url") + "?user=" 
						   + props.getProperty("javabase.jdbc.username") +"&password="
						   + props.getProperty("javabase.jdbc.password");
			conn = DriverManager.getConnection(dbUrl);
			
			// Insert search term into DB
			prepStatement = conn.prepareStatement(sqlInsertTerm);
			prepStatement.setString(1, term.getTerm()); // name
			prepStatement.setBoolean(2, term.isActive()); // active
			prepStatement.setTimestamp(3, new Timestamp(term.getCurrentStart().getMillis()), Localization.UTC); // current_start
			prepStatement.setTimestamp(4, new Timestamp(term.getOldStart().getMillis()), Localization.UTC); // old_start
			prepStatement.setString(5, Localization.printDuration(term.getIntervalLength())); // Interval_Length
			
			if (term.getPriority() != null) // priority
				prepStatement.setInt(6, term.getPriority());
			else
				prepStatement.setNull(6, java.sql.Types.NULL);

			if (term.getTimeLastFetched() == null) // time_last_fetched
				prepStatement.setNull(7, java.sql.Types.NULL);
			else
				prepStatement.setTimestamp(7, new Timestamp(term.getTimeLastFetched().getMillis()), Localization.UTC);

			if (term.getTimeLastFetched() == null) // last_fetched_tweet_id
				prepStatement.setNull(8, java.sql.Types.NULL);
			else
				prepStatement.setLong(8, term.getLastFetchedTweetId());

			if (term.getTimeLastFetched() == null) // last_fetched_tweet_count
				prepStatement.setNull(9, java.sql.Types.NULL);
			else
				prepStatement.setInt(9, term.getLastFetchedTweetCount());
			prepStatement.setTimestamp(10, new Timestamp(term.getWhenCreated().getMillis()), Localization.UTC); // when_started
			
			prepStatement.execute();
			prepStatement.close();
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (SQLException sqlEx) {
			sqlEx.printStackTrace();
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (resSet != null)
				resSet.close();
			
			if (prepStatement != null)
				prepStatement.close();
			
			if (conn != null)
				conn.close();
		}
	}
	
	/**
	 * Returns whether this search term is considered new.
	 * A new Term has neither a old start nor a last fetched
	 * tweet id.
	 * @return Whether this search term is considered new.
	 */
	public boolean isNew() {
		return _old_start == null && _lastFetchedTweetId == null;
	}

	/**
	 * Compares if search_term object is the same as this. 
	 * Parameter to check equality is the Name of the Search Term
	 * @param obj The other SearchTerm object to be compared.
	 * @return Return true, if the names match, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		SearchTerm other = (SearchTerm)obj;
		
		return _term.equals(other._term);
	}
	
	/**
	 * Returns a unique hashcode for this object.
	 * Used internally by Java to create HashMaps
	 */
	@Override
	public int hashCode() {
		int hash = HashCodeUtil.SEED;
		return HashCodeUtil.hash(hash, _id);
	}
/**
 * Checks all search terms and returns only these we consider searchable. Starts a new iteration if the 
 * updatecurrentstart is set to true. A searchable term if it is either a) in the middle of a iteration
 * or b) we have waited long enough(>= interval length).
 *  
 * @param updateCurrentStart set true if we should also start a new iteration if possible
 * @return  true if we are in the middle of a active iteration or its has waited long enough
 * 			false else
 * 			
 */
	public boolean isSearchable(boolean updateCurrentStart) {
		// Check if we are in a iteration
		if (getLastFetchedTweetId() == null) {
			// We are not in the middle of an iteration
			if (getOldStart() != null) {
				// We have an old start date, so we now have to check if we
				// have waited long enough
				DateTime oldStart = getOldStart();
				DateTime now = new DateTime();
				
				// Check for interval length constraint
				if ((now.getMillis() - oldStart.getMillis()) >= getIntervalLength().getMillis()) {
					// Start a new iteration if updatecurrentstart set to true
					if (updateCurrentStart) {
						setCurrentStart(now);
					}
					return true;
				}
			// else: we haven't waited long enough
			} else {
				// This is the very first iteration of the search term, so add it regardless of
				// interval length
				return true;
			}
		}
		else { // We are in the middle of an iteration, so add the search term
			return true;
		}
		// failed all checks, not searchable
		return false;
	}
}
