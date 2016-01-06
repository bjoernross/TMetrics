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
package com.daemon;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.daemon.database.SearchTerm;
import com.daemon.database.Transactor;

public class SearchTermTest {
	
	private static Transactor transactor = null;
	private static Connection connection = null;
	
	@BeforeClass
	public static void prepareDatabase() throws SQLException {
		try
		{	
			transactor = new Transactor();
			connection = DriverManager.getConnection(transactor.getDbUrl());
			
			// Prepare database
			Reader reader = Resources.getResourceAsReader("DaemonDump.sql");
			
			ScriptRunner sr = new ScriptRunner(connection);
			
			sr.setDelimiter(";");
			sr.setLogWriter(null);
			sr.setErrorLogWriter(null);
			sr.runScript(reader);
			
			connection.commit();
			reader.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch (SQLException sqle)
		{
			sqle.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {
			connection.close();
		}
	}
	
	@Test
	public void testSearchTermLoadId()
	{
		String dbPath = SearchTerm.DATABASE_PROPERTY_PATH;
		
		try {
			SearchTerm.DATABASE_PROPERTY_PATH = "src/test/resources/database.properties";
			
			// Test 1
			SearchTerm term = SearchTerm.load(1);
			
			try {
				assertTrue("SearchTerm.getId() returned " + term.getId() + ", expected 1.", term.getId() == 1);
			}
			catch (Exception ex) {}
			assertTrue("SearchTerm.getTerm() returned " + term.getTerm() + ", expected 'snowden'.", term.getTerm().equals("snowden"));
			assertTrue("SearchTerm.isActive() returned " + term.isActive() + ", expected true.", term.isActive() == true);
			assertTrue("SearchTerm.getPriority() returned " + term.getPriority() + ", expected 0.", term.getPriority() == 0);
			assertTrue("SearchTerm.getCurrentStart().getMillis() returned " + term.getCurrentStart().getMillis() + ", expected 1386252955000.", term.getCurrentStart().getMillis() == 1386252955000L);
			assertTrue("SearchTerm.getOldStart() returned " + term.getOldStart() + ", expected null.", term.getOldStart() == null);
			assertTrue("SearchTerm.getIntervalLength().getMillis() returned " + term.getIntervalLength().getMillis() + ", expected 900000.", term.getIntervalLength().getMillis() == 900000);
			assertTrue("SearchTerm.getTimeLastFetched().getMillis() returned " + term.getTimeLastFetched().getMillis() + ", expected 1386252975000L.", term.getTimeLastFetched().getMillis() == 1386252975000L);
			assertTrue("SearchTerm.getLastFetchedTweetId() returned " + term.getLastFetchedTweetId() + ", expected 408384182499696640L.", term.getLastFetchedTweetId() == 408384182499696640L);
			assertTrue("SearchTerm.getLastFetchedTweetCount() returned " + term.getLastFetchedTweetCount() + ", expected 99.", term.getLastFetchedTweetCount() == 99);
			assertTrue("SearchTerm.getWhenCreated() returned " + term.getWhenCreated() +" , expected.", term.getWhenCreated().getMillis() == 1386252955000L );

			// Test 2
			term = SearchTerm.load(6);

			try {
				assertTrue("SearchTerm.getId() returned " + term.getId() + ", expected 6.", term.getId() == 6);
			}
			catch (Exception ex) {}
			assertTrue("SearchTerm.getTerm() returned " + term.getTerm() + ", expected 'testentry'.", term.getTerm().equals("testentry"));
			assertTrue("SearchTerm.isActive() returned " + term.isActive() + ", expected false.", term.isActive() == false);
			assertTrue("SearchTerm.getPriority() returned " + term.getPriority() + ", expected 1.", term.getPriority() == 1);
			assertTrue("SearchTerm.getCurrentStart().getMillis() returned " + term.getCurrentStart().getMillis() + ", expected 1323065563000L.", term.getCurrentStart().getMillis() == 1323065563000L);
			assertTrue("SearchTerm.getOldStart() returned " + term.getOldStart().getMillis() + ", expected 1323020203000L.", term.getOldStart().getMillis() == 1323020203000L);
			assertTrue("SearchTerm.getIntervalLength().getMillis() returned " + term.getIntervalLength().getMillis() + ", expected 3600000.", term.getIntervalLength().getMillis() == 3600000);
			assertTrue("SearchTerm.getTimeLastFetched().getMillis() returned " + term.getTimeLastFetched().getMillis() + ", expected 1323254809000L.", term.getTimeLastFetched().getMillis() == 1323254809000L);
			assertTrue("SearchTerm.getLastFetchedTweetId() returned " + term.getLastFetchedTweetId() + ", expected null.", term.getLastFetchedTweetId() == null);
			assertTrue("SearchTerm.getLastFetchedTweetCount() returned " + term.getLastFetchedTweetCount() + ", expected 12.", term.getLastFetchedTweetCount() == 12);
			assertTrue("SearchTerm.getWhenCreated() returned " + term.getWhenCreated() +" , expected.", term.getWhenCreated().getMillis() == 1323065563000L );
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Something unexpected happend.");
		}
		finally {
			// Restore old path
			SearchTerm.DATABASE_PROPERTY_PATH = dbPath;
		}
	}

	@Test
	public void testSearchTermLoadTerm()
	{
		String dbPath = SearchTerm.DATABASE_PROPERTY_PATH;
		
		try {
			SearchTerm.DATABASE_PROPERTY_PATH = "src/test/resources/database.properties";
			
			// Test 1

			SearchTerm term = SearchTerm.load("snowden");
			
			try {
				assertTrue("SearchTerm.getId() returned " + term.getId() + ", expected 1.", term.getId() == 1);
			}
			catch (Exception ex) {}
			assertTrue("SearchTerm.getTerm() returned " + term.getTerm() + ", expected 'snowden'.", term.getTerm().equals("snowden"));
			assertTrue("SearchTerm.isActive() returned " + term.isActive() + ", expected true.", term.isActive() == true);
			assertTrue("SearchTerm.getPriority() returned " + term.getPriority() + ", expected 0.", term.getPriority() == 0);
			assertTrue("SearchTerm.getCurrentStart().getMillis() returned " + term.getCurrentStart().getMillis() + ", expected 1386252955000.", term.getCurrentStart().getMillis() == 1386252955000L);
			assertTrue("SearchTerm.getOldStart() returned " + term.getOldStart() + ", expected null.", term.getOldStart() == null);
			assertTrue("SearchTerm.getIntervalLength().getMillis() returned " + term.getIntervalLength().getMillis() + ", expected 900000.", term.getIntervalLength().getMillis() == 900000);
			assertTrue("SearchTerm.getTimeLastFetched().getMillis() returned " + term.getTimeLastFetched().getMillis() + ", expected 1386252975000L.", term.getTimeLastFetched().getMillis() == 1386252975000L);
			assertTrue("SearchTerm.getLastFetchedTweetId() returned " + term.getLastFetchedTweetId() + ", expected 408384182499696640L.", term.getLastFetchedTweetId() == 408384182499696640L);
			assertTrue("SearchTerm.getLastFetchedTweetCount() returned " + term.getLastFetchedTweetCount() + ", expected 99.", term.getLastFetchedTweetCount() == 99);
			assertTrue("SearchTerm.getWhenCreated() returned " + term.getWhenCreated() +" , expected.", term.getWhenCreated().getMillis() == 1386252955000L );

			// Test 2
			term = SearchTerm.load("testentry");

			try {
				assertTrue("SearchTerm.getId() returned " + term.getId() + ", expected 6.", term.getId() == 6);
			}
			catch (Exception ex) {}
			assertTrue("SearchTerm.getTerm() returned " + term.getTerm() + ", expected 'testentry'.", term.getTerm().equals("testentry"));
			assertTrue("SearchTerm.isActive() returned " + term.isActive() + ", expected false.", term.isActive() == false);
			assertTrue("SearchTerm.getPriority() returned " + term.getPriority() + ", expected 1.", term.getPriority() == 1);
			assertTrue("SearchTerm.getCurrentStart().getMillis() returned " + term.getCurrentStart().getMillis() + ", expected 1323065563000L.", term.getCurrentStart().getMillis() == 1323065563000L);
			assertTrue("SearchTerm.getOldStart() returned " + term.getOldStart().getMillis() + ", expected 1323020203000L.", term.getOldStart().getMillis() == 1323020203000L);
			assertTrue("SearchTerm.getIntervalLength().getMillis() returned " + term.getIntervalLength().getMillis() + ", expected 3600000.", term.getIntervalLength().getMillis() == 3600000);
			assertTrue("SearchTerm.getTimeLastFetched().getMillis() returned " + term.getTimeLastFetched().getMillis() + ", expected 1323254809000L.", term.getTimeLastFetched().getMillis() == 1323254809000L);
			assertTrue("SearchTerm.getLastFetchedTweetId() returned " + term.getLastFetchedTweetId() + ", expected null.", term.getLastFetchedTweetId() == null);
			assertTrue("SearchTerm.getLastFetchedTweetCount() returned " + term.getLastFetchedTweetCount() + ", expected 12.", term.getLastFetchedTweetCount() == 12);
			assertTrue("SearchTerm.getWhenCreated() returned " + term.getWhenCreated() +" , expected.", term.getWhenCreated().getMillis() == 1323065563000L );
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			fail("Something unexpected happend.");
		}
		finally {
			// Restore old path
			SearchTerm.DATABASE_PROPERTY_PATH = dbPath;
		}
	}
}
