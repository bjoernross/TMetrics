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
package com.restservice.serviceLogic.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.news.NewsUtil;
import com.news.TopNewsFetcherThread;
import com.restservice.database.Transactor;
import com.restservice.dto.CountAndNewsPerHour;
import com.restservice.dto.DaemonStatus;
import com.restservice.dto.DataGroupingResult;
import com.restservice.dto.DataGroupingResult.Series;
import com.restservice.dto.Envelope;
import com.restservice.dto.HashtagStatisticsForSearchTermId;
import com.restservice.dto.LanguageCount;
import com.restservice.dto.News;
import com.restservice.dto.NewsItem;
import com.restservice.dto.Query;
import com.restservice.dto.QueryMetadata;
import com.restservice.dto.QueryWithOccurence;
import com.restservice.dto.SearchTermStatus;
import com.restservice.dto.SentimentData;
import com.restservice.dto.SentimentPerQueryPerDate;
import com.restservice.dto.Tweet;
import com.restservice.dto.TweetTexts;
import com.restservice.dto.TweetWithUser;
import com.restservice.dto.TwitterUser;
import com.restservice.rs.QueryService;
import com.restservice.rs.ResultService;

public class IntegrationTest {

	private static File properties; // Database properties file
	private static Transactor transactor;
	private static QueryService queryService;
	private static ResultService resultService;

	@BeforeClass
	public static void resetDatabase() {
		properties = new File(System.getProperty("user.home")
				+ "/database_test.properties");
		// Check if properties file exists. Tests don't start if it doesn't.
		if (!properties.isFile()) {
			fail("No test database properties file found at: "
					+ properties.getPath());
		}

		FileInputStream fis = null;

		try {
			// initialize transactor and services with this file
			transactor = new Transactor(properties.getPath());
			queryService = new QueryService(properties.getPath());
			resultService = new ResultService(properties.getPath());

			Properties props = new Properties();
			fis = new FileInputStream(properties.getPath());
			props.load(fis);
			if (!props.containsKey("database.name")) {
				fail("Please add a database.name setting to your local database.properties file");
			}

			Class.forName(props.getProperty("javabase.jdbc.driver"));

			String dbUrl = props.getProperty("javabase.jdbc.url") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");

			Path path = Paths.get("src/test/resources/Dump.sql");
			Path newPath = Paths.get("src/test/resources/LocalDump.sql");
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(path), charset);

			content = content.replaceAll("resttest",
					props.getProperty("database.name"));
			Files.write(newPath, content.getBytes(charset));

			Reader reader = Resources.getResourceAsReader("LocalDump.sql");

			Connection connection = DriverManager.getConnection(dbUrl);

			ScriptRunner sr = new ScriptRunner(connection);

			sr.setDelimiter(";");
			sr.setLogWriter(null);
			sr.setErrorLogWriter(null);
			sr.runScript(reader);

			connection.commit();
			reader.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			fail("fail");
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Query Logic

	@Test
	public void testContainsQueryPositive() {
		Envelope env = queryService.containsQuery("merkel");

		assertTrue("getData() returned " + (boolean) env.getData()
				+ ", expected: true", (boolean) env.getData());
	}

	@Test
	public void testContainsQueryNegative() {
		Envelope env = queryService.containsQuery("gabriel");

		assertFalse("getData() returned " + (boolean) env.getData()
				+ ", expected: false", (boolean) env.getData());
	}

	@Test(expected = WebApplicationException.class)
	public void testNullParameterContainsQuery() {
		queryService.containsQuery(null);
	}

	@Test
	public void testGetQueriesString() {
		Envelope env = queryService.getQueries("merkel");

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: Query",
				env.getData(), instanceOf(Query.class));

		Query query = (Query) env.getData();

		assertTrue("getString() returned " + query.getString()
				+ ", expected: Merkel",
				query.getString().equalsIgnoreCase("merkel"));
		assertTrue("getId() returned " + query.getId().longValue()
				+ ", expected: 1", query.getId().longValue() == 1);
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetQueriesString() {
		queryService.getQueries((String) null);

	}

	@Test
	public void testGetQueriesId() {
		Envelope env = queryService.getQueries((long) 1);

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: Query",
				env.getData(), instanceOf(Query.class));

		Query query = (Query) env.getData();

		assertTrue("getString() returned " + query.getString()
				+ ", expected: Merkel",
				query.getString().equalsIgnoreCase("merkel"));
		assertTrue("getId() returned " + query.getId().longValue()
				+ ", expected: 1", query.getId().longValue() == 1);
	}

	@Test
	public void testGetQueriesInvalidId() {
		Envelope env = queryService.getQueries((long) -1);

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: Query",
				env.getData(), instanceOf(Query.class));

		Query query = (Query) env.getData();

		assertNull("getString() did not return null as expected",
				query.getString());
		assertNull("getId() did not return null as expected", query.getId());
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetQueriesId() {
		queryService.getQueries((Long) null);
	}

	@Test
	public void testGetMatchingQueriesFull() {
		Envelope env = queryService.getMatchingQueries("merkel");

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		List<QueryWithOccurence> queries = (List<QueryWithOccurence>) env
				.getData();

		assertTrue("List.size() returned " + queries.size() + ", expected: 1",
				queries.size() == 1);

		QueryWithOccurence firstQuery = queries.get(0);

		assertTrue("getString() for query returned " + firstQuery.getString()
				+ ", expected: Merkel", firstQuery.getString()
				.equalsIgnoreCase("merkel"));
		assertTrue("getId() for query returned " + firstQuery.getId()
				+ ", expected: 1", firstQuery.getId().longValue() == 1);
		assertTrue("getCount() for query returned " + firstQuery.getCount()
				+ ", expected: 6", firstQuery.getCount() == 6);
	}

	@Test
	public void testGetMatchingQueriesPartial() {
		Envelope env = queryService.getMatchingQueries("me");

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		List<QueryWithOccurence> queries = (List<QueryWithOccurence>) env
				.getData();

		assertTrue("List.size() returned " + queries.size() + ", expected: 2",
				queries.size() == 2);

		Query merkelQuery = queries.get(0);
		Query steuerQuery = queries.get(1);

		assertTrue("getQuery().getId() returned " + merkelQuery.getId()
				+ ", expected: 1", merkelQuery.getId() == 1);
		assertTrue("getQuery().getString() returned " + merkelQuery.getString()
				+ ", expected: Merkel", merkelQuery.getString()
				.equalsIgnoreCase("Merkel"));

		assertTrue("getQuery().getId() returned " + steuerQuery.getId()
				+ ", expected: 4", steuerQuery.getId() == 4);
		assertTrue("getQuery().getString() returned " + steuerQuery.getString()
				+ ", expected: Mehrwertsteuer", steuerQuery.getString()
				.equalsIgnoreCase("Mehrwertsteuer"));

	}

	@Test
	public void testGetMatchingQueriesEmpty() {
		Envelope env = queryService.getMatchingQueries("Kugel");

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		List<QueryWithOccurence> queries = (List<QueryWithOccurence>) env
				.getData();

		assertTrue("List.size() returned " + queries.size() + ", expected: 0",
				queries.size() == 0);
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetMatchingQueries() {
		queryService.getMatchingQueries((String) null);
	}

	@Test
	public void testGetMetadata() {
		Response res = queryService.getMetadata(3l, "de", null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryMetadata", env.getData(),
				instanceOf(QueryMetadata.class));

		QueryMetadata query = (QueryMetadata) env.getData();

		LocalDate expectedFirstDate = new LocalDate(2013, 1, 1);
		LocalDate expectedLastDate = new LocalDate(2013, 1, 2);

		assertTrue("getNewestTweet().getTime() returned "
				+ query.getNewestTweet().getTime() + ", expected: "
				+ expectedFirstDate.toDate().getTime(), query.getNewestTweet()
				.getTime() == expectedLastDate.toDate().getTime());
		assertTrue("getOldestTweet().getTime() returned "
				+ query.getOldestTweet().getTime() + ", expected: "
				+ expectedLastDate.toDate().getTime(), query.getOldestTweet()
				.getTime() == expectedFirstDate.toDate().getTime());

		assertTrue("getOccurence() returned " + query.getOccurence()
				+ ", expected: 2", query.getOccurence() == 2);
		assertThat("getQuery().class returned "
				+ query.getQuery().getClass().toString() + ", expected: Query",
				query.getQuery(), instanceOf(Query.class));

		assertTrue("getQuery().getId() returned " + query.getQuery().getId()
				+ ", expected: 3", query.getQuery().getId() == 3);
		assertTrue("getQuery().getString() returned "
				+ query.getQuery().getString() + ", expected: Datum", query
				.getQuery().getString().equalsIgnoreCase("Datum"));
	}

	@Test
	public void testGetMetadataInvalidId() {
		Response res = queryService.getMetadata(-1l, "de", null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryMetadata", env.getData(),
				instanceOf(QueryMetadata.class));

		QueryMetadata query = (QueryMetadata) env.getData();

		assertNull("getNewestTweet() did not return null as expected",
				query.getNewestTweet());
		assertNull("getOldestTweet() did not return null as expected",
				query.getOldestTweet());

		assertTrue("getOccurence() returned " + query.getOccurence()
				+ ", expected: 0", query.getOccurence() == 0);
		assertThat("getQuery().class returned "
				+ query.getQuery().getClass().toString() + ", expected: Query",
				query.getQuery(), instanceOf(Query.class));

		assertTrue("getQuery().getId() returned " + query.getQuery().getId()
				+ ", expected: -1", query.getQuery().getId() == -1);
		assertNull("getQuery().getString() did not return null as expected",
				query.getQuery().getString());
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetMetadata() {
		queryService.getMetadata(null, null, null);
	}

	// Save a new search term and delete it afterwards by resetting the database
	// (in the finally statement)
	@Test
	public void testSaveSearchTerm() {
		try {
			queryService.postTwitterQuery("Gabriel");

			try {
				ResultSet results = transactor
						.executeQuery("select active, interval_length from search_terms where term = 'Gabriel';");

				assertTrue("Entered search term not found in database",
						results.first());
				assertTrue(
						"search_term entry active returned "
								+ results.getInt("active") + ", expected: 1",
						results.getInt("active") == 1);
				assertTrue(
						"search_term entry interval_length returned "
								+ results.getTime("interval_length")
								+ ", expected: 00:15:00",
						results.getTime("interval_length").equals(
								Time.valueOf("00:15:00")));
			} catch (SQLException e) {
				fail("Unable to look up entered search term: " + e.toString());
			} finally {
				transactor.close();
			}
		} finally {
			resetDatabase();
		}
	}

	@Test
	public void testSaveExistingSearchTerm() {
		assertTrue("Existing search term doesn't exist in database",
				(boolean) queryService.containsQuery("Merkel").getData());

		try {
			ResultSet results = transactor
					.executeQuery("select count(*) as count from search_terms where term = 'Merkel';");

			assertTrue("Expected at least one result", results.first());
			assertTrue(
					"Number of search term entries: " + results.getInt("count")
							+ ", expected: 1", results.getInt("count") == 1);
		} catch (SQLException e) {
			fail("Unable to count returned entries: " + e.toString());
		} finally {
			transactor.close();
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testNullSaveSearchTerm() {
		queryService.postTwitterQuery((String) null);
	}

	// Result Logic

	@Test
	public void testGetCountAndNewsPerHour() {

		Response res = resultService.getCountAndNewsPerHour(5l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: CountAndNewsPerHour", env.getData(),
				instanceOf(CountAndNewsPerHour.class));

		CountAndNewsPerHour data = (CountAndNewsPerHour) env.getData();

		assertTrue("getQuery().getId() returned " + data.getQuery().getId()
				+ ", expected: 5", data.getQuery().getId() == 5);
		assertTrue("getQuery().getString() returned "
				+ data.getQuery().getString() + ", expected: Count", data
				.getQuery().getString().equalsIgnoreCase("Count"));

		assertTrue("getGraph().size() returned " + data.getGraph().size()
				+ ", expected: 3", data.getGraph().size() == 3);
		assertTrue("getCounts().get(0) returned "
				+ data.getGraph().get(0).getCount() + ", expected: 1", data
				.getGraph().get(0).getCount() == 1);
		assertTrue("getCounts().get(1) returned "
				+ data.getGraph().get(1).getCount() + ", expected: 1", data
				.getGraph().get(1).getCount() == 1);
		assertTrue("getCounts().get(2) returned "
				+ data.getGraph().get(2).getCount() + ", expected: 4", data
				.getGraph().get(2).getCount() == 4);

		assertTrue(
				"getDateStrings().get(0) returned "
						+ data.getGraph().get(0).getDateString()
						+ ", expected: '013-01-01T10:00:00.000'",
				data.getGraph().get(0).getDateString()
						.equalsIgnoreCase("2013-01-01T10:00:00.000"));
		assertTrue(
				"getDateStrings().get(1) returned "
						+ data.getGraph().get(1).getDateString()
						+ ", expected: '2013-01-01T11:00:00.000'",
				data.getGraph().get(1).getDateString()
						.equalsIgnoreCase("2013-01-01T11:00:00.000"));
		assertTrue(
				"getDateStrings().get(2) returned "
						+ data.getGraph().get(2).getDateString()
						+ ", expected: '2013-01-01T12:00:00.000'",
				data.getGraph().get(2).getDateString()
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));

	}

	@Test
	public void testGetCountAndNewsPerHourInvalidId() {
		Response res = resultService.getCountAndNewsPerHour(-1l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: CountAndNewsPerHour", env.getData(),
				instanceOf(CountAndNewsPerHour.class));

		CountAndNewsPerHour data = (CountAndNewsPerHour) env.getData();

		assertTrue("getQuery().getId() returned " + data.getQuery().getId()
				+ ", expected: -1", data.getQuery().getId() == -1);
		assertNull("getQuery().getString() did not return null as expected",
				data.getQuery().getString());

		assertTrue("getGraph().size() returned " + data.getGraph().size()
				+ ", expected: 0", data.getGraph().size() == 0);

	}

	@Test
	public void testGetCountAndNewsPerHourWithZeroes() {
		Response res = resultService.getCountAndNewsPerHour(6l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: CountAndNewsPerHour", env.getData(),
				instanceOf(CountAndNewsPerHour.class));

		CountAndNewsPerHour data = (CountAndNewsPerHour) env.getData();

		assertTrue("getQuery().getId() returned " + data.getQuery().getId()
				+ ", expected: 6", data.getQuery().getId() == 6);
		assertTrue("getQuery().getString() returned "
				+ data.getQuery().getString() + ", expected: CountZero", data
				.getQuery().getString().equalsIgnoreCase("CountZero"));

		assertTrue("getGraph().size() returned " + data.getGraph().size()
				+ ", expected: 4", data.getGraph().size() == 4);
		assertTrue("getCount().get(0) returned "
				+ data.getGraph().get(0).getCount() + ", expected: 1", data
				.getGraph().get(0).getCount() == 1);
		assertTrue("getCounts().get(1) returned "
				+ data.getGraph().get(1).getCount() + ", expected: 0", data
				.getGraph().get(1).getCount() == 0);
		assertTrue("getCounts().get(2) returned "
				+ data.getGraph().get(2).getCount() + ", expected: 0", data
				.getGraph().get(2).getCount() == 0);
		assertTrue("getCounts().get(2) returned "
				+ data.getGraph().get(3).getCount() + ", expected: 1", data
				.getGraph().get(3).getCount() == 1);

		assertTrue(
				"getDateStrings().get(0) returned "
						+ data.getGraph().get(0).getDateString()
						+ ", expected: '2013-01-01T12:00:00.000'",
				data.getGraph().get(0).getDateString()
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));
		assertTrue(
				"getDateStrings().get(1) returned "
						+ data.getGraph().get(1).getDateString()
						+ ", expected: '2013-01-01T13:00:00.000'",
				data.getGraph().get(1).getDateString()
						.equalsIgnoreCase("2013-01-01T13:00:00.000"));
		assertTrue(
				"getDateStrings().get(2) returned "
						+ data.getGraph().get(2).getDateString()
						+ ", expected: '2013-01-01T14:00:00.000'",
				data.getGraph().get(2).getDateString()
						.equalsIgnoreCase("2013-01-01T14:00:00.000"));
		assertTrue(
				"getDateStrings().get(3) returned "
						+ data.getGraph().get(3).getDateString()
						+ ", expected: '2013-01-01T15:00:00.000'",
				data.getGraph().get(3).getDateString()
						.equalsIgnoreCase("2013-01-01T15:00:00.000"));

	}

	@Test
	public void testGetSentimentPerDay() {
		Response res = resultService.getSentimentPerHour(5l, "de", null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentPerQueryPerDate", env.getData(),
				instanceOf(SentimentPerQueryPerDate.class));

		SentimentPerQueryPerDate data = (SentimentPerQueryPerDate) env
				.getData();

		assertTrue(
				"getPositiveCounts().getQuery().getId() returned "
						+ data.getPositiveCounts().getQuery().getId()
						+ ", expected: 5", data.getPositiveCounts().getQuery()
						.getId() == 5);
		assertTrue("getPositiveCounts().getQuery().getString() returned "
				+ data.getPositiveCounts().getQuery().getString()
				+ ", expected: Count", data.getPositiveCounts().getQuery()
				.getString().equalsIgnoreCase("Count"));

		assertTrue(
				"getPositiveCounts().getCounts().size() returned "
						+ data.getPositiveCounts().getCounts().size()
						+ ", expected: 3", data.getPositiveCounts().getCounts()
						.size() == 3);
		assertTrue(
				"getPositiveCounts().getCounts().get(0) returned "
						+ data.getPositiveCounts().getCounts().get(0)
						+ ", expected: 1", data.getPositiveCounts().getCounts()
						.get(0) == 1);
		assertTrue(
				"getPositiveCounts().getCounts().get(1) returned "
						+ data.getPositiveCounts().getCounts().get(1)
						+ ", expected: 0", data.getPositiveCounts().getCounts()
						.get(1) == 0);
		assertTrue(
				"getPositiveCounts().getCounts().get(2) returned "
						+ data.getPositiveCounts().getCounts().get(2)
						+ ", expected: 3", data.getPositiveCounts().getCounts()
						.get(2) == 3);

		assertTrue("getPositiveCounts().getDateStrings().size() returned "
				+ data.getPositiveCounts().getDateStrings().size()
				+ ", expected: 3", data.getPositiveCounts().getDateStrings()
				.size() == 3);
		assertTrue("getPositiveCounts().getDateStrings().get(0) returned "
				+ data.getPositiveCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T10:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T10:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(1) returned "
				+ data.getPositiveCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T11:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T11:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(2) returned "
				+ data.getPositiveCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));

		assertTrue(
				"getNegativeCounts().getQuery().getId() returned "
						+ data.getNegativeCounts().getQuery().getId()
						+ ", expected: 5", data.getNegativeCounts().getQuery()
						.getId() == 5);
		assertTrue("getNegativeCounts().getQuery().getString() returned "
				+ data.getNegativeCounts().getQuery().getString()
				+ ", expected: Count", data.getNegativeCounts().getQuery()
				.getString().equalsIgnoreCase("Count"));

		assertTrue(
				"getNegativeCounts().getCounts().size() returned "
						+ data.getNegativeCounts().getCounts().size()
						+ ", expected: 3", data.getNegativeCounts().getCounts()
						.size() == 3);
		assertTrue(
				"getNegativeCounts().getCounts().get(0) returned "
						+ data.getNegativeCounts().getCounts().get(0)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(0) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(1) returned "
						+ data.getNegativeCounts().getCounts().get(1)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(1) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(2) returned "
						+ data.getNegativeCounts().getCounts().get(2)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(2) == 0);

		assertTrue("getNegativeCounts().getDateStrings().size() returned "
				+ data.getNegativeCounts().getDateStrings().size()
				+ ", expected: 3", data.getNegativeCounts().getDateStrings()
				.size() == 3);
		assertTrue("getNegativeCounts().getDateStrings().get(0) returned "
				+ data.getNegativeCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T10:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T10:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(1) returned "
				+ data.getNegativeCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T11:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T11:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(2) returned "
				+ data.getNegativeCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));
	}
	
	@Test
	public void testGetSentimentPerDayLanguage() {
		Response res = resultService.getSentimentPerHour(5l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentPerQueryPerDate", env.getData(),
				instanceOf(SentimentPerQueryPerDate.class));

		SentimentPerQueryPerDate data = (SentimentPerQueryPerDate) env
				.getData();

		assertTrue(
				"getPositiveCounts().getQuery().getId() returned "
						+ data.getPositiveCounts().getQuery().getId()
						+ ", expected: 5", data.getPositiveCounts().getQuery()
						.getId() == 5);
		assertTrue("getPositiveCounts().getQuery().getString() returned "
				+ data.getPositiveCounts().getQuery().getString()
				+ ", expected: Count", data.getPositiveCounts().getQuery()
				.getString().equalsIgnoreCase("Count"));

		assertTrue(
				"getPositiveCounts().getCounts().size() returned "
						+ data.getPositiveCounts().getCounts().size()
						+ ", expected: 3", data.getPositiveCounts().getCounts()
						.size() == 3);
		assertTrue(
				"getPositiveCounts().getCounts().get(0) returned "
						+ data.getPositiveCounts().getCounts().get(0)
						+ ", expected: 1", data.getPositiveCounts().getCounts()
						.get(0) == 1);
		assertTrue(
				"getPositiveCounts().getCounts().get(1) returned "
						+ data.getPositiveCounts().getCounts().get(1)
						+ ", expected: 1", data.getPositiveCounts().getCounts()
						.get(1) == 1);
		assertTrue(
				"getPositiveCounts().getCounts().get(2) returned "
						+ data.getPositiveCounts().getCounts().get(2)
						+ ", expected: 4", data.getPositiveCounts().getCounts()
						.get(2) == 4);

		assertTrue("getPositiveCounts().getDateStrings().size() returned "
				+ data.getPositiveCounts().getDateStrings().size()
				+ ", expected: 3", data.getPositiveCounts().getDateStrings()
				.size() == 3);
		assertTrue("getPositiveCounts().getDateStrings().get(0) returned "
				+ data.getPositiveCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T10:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T10:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(1) returned "
				+ data.getPositiveCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T11:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T11:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(2) returned "
				+ data.getPositiveCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));

		assertTrue(
				"getNegativeCounts().getQuery().getId() returned "
						+ data.getNegativeCounts().getQuery().getId()
						+ ", expected: 5", data.getNegativeCounts().getQuery()
						.getId() == 5);
		assertTrue("getNegativeCounts().getQuery().getString() returned "
				+ data.getNegativeCounts().getQuery().getString()
				+ ", expected: Count", data.getNegativeCounts().getQuery()
				.getString().equalsIgnoreCase("Count"));

		assertTrue(
				"getNegativeCounts().getCounts().size() returned "
						+ data.getNegativeCounts().getCounts().size()
						+ ", expected: 3", data.getNegativeCounts().getCounts()
						.size() == 3);
		assertTrue(
				"getNegativeCounts().getCounts().get(0) returned "
						+ data.getNegativeCounts().getCounts().get(0)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(0) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(1) returned "
						+ data.getNegativeCounts().getCounts().get(1)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(1) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(2) returned "
						+ data.getNegativeCounts().getCounts().get(2)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(2) == 0);

		assertTrue("getNegativeCounts().getDateStrings().size() returned "
				+ data.getNegativeCounts().getDateStrings().size()
				+ ", expected: 3", data.getNegativeCounts().getDateStrings()
				.size() == 3);
		assertTrue("getNegativeCounts().getDateStrings().get(0) returned "
				+ data.getNegativeCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T10:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T10:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(1) returned "
				+ data.getNegativeCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T11:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T11:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(2) returned "
				+ data.getNegativeCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));
	}

	@Test
	public void testGetSentimentPerDayInvalidId() {
		Response res = resultService.getSentimentPerHour(-1l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentPerQueryPerDate", env.getData(),
				instanceOf(SentimentPerQueryPerDate.class));

		SentimentPerQueryPerDate data = (SentimentPerQueryPerDate) env
				.getData();

		assertTrue("getPositiveCounts().getQuery().getId() returned "
				+ data.getPositiveCounts().getQuery().getId()
				+ ", expected: -1",
				data.getPositiveCounts().getQuery().getId() == -1);
		assertTrue("getPositiveCounts().getQuery().getString() returned "
				+ data.getPositiveCounts().getQuery().getString()
				+ ", expected: ", data.getPositiveCounts().getQuery()
				.getString().equals(""));

		assertTrue(
				"getPositiveCounts().getCounts().size() returned "
						+ data.getPositiveCounts().getCounts().size()
						+ ", expected: 0", data.getPositiveCounts().getCounts()
						.size() == 0);
		assertTrue("getPositiveCounts().getDateStrings().size() returned "
				+ data.getPositiveCounts().getDateStrings().size()
				+ ", expected: 0", data.getPositiveCounts().getDateStrings()
				.size() == 0);

		assertTrue("getNegativeCounts().getQuery().getId() returned "
				+ data.getNegativeCounts().getQuery().getId()
				+ ", expected: -1",
				data.getNegativeCounts().getQuery().getId() == -1);
		assertTrue("getNegativeCounts().getQuery().getString() returned "
				+ data.getNegativeCounts().getQuery().getString()
				+ ", expected: ", data.getNegativeCounts().getQuery()
				.getString().equals(""));

		assertTrue(
				"getNegativeCounts().getCounts().size() returned "
						+ data.getNegativeCounts().getCounts().size()
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.size() == 0);
		assertTrue("getNegativeCounts().getDateStrings().size() returned "
				+ data.getNegativeCounts().getDateStrings().size()
				+ ", expected: 0", data.getNegativeCounts().getDateStrings()
				.size() == 0);
	}

	@Test
	public void testGetSentimentPerDayWithZeroes() {
		Response res = resultService.getSentimentPerHour(6l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentPerQueryPerDate", env.getData(),
				instanceOf(SentimentPerQueryPerDate.class));

		SentimentPerQueryPerDate data = (SentimentPerQueryPerDate) env
				.getData();

		assertTrue(
				"getPositiveCounts().getQuery().getId() returned "
						+ data.getPositiveCounts().getQuery().getId()
						+ ", expected: 6", data.getPositiveCounts().getQuery()
						.getId() == 6);
		assertTrue("getPositiveCounts().getQuery().getString() returned "
				+ data.getPositiveCounts().getQuery().getString()
				+ ", expected: CountZero", data.getPositiveCounts().getQuery()
				.getString().equalsIgnoreCase("CountZero"));

		assertTrue(
				"getPositiveCounts().getCounts().size() returned "
						+ data.getPositiveCounts().getCounts().size()
						+ ", expected: 4", data.getPositiveCounts().getCounts()
						.size() == 4);
		assertTrue(
				"getPositiveCounts().getCounts().get(0) returned "
						+ data.getPositiveCounts().getCounts().get(0)
						+ ", expected: 1", data.getPositiveCounts().getCounts()
						.get(0) == 1);
		assertTrue(
				"getPositiveCounts().getCounts().get(1) returned "
						+ data.getPositiveCounts().getCounts().get(1)
						+ ", expected: 0", data.getPositiveCounts().getCounts()
						.get(1) == 0);
		assertTrue(
				"getPositiveCounts().getCounts().get(2) returned "
						+ data.getPositiveCounts().getCounts().get(2)
						+ ", expected: 0", data.getPositiveCounts().getCounts()
						.get(2) == 0);
		assertTrue(
				"getPositiveCounts().getCounts().get(2) returned "
						+ data.getPositiveCounts().getCounts().get(3)
						+ ", expected: 1", data.getPositiveCounts().getCounts()
						.get(3) == 1);

		assertTrue("getPositiveCounts().getDateStrings().size() returned "
				+ data.getPositiveCounts().getDateStrings().size()
				+ ", expected: 4", data.getPositiveCounts().getDateStrings()
				.size() == 4);
		assertTrue("getPositiveCounts().getDateStrings().get(0) returned "
				+ data.getPositiveCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(1) returned "
				+ data.getPositiveCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T13:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T13:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(2) returned "
				+ data.getPositiveCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T14:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T14:00:00.000"));
		assertTrue("getPositiveCounts().getDateStrings().get(3) returned "
				+ data.getPositiveCounts().getDateStrings().get(3)
				+ ", expected: '2013-01-01T15:00:00.000'",
				data.getPositiveCounts().getDateStrings().get(3)
						.equalsIgnoreCase("2013-01-01T15:00:00.000"));

		assertTrue(
				"getNegativeCounts().getQuery().getId() returned "
						+ data.getNegativeCounts().getQuery().getId()
						+ ", expected: 6", data.getNegativeCounts().getQuery()
						.getId() == 6);
		assertTrue("getNegativeCounts().getQuery().getString() returned "
				+ data.getNegativeCounts().getQuery().getString()
				+ ", expected: CountZero", data.getNegativeCounts().getQuery()
				.getString().equalsIgnoreCase("CountZero"));

		assertTrue(
				"getNegativeCounts().getCounts().size() returned "
						+ data.getNegativeCounts().getCounts().size()
						+ ", expected: 4", data.getNegativeCounts().getCounts()
						.size() == 4);
		assertTrue(
				"getNegativeCounts().getCounts().get(0) returned "
						+ data.getNegativeCounts().getCounts().get(0)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(0) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(1) returned "
						+ data.getNegativeCounts().getCounts().get(1)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(1) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(2) returned "
						+ data.getNegativeCounts().getCounts().get(2)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(2) == 0);
		assertTrue(
				"getNegativeCounts().getCounts().get(2) returned "
						+ data.getNegativeCounts().getCounts().get(3)
						+ ", expected: 0", data.getNegativeCounts().getCounts()
						.get(3) == 0);

		assertTrue("getNegativeCounts().getDateStrings().size() returned "
				+ data.getNegativeCounts().getDateStrings().size()
				+ ", expected: 4", data.getNegativeCounts().getDateStrings()
				.size() == 4);
		assertTrue("getNegativeCounts().getDateStrings().get(0) returned "
				+ data.getNegativeCounts().getDateStrings().get(0)
				+ ", expected: '2013-01-01T12:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(0)
						.equalsIgnoreCase("2013-01-01T12:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(1) returned "
				+ data.getNegativeCounts().getDateStrings().get(1)
				+ ", expected: '2013-01-01T13:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(1)
						.equalsIgnoreCase("2013-01-01T13:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(2) returned "
				+ data.getNegativeCounts().getDateStrings().get(2)
				+ ", expected: '2013-01-01T14:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(2)
						.equalsIgnoreCase("2013-01-01T14:00:00.000"));
		assertTrue("getNegativeCounts().getDateStrings().get(3) returned "
				+ data.getNegativeCounts().getDateStrings().get(3)
				+ ", expected: '2013-01-01T15:00:00.000'",
				data.getNegativeCounts().getDateStrings().get(3)
						.equalsIgnoreCase("2013-01-01T15:00:00.000"));
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetCountPerDay() {
		resultService.getCountAndNewsPerHour(null, null, null);
	}

	@Test
	public void testGetSentiments() {
		Response res = resultService.getSentiments(1l, "de", null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentData", env.getData(),
				instanceOf(SentimentData.class));

		SentimentData sentData = (SentimentData) env.getData();

		assertTrue("sentData.getId() returned " + sentData.getID()
				+ ", expected: 1", sentData.getID() == 1);
		assertTrue("sentData.getPositive() returned " + sentData.getPositive()
				+ ", expected: 4", sentData.getPositive() == 4);
		assertTrue("sentData.getNeutral() returned " + sentData.getNeutral()
				+ ", expected: 1", sentData.getNeutral() == 1);
		assertTrue("sentData.getNegative() returned " + sentData.getNegative()
				+ ", expected: 0", sentData.getNegative() == 0);

	}

	@Test
	public void testGetHashtagStatisticsForSearchTermId() {
		Response res = resultService.getHashtagStatisticsForSearchTermId(1l,
				"de", 10l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: "
				+ HashtagStatisticsForSearchTermId.class.toString(),
				env.getData(),
				instanceOf(HashtagStatisticsForSearchTermId.class));

		HashtagStatisticsForSearchTermId sentData = (HashtagStatisticsForSearchTermId) env
				.getData();

		assertEquals(sentData.getCounts().size(), 3);
		assertEquals(sentData.getHashtagIds().size(), 3);
		assertEquals(sentData.getHashtagTexts().size(), 3);

		assertTrue(sentData.getCounts().get(0) == 3);
		assertEquals(sentData.getHashtagIds().get(0), "1");
		assertEquals(sentData.getHashtagTexts().get(0), "hashtag1");

		assertTrue(sentData.getCounts().get(1) == 2);
		assertEquals(sentData.getHashtagIds().get(1), "2");
		assertEquals(sentData.getHashtagTexts().get(1), "hashtag2");

		assertTrue(sentData.getCounts().get(2) == 1);
		assertEquals(sentData.getHashtagIds().get(2), "3");
		assertEquals(sentData.getHashtagTexts().get(2), "hashtag3");
	}

	@Test
	public void testGetHashtagStatisticsForSearchTermIdLimit() {
		long limit = 1l;
		// 1
		Response res = resultService.getHashtagStatisticsForSearchTermId(1l,
				null, limit, null);
		Envelope env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: "
				+ HashtagStatisticsForSearchTermId.class.toString(),
				env.getData(),
				instanceOf(HashtagStatisticsForSearchTermId.class));

		HashtagStatisticsForSearchTermId data = (HashtagStatisticsForSearchTermId) env
				.getData();

		assertTrue("Value pairs returned: " + data.getHashtagIds().size()
				+ ", expected: 2", data.getHashtagIds().size() == limit);

		// 2
		limit = 2l;
		res = resultService.getHashtagStatisticsForSearchTermId(1l, null,
				limit, null);
		env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: "
				+ HashtagStatisticsForSearchTermId.class.toString(),
				env.getData(),
				instanceOf(HashtagStatisticsForSearchTermId.class));

		data = (HashtagStatisticsForSearchTermId) env.getData();

		assertTrue("Value pairs returned: " + data.getHashtagIds().size()
				+ ", expected: 2", data.getHashtagIds().size() == limit);

		// 3
		limit = 3l;
		res = resultService.getHashtagStatisticsForSearchTermId(1l, null,
				limit, null);
		env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: "
				+ HashtagStatisticsForSearchTermId.class.toString(),
				env.getData(),
				instanceOf(HashtagStatisticsForSearchTermId.class));

		data = (HashtagStatisticsForSearchTermId) env.getData();

		assertTrue("Value pairs returned: " + data.getHashtagIds().size()
				+ ", expected: 2", data.getHashtagIds().size() == limit);

	}

	/*
	 * Tests whether a hashtag is ignored and not returned in case it equals the
	 * search term
	 */
	@Test
	public void testGetHashtagStatisticsIgnoreSearchTerm() {
		Response res = resultService.getHashtagStatisticsForSearchTermId(3l,
				null, 10l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: "
				+ HashtagStatisticsForSearchTermId.class.toString(),
				env.getData(),
				instanceOf(HashtagStatisticsForSearchTermId.class));

		HashtagStatisticsForSearchTermId data = (HashtagStatisticsForSearchTermId) env
				.getData();

		assertTrue("Value pairs returned: " + data.getHashtagIds().size()
				+ ", expected: 1", data.getHashtagIds().size() == 1);
	}

	@Test
	public void testGetSentimentsInvalidId() {
		Response res = resultService.getSentiments(-1l, null, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: SentimentData", env.getData(),
				instanceOf(SentimentData.class));

		SentimentData sentData = (SentimentData) env.getData();

		assertTrue("sentData.getId() returned " + sentData.getID()
				+ ", expected: -1", sentData.getID() == -1);
		assertTrue("sentData.getPositive() returned " + sentData.getPositive()
				+ ", expected: 0", sentData.getPositive() == 0);
		assertTrue("sentData.getNeutral() returned " + sentData.getNeutral()
				+ ", expected: 0", sentData.getNeutral() == 0);
		assertTrue("sentData.getNegative() returned " + sentData.getNegative()
				+ ", expected: 0", sentData.getNegative() == 0);

	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetSentiments() {
		resultService.getSentiments(null, null, null);
	}

	@Test
	public void testGetTweet() {
		Envelope env = resultService.getTweet("1");

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: Tweet",
				env.getData(), instanceOf(Tweet.class));

		Tweet tweet = (Tweet) env.getData();

		assertTrue("tweet.getId() returned " + tweet.getId().toString()
				+ ", expected: 1", tweet.getId().equalsIgnoreCase("1"));
		assertTrue("tweet.getLang().getIsoCode() returned "
				+ tweet.getLang().getIsoCode() + ", expected: de", tweet
				.getLang().getIsoCode().equalsIgnoreCase("de"));
		assertNull("tweet.getReplyId() did not return null as expected ",
				tweet.getReplyId());
		assertNull("tweet.getRetweetId() did not return null as expected ",
				tweet.getRetweetId());
		assertTrue("tweet.getSource() returned " + tweet.getSource()
				+ ", expected: www.url.com", tweet.getSource()
				.equalsIgnoreCase("www.url.com"));
		assertTrue("tweet.getText() returned " + tweet.getText()
				+ ", expected: Merkel",
				tweet.getText().equalsIgnoreCase("Merkel"));
		assertTrue("tweet.getUserId() returned " + tweet.getUserId()
				+ ", expected: 1", tweet.getUserId().equalsIgnoreCase("1"));
		assertTrue(
				"tweet.getCoordinateLatitude() returned "
						+ tweet.getCoordinateLatitude() + ", expected: 18.9413",
				tweet.getCoordinateLatitude() == 18.9413f);
		assertTrue(
				"tweet.getCoordinateLongitude() returned "
						+ tweet.getCoordinateLongitude() + ", expected: 69.641",
				tweet.getCoordinateLongitude() == 69.641f);
		assertTrue(
				"tweet.getRetweetCount() returned " + tweet.getRetweetCount()
						+ ", expected: 2", tweet.getRetweetCount() == 2);
		assertTrue("tweet.getSentiment().getValue() returned "
				+ tweet.getSentiment().getValue() + ", expected: 0", tweet
				.getSentiment().getValue() == (float) 0);

	}

	@Test
	public void testGetTweetInvalidId() {
		Envelope env = resultService.getTweet("-1");
		assertNull("getData() did not return null as expected ", env.getData());
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetTweet() {
		resultService.getTweet(null);
	}

	@Test
	public void testGetUser() {
		Envelope env = resultService.getUser("1");

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString()
				+ ", expected: TwitterUser", env.getData(),
				instanceOf(TwitterUser.class));

		TwitterUser user = (TwitterUser) env.getData();

		assertTrue("user.getId() returned " + user.getId() + ", expected: 1",
				user.getId().equalsIgnoreCase("1"));
		assertTrue("user.getDescription() returned " + user.getDescription()
				+ ", expected: description", user.getDescription()
				.equalsIgnoreCase("description"));
		assertTrue(
				"user.getFollowersCount() returned " + user.getFollowersCount()
						+ ", expected: 1", user.getFollowersCount() == 1);
		assertTrue("user.getLang() returned " + user.getLang()
				+ ", expected: de", user.getLang().equalsIgnoreCase("de"));
		assertTrue("user.getLocation() returned " + user.getLocation()
				+ ", expected: de", user.getLocation().equalsIgnoreCase("de"));
		assertTrue("user.getName() returned " + user.getName()
				+ ", expected: name", user.getName().equalsIgnoreCase("name"));
		assertTrue(
				"user.getProfileImageUrl() returned "
						+ user.getProfileImageUrl() + ", expected: image url",
				user.getProfileImageUrl().equalsIgnoreCase("image url"));
		assertTrue("user.getScreenName() returned " + user.getScreenName()
				+ ", expected: screen_name", user.getScreenName()
				.equalsIgnoreCase("screen_name"));
		assertTrue(
				"user.getStatusesCount() returned " + user.getStatusesCount()
						+ ", expected: 1", user.getStatusesCount() == 1);
		assertTrue("user.getTimeZone() returned " + user.getTimeZone()
				+ ", expected: UTC", user.getTimeZone().equalsIgnoreCase("UTC"));
		assertTrue("user.getUrl() returned " + user.getUrl()
				+ ", expected: url", user.getUrl().equalsIgnoreCase("url"));
		assertTrue("user.getVerified() returned " + user.getVerified()
				+ ", expected: 1", user.getVerified() == 1);

	}

	@Test
	public void testGetUserInvalidId() {
		Envelope env = resultService.getUser("-1");

		assertNull("getData() did not return null as expected ", env.getData());
	}

	@Test(expected = WebApplicationException.class)
	public void testNullGetUser() {
		resultService.getUser(null);
	}

	@Test
	public void testGetTweetsForSearchTerm() {
		try {
			Response res = resultService.getTweets((long) 1, null, null, null,
					null, null, 100, null);

			Envelope env = (Envelope) res.getEntity();

			assertThat("getData().Class returned "
					+ env.getData().getClass().toString() + ", expected: List",
					env.getData(), instanceOf(List.class));

			@SuppressWarnings("unchecked")
			ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
					.getData();

			assertTrue("List.size() returned " + tweets.size()
					+ ", expected: 6", tweets.size() == 6);

			TweetWithUser twu = tweets.get(0);

			assertTrue("twu.getTweet().getId() returned "
					+ twu.getTweet().getId() + ", expected: 1", twu.getTweet()
					.getId().equalsIgnoreCase("1"));
			assertTrue("twu.getTweet().getText() returned "
					+ twu.getTweet().getText() + ", expected: Merkel", twu
					.getTweet().getText().equalsIgnoreCase("Merkel"));
			assertTrue("twu.getTweet().getCoordinateLongitude() returned "
					+ twu.getTweet().getCoordinateLongitude()
					+ ", expected: 69.641", (twu.getTweet()
					.getCoordinateLongitude() == 69.641f));
			assertTrue("twu.getTweet().getCoordinateLatitude() returned "
					+ twu.getTweet().getCoordinateLatitude()
					+ ", expected: 18.9413", (twu.getTweet()
					.getCoordinateLatitude() == 18.9413f));
			assertTrue("twu.getTweet().getCreatedAt() returned "
					+ twu.getTweet().getCreatedAt()
					+ ", expected: 2013-12-02 16:04:29.0", twu.getTweet()
					.getCreatedAt().equalsIgnoreCase("2013-12-02 16:04:29.0"));
			assertTrue("twu.getTweet().getRetweetCount() returned "
					+ twu.getTweet().getRetweetCount() + ", expected: 2", (twu
					.getTweet().getRetweetCount() == 2));
			assertTrue("twu.getTweet().getLang().getIsoCode() returned "
					+ twu.getTweet().getLang().getIsoCode() + ", expected: de",
					twu.getTweet().getLang().getIsoCode()
							.equalsIgnoreCase("de"));
			assertTrue("twu.getTweet().getSentiment().getValue() returned "
					+ twu.getTweet().getSentiment().getValue()
					+ ", expected: 0", (twu.getTweet().getSentiment()
					.getValue() == 0));

			assertTrue("twu.getUser().getId() returned "
					+ twu.getUser().getId() + ", expected: 1", twu.getUser()
					.getId().equalsIgnoreCase("1"));
			assertTrue("twu.getUser().getName() returned "
					+ twu.getUser().getName() + ", expected: name", twu
					.getUser().getName().equalsIgnoreCase("name"));
			assertTrue(
					"twu.getUser().getScreenName() returned "
							+ twu.getUser().getScreenName()
							+ ", expected: screen_name", twu.getUser()
							.getScreenName().equalsIgnoreCase("screen_name"));
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTweetsForSearchTermPositive() {
		Response res = resultService.getTweets((long) 1, "positive", null,
				"2014-01-01 11:00:00", null, null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 2",
				tweets.size() == 2);

		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 3", twu.getTweet().getId().equalsIgnoreCase("3"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLongitude() == 0));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLatitude() == 0));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:07:11.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:07:11.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 1", (twu
				.getTweet().getRetweetCount() == 1));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 1",
				(twu.getTweet().getSentiment().getValue() == 1));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermNeutral() {
		Response res = resultService.getTweets((long) 1, "neutral", null, null,
				null, null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 1",
				tweets.size() == 1);

		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 1", twu.getTweet().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude()
				+ ", expected: 69.641", (twu.getTweet()
				.getCoordinateLongitude() == 69.641f));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude()
				+ ", expected: 18.9413", (twu.getTweet()
				.getCoordinateLatitude() == 18.9413f));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:04:29.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:04:29.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 2", (twu
				.getTweet().getRetweetCount() == 2));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 0",
				(twu.getTweet().getSentiment().getValue() == 0));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermNegative() {
		Response res = resultService.getTweets((long) 1, "negative", null,
				null, null, null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 0",
				tweets.size() == 0);

	}

	@Test
	public void testGetTweetsForSearchTermBefore() {
		Response res = resultService.getTweets((long) 1, null, null,
				"2013-12-02T16:05Z", null, null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 1",
				tweets.size() == 1);

		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 1", twu.getTweet().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude()
				+ ", expected: 69.641", (twu.getTweet()
				.getCoordinateLongitude() == 69.641f));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude()
				+ ", expected: 18.9413", (twu.getTweet()
				.getCoordinateLatitude() == 18.9413f));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:04:29.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:04:29.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 2", (twu
				.getTweet().getRetweetCount() == 2));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 0",
				(twu.getTweet().getSentiment().getValue() == 0));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermAfter() {
		Response res = resultService.getTweets((long) 1, null,
				"2013-12-02T16:05Z", "2014-01-01T11:00:00Z", null, null, 100,
				null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 2",
				tweets.size() == 2);
		
		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 3", twu.getTweet().getId().equalsIgnoreCase("3"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLongitude() == 0));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLatitude() == 0));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:07:11.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:07:11.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 1", (twu
				.getTweet().getRetweetCount() == 1));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 1",
				(twu.getTweet().getSentiment().getValue() == 1));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermIntervall() {
		Response res = resultService.getTweets((long) 1, null,
				"2013-12-02T16:05:05Z", "2013-12-02T16:07:05Z", null, null,
				100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 1",
				tweets.size() == 1);

		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 2", twu.getTweet().getId().equalsIgnoreCase("2"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLongitude() == 0));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLatitude() == 0));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:07:04.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:07:04.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 0", (twu
				.getTweet().getRetweetCount() == 0));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 1",
				(twu.getTweet().getSentiment().getValue() == 1));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermLimit() {
		Response res = resultService.getTweets((long) 1, null,
				"2013-12-02T16:05:05Z", "2013-12-02T16:08:05Z", null, null, 1,
				null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 1",
				tweets.size() == 1);

		TweetWithUser twu = tweets.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 3", twu.getTweet().getId().equalsIgnoreCase("3"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLongitude() == 0));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude() + ", expected: 0",
				(twu.getTweet().getCoordinateLatitude() == 0));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:07:11.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:07:11.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 1", (twu
				.getTweet().getRetweetCount() == 1));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 1",
				(twu.getTweet().getSentiment().getValue() == 1));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));
	}

	@Test
	public void testGetTweetsForSearchTermInvalidId() {
		Response res = resultService.getTweets((long) -1, null, null, null,
				null, null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().Class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 0",
				tweets.size() == 0);

	}

	/*
	 * Database contains two "count" tweets with language code "en"
	 */
	@Test
	public void testGetTweetsForSearchTermLanguage() {
		Response res = resultService.getTweets(5l, null, null, null, "en",
				null, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> tweets = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned " + tweets.size() + ", expected: 2",
				tweets.size() == 2);

		for (TweetWithUser tweet : tweets) {
			assertTrue("tweet.getLang().getIsoCode() returned "
					+ tweet.getTweet().getLang().getIsoCode()
					+ ", expected: en", tweet.getTweet().getLang().getIsoCode()
					.equals("en"));
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testNullForTweets() {
		resultService.getTweets(null, null, null, null, null, null, null, null);
	}

	@Test
	public void testTagCloud() {
		Response res = resultService.getTagCloud(5l, null, 100l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryWithText", env.getData(),
				instanceOf(TweetTexts.class));

		TweetTexts query = (TweetTexts) env.getData();

		assertTrue("getCount() returned " + query.getCount() + ", expected: 6",
				query.getCount().longValue() == 6l);
		assertTrue(
				"getText() returned "
						+ query.getText()
						+ ", expected: Counts1 Counts Counts Counts Counts Counts ",
				query.getText().equals(
						"Counts1 Counts Counts Counts Counts Counts "));
	}

	@Test
	public void testTagCloudLimited() {
		try {
			Response res = resultService.getTagCloud(5l, null, 3l, null);

			Envelope env = (Envelope) res.getEntity();

			assertThat("getData().class returned "
					+ env.getData().getClass().toString()
					+ ", expected: QueryWithText", env.getData(),
					instanceOf(TweetTexts.class));

			TweetTexts query = (TweetTexts) env.getData();

			assertTrue("getCount() returned " + query.getCount()
					+ ", expected: 3", query.getCount().longValue() == 3);
			assertTrue("getText() returned " + query.getText()
					+ ", expected: Counts1 Counts Counts ", query.getText()
					.equals("Counts1 Counts Counts "));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testTagCloudNull() {
		resultService.getTagCloud(null, null, null, null);
	}

	@Test
	public void testTagCloudNonexistentQuery() {
		Response res = resultService.getTagCloud(100l, null, 100l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryWithText", env.getData(),
				instanceOf(TweetTexts.class));

		TweetTexts query = (TweetTexts) env.getData();

		assertTrue("getCount() returned " + query.getCount() + ", expected: 0",
				query.getCount().longValue() == 0);
		assertTrue("getText() returned " + query.getText() + ", expected: ",
				query.getText().equals(""));
	}

	/*
	 * count only contains four German tweets
	 */
	@Test
	public void testTagCloudLanguage() {
		Response res = resultService.getTagCloud(5l, "de", 100l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryWithText", env.getData(),
				instanceOf(TweetTexts.class));

		TweetTexts query = (TweetTexts) env.getData();

		assertTrue("getCount() returned " + query.getCount() + ", expected: 4",
				query.getCount().longValue() == 4);
	}

	/*
	 * one "Merkel" tweet (id=1) is a retweet (is_retweet_of_id != null), so we
	 * expect 2 instead of 3 results
	 */
	@Test
	public void testTagCloudIgnoreRetweets() {
		Response res = resultService.getTagCloud(1l, null, 100l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString()
				+ ", expected: QueryWithText", env.getData(),
				instanceOf(TweetTexts.class));

		TweetTexts query = (TweetTexts) env.getData();

		assertTrue("getCount() returned " + query.getCount() + ", expected: 5",
				query.getCount().longValue() == 5);
	}

	
	@Test
	public void testGetDataGroupsQuery() {
		resultService.setUseRestTestDBForDataGrouping(true);
		Response res = resultService.dataGroupingAlternative((long) 1, "en", 100, null);

		Envelope env = (Envelope) res.getEntity();

		DataGroupingResult dgr = (DataGroupingResult) env.getData();

		ArrayList<Series> series = dgr.getSeries();
		assertTrue(series.get(0).getName().equals("Cluster 1"));
	}

	/*
	 * For search term "count" (id=5) there are four German and two English
	 * tweets
	 */
	@Test
	public void testGetLanguages() {
		Response res = resultService.getLanguages(5l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<LanguageCount> data = (ArrayList<LanguageCount>) env
				.getData();

		assertTrue("List.size() returned: " + data.size() + ", expected: 2",
				data.size() == 2);
		assertTrue("First entry language: " + data.get(0).getIsoCode()
				+ ", expected: de", data.get(0).getIsoCode().equals("de"));
		assertTrue("First entry count: " + data.get(0).getCount()
				+ ", expected: 4", data.get(0).getCount() == 4);
		assertTrue("Second entry language: " + data.get(1).getIsoCode()
				+ ", expected: en", data.get(1).getIsoCode().equals("en"));
		assertTrue("Second entry count: " + data.get(1).getCount()
				+ ", expected: 2", data.get(1).getCount() == 2);
	}

	/*
	 * The id 100 doesn't exist in our data base
	 */
	@Test
	public void testGetLanguagesInvalidId() {
		Response res = resultService.getLanguages(100l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<LanguageCount> data = (ArrayList<LanguageCount>) env
				.getData();

		assertTrue("List.size() returned: " + data.size() + ", expected: 0",
				data.size() == 0);
	}

	/*
	 * Search term 4 contains only one tweet without language (= null)
	 */
	@Ignore
	public void testGetLanguagesNull() {
		Response res = resultService.getLanguages(4l, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<LanguageCount> data = (ArrayList<LanguageCount>) env
				.getData();

		assertTrue("List.size() returned: " + data.size() + ", expected: 1",
				data.size() == 1);
		assertNull("First entry language expected to be null", data.get(0)
				.getIsoCode());
	}

	@Test
	public void testGetHashtagTweets() {
		Response res = resultService.getTweets((long) 1, null, null, null,
				null, 3l, 100, null);

		Envelope env = (Envelope) res.getEntity();

		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: List",
				env.getData(), instanceOf(List.class));

		@SuppressWarnings("unchecked")
		ArrayList<TweetWithUser> data = (ArrayList<TweetWithUser>) env
				.getData();

		assertTrue("List.size() returned: " + data.size() + ", expected: 1",
				data.size() == 1);

		TweetWithUser twu = data.get(0);

		assertTrue("twu.getTweet().getId() returned " + twu.getTweet().getId()
				+ ", expected: 1", twu.getTweet().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getTweet().getText() returned "
				+ twu.getTweet().getText() + ", expected: Merkel", twu
				.getTweet().getText().equalsIgnoreCase("Merkel"));
		assertTrue("twu.getTweet().getCoordinateLongitude() returned "
				+ twu.getTweet().getCoordinateLongitude()
				+ ", expected: 69.641", (twu.getTweet()
				.getCoordinateLongitude() == 69.641f));
		assertTrue("twu.getTweet().getCoordinateLatitude() returned "
				+ twu.getTweet().getCoordinateLatitude()
				+ ", expected: 18.9413", (twu.getTweet()
				.getCoordinateLatitude() == 18.9413f));
		assertTrue("twu.getTweet().getCreatedAt() returned "
				+ twu.getTweet().getCreatedAt()
				+ ", expected: 2013-12-02 16:04:29.0", twu.getTweet()
				.getCreatedAt().equalsIgnoreCase("2013-12-02 16:04:29.0"));
		assertTrue("twu.getTweet().getRetweetCount() returned "
				+ twu.getTweet().getRetweetCount() + ", expected: 2", (twu
				.getTweet().getRetweetCount() == 2));
		assertTrue("twu.getTweet().getLang().getIsoCode() returned "
				+ twu.getTweet().getLang().getIsoCode() + ", expected: de", twu
				.getTweet().getLang().getIsoCode().equalsIgnoreCase("de"));
		assertTrue("twu.getTweet().getSentiment().getValue() returned "
				+ twu.getTweet().getSentiment().getValue() + ", expected: 0",
				(twu.getTweet().getSentiment().getValue() == 0));

		assertTrue("twu.getUser().getId() returned " + twu.getUser().getId()
				+ ", expected: 1", twu.getUser().getId().equalsIgnoreCase("1"));
		assertTrue("twu.getUser().getName() returned "
				+ twu.getUser().getName() + ", expected: name", twu.getUser()
				.getName().equalsIgnoreCase("name"));
		assertTrue("twu.getUser().getScreenName() returned "
				+ twu.getUser().getScreenName() + ", expected: screen_name",
				twu.getUser().getScreenName().equalsIgnoreCase("screen_name"));

	}

	@Test(expected = WebApplicationException.class)
	public void testGetHashtagTweetsIdNull() {
		resultService.getTweets(null, null, null, null, null, null, 100, null);
	}

	@Test
	public void testGetDaemonStatus() {
		Envelope env = queryService.getDaemonStatus();

		DaemonStatus data = (DaemonStatus) env.getData();

		assertTrue("data.getTotalCount() returned: " + data.getTotalCount()
				+ ", expected: 6", data.getTotalCount() == 6);
		assertTrue("data.getActiveCount() returned: " + data.getActiveCount()
				+ ", expected: 4", data.getActiveCount() == 4);
		assertTrue("data.getSearchTerms().size() returned: "
				+ data.getSearchTerms().size() + ", expected: 6", data
				.getSearchTerms().size() == 6);

		SearchTermStatus searchTerm = data.getSearchTerms().get(0);
		assertTrue("searchTerm.getName() returned: " + searchTerm.getName()
				+ ", expected: Merkel", searchTerm.getName().equals("Merkel"));
		assertTrue(
				"searchTerm.getCreatedAtString() returned: "
						+ searchTerm.getCreatedAt()
						+ ", expected: 2013-10-01 13:37:42.0",
				searchTerm.getCreatedAt().equalsIgnoreCase(
						"2013-10-01 13:37:42.0"));
		assertTrue("searchTerm.getTimeLastFetchedString() returned: "
				+ searchTerm.getTimeLastFetched()
				+ ", expected: 2013-12-04 16:15:35.0", searchTerm
				.getTimeLastFetched().equalsIgnoreCase("2013-12-04 16:15:35.0"));
		assertTrue("searchTerm.getIntervalLengthString() returned: "
				+ searchTerm.getIntervalLength() + ", expected: 00:30:00",
				searchTerm.getIntervalLength().equalsIgnoreCase("00:30:00"));
		assertTrue(
				"searchTerm.getInIteration() returned: "
						+ searchTerm.getInIteration() + ", expected: true",
				searchTerm.getInIteration() == true);
		assertTrue(
				"searchTerm.getPriority() returned: "
						+ searchTerm.getPriority() + ", expected: 1",
				searchTerm.getPriority() == 1);
		assertTrue("searchTerm.getActive() returned: " + searchTerm.getActive()
				+ ", expected: true", searchTerm.getActive() == true);

		searchTerm = data.getSearchTerms().get(1);
		assertTrue("searchTerm.getName() returned: " + searchTerm.getName()
				+ ", expected: Meer", searchTerm.getName().equals("Meer"));
		assertTrue(
				"searchTerm.getCreatedAtString() returned: "
						+ searchTerm.getCreatedAt()
						+ ", expected: 2013-10-02 13:37:42.0",
				searchTerm.getCreatedAt().equalsIgnoreCase(
						"2013-10-02 13:37:42.0"));
		assertNull("searchTerm.getTimeLastFetched() expected to be null",
				searchTerm.getTimeLastFetched());
		assertTrue("searchTerm.getIntervalLengthString() returned: "
				+ searchTerm.getIntervalLength() + ", expected: 00:15:00",
				searchTerm.getIntervalLength().equalsIgnoreCase("00:15:00"));
		assertTrue(
				"searchTerm.getInIteration() returned: "
						+ searchTerm.getInIteration() + ", expected: false",
				searchTerm.getInIteration() == false);
		assertTrue(
				"searchTerm.getPriority() returned: "
						+ searchTerm.getPriority() + ", expected: -2",
				searchTerm.getPriority() == -2);
		assertTrue("searchTerm.getActive() returned: " + searchTerm.getActive()
				+ ", expected: false", searchTerm.getActive() == false);
	}

	@Test
	public void testPostPriority() {
		try {
			Envelope env = queryService.postPriority(1, -2);

			env = queryService.getDaemonStatus();

			DaemonStatus data = (DaemonStatus) env.getData();
			SearchTermStatus searchTerm = data.getSearchTerms().get(0);
			assertTrue("searchTerm.getName() returned: " + searchTerm.getName()
					+ ", expected: Merkel",
					searchTerm.getName().equals("Merkel"));
			assertTrue(
					"searchTerm.getPriority() returned: "
							+ searchTerm.getPriority() + ", expected: -2",
					searchTerm.getPriority() == -2);
		} finally {
			resetDatabase();
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testPostPriorityIdNull() {
		queryService.postPriority(null, -2);
	}

	@Test(expected = WebApplicationException.class)
	public void testPostPriorityNull() {
		queryService.postPriority(1, null);
	}

	@Test(expected = WebApplicationException.class)
	public void testPostPriorityOutOfRange() {
		queryService.postPriority(1, 3);
	}

	@Test
	public void testPostActiveFlag() {
		try {
			Envelope env = queryService.postActiveFlag(1, false);

			env = queryService.getDaemonStatus();

			DaemonStatus data = (DaemonStatus) env.getData();
			SearchTermStatus searchTerm = data.getSearchTerms().get(0);
			assertTrue("searchTerm.getName() returned: " + searchTerm.getName()
					+ ", expected: Merkel",
					searchTerm.getName().equals("Merkel"));
			assertTrue(
					"searchTerm.getActive() returned: "
							+ searchTerm.getActive() + ", expected: false",
					searchTerm.getActive() == false);
		} finally {
			resetDatabase();
		}
	}

	@Test(expected = WebApplicationException.class)
	public void testPostActiveFlagIdNull() {
		queryService.postActiveFlag(null, false);
	}

	@Test(expected = WebApplicationException.class)
	public void testPostActiveFlagNull() {
		queryService.postActiveFlag(1, null);
	}

	/*
	 * getRelatedNews regular with results
	 */
	@Test
	public void testGetFeedsFromTweetstextRegularInput() {
		// change news provider to existing test file
		try {
			String url = (new File("src/test/resources/RSSTestFile.xhtml"))
					.toURI().toURL().toString();
			String[][] searchpaths = { { "RSSTestFile", url } };
			String[] dateformats = { "" };
			NewsUtil.SEARCHPATHS = searchpaths;
			NewsUtil.DATEFORMATS = dateformats;
			TopNewsFetcherThread.transactor = this.transactor;
		} catch (MalformedURLException e) {
			fail("testfile not found");
			e.printStackTrace();
		}
		// get and check news
		Response res = resultService.getRelatedNews(1l, null, 1, 1, 2014, null);
		Envelope env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: News",
				env.getData(), instanceOf(News.class));
		@SuppressWarnings("unchecked")
		News data = (News) env.getData();
		assertTrue("wrong number of news items returned (expected 2)", data
				.getNews().size() == 2);
		assertTrue("wrong elemented was listed top", data.getNews().get(0)
				.getTitle()
				.equals("Bundeskanzlerin ruft Brger zum Handeln auf"));
		for (NewsItem n : data.getNews()) {
			assertTrue("all newsitems should be rated", n.getRating() > .0);
		}
	}

	/*
	 * getRelatedNews regular without results
	 */
	@Test
	public void testGetFeedsFromTweetstextRegularInputWithoutNewsResults() {
		// change news provider to existing test file
		try {
			String urlB = (new File(
					"src/test/resources/RSSTestFileEmptyBing.xhtml")).toURI()
					.toURL().toString();
			String urlG = (new File(
					"src/test/resources/RSSTestFileEmptyGoogle.xhtml")).toURI()
					.toURL().toString();
			String[][] searchpaths = { { "EmptyBing", urlB },
					{ "EmptyGoogle", urlG } };
			String[] dateformats = { "" };
			NewsUtil.SEARCHPATHS = searchpaths;
			NewsUtil.DATEFORMATS = dateformats;
			TopNewsFetcherThread.transactor = this.transactor;
		} catch (MalformedURLException e) {
			fail("testfile not found");
			e.printStackTrace();
		}
		// get and check news
		Response res = resultService.getRelatedNews(1l, null, 1, 1, 2014, null);
		Envelope env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: News",
				env.getData(), instanceOf(News.class));
		@SuppressWarnings("unchecked")
		News data = (News) env.getData();
		assertTrue("wrong number of news items returned (expected 0)", data
				.getNews().size() == 0);
	}

	/*
	 * getRelatedNews InvalidId
	 */
	@Test
	public void testGetFeedsFromTweetstextInvalidId() {
		// change news provider to existing test file
		try {
			String urlB = (new File(
					"src/test/resources/RSSTestFileEmptyBing.xhtml")).toURI()
					.toURL().toString();
			String urlG = (new File(
					"src/test/resources/RSSTestFileEmptyGoogle.xhtml")).toURI()
					.toURL().toString();
			String[][] searchpaths = { { "EmptyBing", urlB },
					{ "EmptyGoogle", urlG } };
			String[] dateformats = { "" };
			NewsUtil.SEARCHPATHS = searchpaths;
			NewsUtil.DATEFORMATS = dateformats;
			TopNewsFetcherThread.transactor = this.transactor;
		} catch (MalformedURLException e) {
			fail("testfile not found");
			e.printStackTrace();
		}
		// get and check news
		Response res = resultService
				.getRelatedNews(-1l, null, 1, 1, 2014, null);
		Envelope env = (Envelope) res.getEntity();
		assertThat("getData().class returned "
				+ env.getData().getClass().toString() + ", expected: News",
				env.getData(), instanceOf(News.class));
		@SuppressWarnings("unchecked")
		News data = (News) env.getData();
		assertTrue("wrong number of news items returned (expected 0)", data
				.getNews().size() == 0);
	}

	@Test
	public void testHasDaemonFetchedTrue() {
		Envelope env = queryService.hasDaemonFetched(1);

		Boolean hasFetched = (Boolean) env.getData();
		assertTrue("data returned: " + hasFetched + ", expected: true",
				hasFetched == true);
	}

	@Test
	public void testHasDaemonFetchedFalse() {
		Envelope env = queryService.hasDaemonFetched(2);

		Boolean hasFetched = (Boolean) env.getData();
		assertTrue("data returned: " + hasFetched + ", expected: false",
				hasFetched == false);
	}

	@Test
	public void testHasDaemonFetchedInvalidId() {
		Envelope env = queryService.hasDaemonFetched(-1);

		Boolean hasFetched = (Boolean) env.getData();
		assertNull("data did not return null as expected", hasFetched);
	}

	@Ignore
	public void testNotModifiedCache() {
		// Entered data = time_last_fetched for search term 1
		Response response = resultService.getLanguages(1l,
				"WED, 04 Dec 2013 15:15:35 GMT");
		
		assertTrue(
				"Response didn't have status HTTP_NOT_MODIFIED as expected.",
				response.getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED);
	}

}
