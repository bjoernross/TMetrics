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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

public class LoggingTest {
	private static String LOG_FILENAME = "test1/test2/Test.log";
	
	@BeforeClass
	public static void prepareTest() {
		
	}
	
	@Test
	public void testLog() {
		// Delete file if it already exists
		new File(LOG_FILENAME).delete();
		
		Logger log = LogManager.getLogger(LOG_FILENAME, 100, 10);
		
		for (int i = 0; i < 52; ++i) {
			log.log("" + i, "Test");
		}

		Scanner s = null;
		
		try {
			s = new Scanner(new File(LOG_FILENAME));
			List<String> lineList = new LinkedList<String>();
			
			while(s.hasNextLine()) {
				String str = s.nextLine();
				lineList.add(str);
			}
			
			assertTrue("log.log(\"\" + i, \"Test\") truncated to " + lineList.size() + ", expected 96.", lineList.size() == 94);
		}
		catch (Exception ex) {
			System.out.println("Cannot open " + LOG_FILENAME + " for testing.");
		}
		finally {
			if (s != null)
				s.close();

			// Close and delete the logged file
			log.close();

			new File(LOG_FILENAME).delete();
		}
	}
}
