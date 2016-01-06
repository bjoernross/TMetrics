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
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.daemon.database.SearchTerm;
import com.daemon.database.Transactor;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

public class TransactorTest {
	private static Transactor _trans = null;
	private static String _dbPath = null;
	
	@BeforeClass
	public static void beforeClass() {
		try {
			_dbPath = Transactor.DATABASE_PROPERTY_PATH;
			URL url = Resources.getResourceURL("database.properties");
			Transactor.DATABASE_PROPERTY_PATH = URLDecoder.decode(url.getFile(), "UTF-8");
			_trans = new Transactor();
			_trans.connect();
			
			// Prepare database
			Reader reader = Resources.getResourceAsReader("DaemonDump.sql");
			
			ScriptRunner sr = new ScriptRunner(_trans.getConnection());
			
			sr.setDelimiter(";");
			sr.setLogWriter(null);
			sr.setErrorLogWriter(null);
			sr.runScript(reader);
			
			_trans.getConnection().commit();
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
	}
	
	
	@Test
	public void testGetSearchTerms() {
		String[] termNames = new String[] {
				"snowden",
				"merkel",
				"korea",
				"wurst",
				null, // Not in DB
				"testentry",
				"duration25"
		};
		
		// Check ALL search terms
		List<SearchTerm> terms = _trans.getSearchTerms(false);

		assertTrue("terms.size() returned " + terms.size() + ", expected 6.", terms.size() == 6);
		
		int counter = 1;
		for (SearchTerm term : terms) {
			try {
				if (counter == 5) // There is no entry for id 5, next id is 6
					counter++;

				assertTrue("term.getId() returned " + term.getId() + ", expected " + counter + ".", term.getId() == counter);
				assertTrue("term.getTerm() returned " + term.getTerm() + ", expected " + termNames[counter - 1] + ".", term.getTerm().equals(termNames[counter - 1]));
			}
			catch (Exception ex) {
				// Not possible
			}
			counter++;
		}
		
		// Check only for "active" search terms (that is term.isActive() == true and
		// they are out of the min. interval length)
		terms = _trans.getSearchTerms(true);

		// 5th entry: isActive() == false -> not in this list
		assertTrue("terms.size() returned " + terms.size() + ", expected 5.", terms.size() == 5);
		
		counter = 1;
		for (SearchTerm term : terms) {
			try {
				if (counter == 5) // There is no entry for id 5, next id is 6
					counter += 2; // But id 6 is inactive, so next id is 7
				
				assertTrue("term.getId() returned " + term.getId() + ", expected " + counter + ".", term.getId() == counter);
				assertTrue("term.getTerm() returned " + term.getTerm() + ", expected " + termNames[counter - 1] + ".", term.getTerm().equals(termNames[counter - 1]));
				assertTrue("term.isActive() returned " + term.isActive() + ", expected true.", term.isActive() == true);
			}
			catch (Exception ex) {
				// Not possible
			}
			counter++;
		}
	}
	
	@Test
	public void testGetLongIntervalLength() {
		// 25h15m in Milliseconds
		long intervalLengthMillis = ((25 * 60 * 60 + 15 * 60) * 1000);
		
		// Get search terms
		List<SearchTerm> terms = _trans.getSearchTerms(true);
		assertTrue("terms[(id=7)].getIntervalLength() returned " + terms.get(4).getIntervalLength().toPeriod() + ", expected 25h15m.", terms.get(4).getIntervalLength().getMillis() == intervalLengthMillis);
	}
	
	@Test
	public void testUpdateIntervalLength() {
		// 36h40m in Milliseconds
		long intervalLengthMillis = ((36 * 60 * 60 + 40 * 60) * 1000);
		
		// Get search terms
		List<SearchTerm> terms = _trans.getSearchTerms(true);
		
		// Update interval length
		terms.get(4).setIntervalLength(new Duration(intervalLengthMillis));
		_trans.updateSearchTerm(terms.get(4));
		
		// Refetch entry
		String dbPath = "";
		try {
			dbPath = SearchTerm.DATABASE_PROPERTY_PATH;
			SearchTerm.DATABASE_PROPERTY_PATH = Transactor.DATABASE_PROPERTY_PATH;
			
			SearchTerm term = SearchTerm.load(7);
			assertTrue("term(id).getIntervalLength() returned " + term.getIntervalLength().getMillis() + ", expected " + intervalLengthMillis + ".", term.getIntervalLength().getMillis() == intervalLengthMillis);
			term = SearchTerm.load("duration25");
			assertTrue("term(string).getIntervalLength() returned " + term.getIntervalLength().getMillis() + ", expected " + intervalLengthMillis + ".", term.getIntervalLength().getMillis() == intervalLengthMillis);
		}
		catch (Exception ex) {
			fail(ex.getMessage());
		}
		finally {
			SearchTerm.DATABASE_PROPERTY_PATH = dbPath;
		}
	}

    @Test
    public void testGetSearchTermsImmediately() throws IOException {
		Transactor transactor = null;
		Statement stmt = null;
		
		try {
			transactor = new Transactor();
			transactor.connect();
			
			// Get "old" search terms
			List<SearchTerm> termsBefore = _trans.getSearchTerms(false);
			SearchTerm toBeInserted = new SearchTerm("NOT_INSERTED_YET", new DateTime());
			
			// Add a new search term on a different connection
			transactor.insertSearchTerm(toBeInserted);
			
			List<SearchTerm> termsAfter = _trans.getSearchTerms(false);
			
			// Test if new search term is already there
			assertTrue("termsBefore().size() < termsAfter.size()? Returned " + (termsBefore.size() < termsAfter.size()) +  ", expected true", termsBefore.size() < termsAfter.size());
		
			// Delete newly created search term from DB
			stmt = transactor.getConnection().createStatement();
			stmt.executeUpdate("DELETE FROM search_terms WHERE term = 'NOT_INSERTED_YET'");
			transactor.getConnection().commit();
			
			termsAfter = _trans.getSearchTerms(false);
			
			// Test if search term was removed and we know about it
			assertTrue("termsBefore().size() == termsAfter.size()? Returned " + (termsBefore.size() == termsAfter.size()) +  ", expected true", termsBefore.size() == termsAfter.size());
		}
		catch (Exception ex) {
			fail("Exception" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (Exception _) {}
			}
			
			if (transactor != null) {
				transactor.close();
			}
		}
    }
	
	@Test
	public void testTransactorConnect() {
		Transactor trans = null;
		
		try {
			trans = new Transactor();
			trans.connect();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (trans != null)
				trans.close();
		}
	}
	
	@Test
	public void testUpdateSearchTerm() throws IOException {
		// Restore old value at end
		SearchTerm copy = null;
		String dbPath = SearchTerm.DATABASE_PROPERTY_PATH;
		try {
			URL url = Resources.getResourceURL("database.properties");
			SearchTerm.DATABASE_PROPERTY_PATH = URLDecoder.decode(url.getFile(), "UTF-8");
			copy = SearchTerm.load(1);
			SearchTerm term = SearchTerm.load(1);

			term.setCurrentStart(new DateTime(2013, 12, 04, 13, 40, 30));
			term.setOldStart(new DateTime(2013, 12, 03, 23, 40, 30));
			term.setIntervalLength(SearchTerm.DURATION_FORMATTER.parsePeriod("01:00:00").toStandardDuration());
			term.setTimeLastFetched(null);
			term.setLastFetchedTweetId(null);
			term.setLastFetchedTweetCount(null);
			
			_trans.updateSearchTerm(term);
			
			term = null;
			term = SearchTerm.load(1);

			assertTrue("term.getCurrentStart().getMillis() returned " + term.getCurrentStart().getMillis() + ", expected " + new DateTime(2013, 12, 04, 13, 40, 30).getMillis() + ".", term.getCurrentStart().getMillis() == new DateTime(2013, 12, 04, 13, 40, 30).getMillis());
			assertTrue("term.getOldStart().getMillis() returned " + term.getOldStart().getMillis() + ", expected " + new DateTime(2013, 12, 03, 23, 40, 30).getMillis() + ".", term.getOldStart().getMillis() == new DateTime(2013, 12, 03, 23, 40, 30).getMillis());
			assertTrue("term.getIntervalLength().getMillis() returned " + term.getIntervalLength().getMillis() + ", expected 3600000.", term.getIntervalLength().getMillis() == 3600000);
			assertTrue("term.getTimeLastFetched() returned " + term.getTimeLastFetched() + ", expected null.", term.getTimeLastFetched() == null);
			assertTrue("term.getLastFetchedTweetId() returned " + term.getLastFetchedTweetId() + ", expected null.", term.getLastFetchedTweetId() == null);
			assertTrue("term.getLastFetchedTweetCount() returned " + term.getLastFetchedTweetCount() + ", expected null.", term.getLastFetchedTweetCount() == null);
		}
		catch (SQLException ex) {
			fail("SQL Exception" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
			SearchTerm.DATABASE_PROPERTY_PATH = dbPath;
			if (copy != null) {
				_trans.updateSearchTerm(copy);
			}
		}
	}
	
	@AfterClass
	public static void afterClass() {
		// Restore old DB path
		Transactor.DATABASE_PROPERTY_PATH = _dbPath;
		_trans.close();
	}
}
