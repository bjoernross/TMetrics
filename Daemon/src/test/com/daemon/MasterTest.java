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

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import com.daemon.database.SearchTerm;

public class MasterTest {
	private static List<SearchTerm> _remote1 = new LinkedList<SearchTerm>();
	private static List<SearchTerm> _remote2 = new LinkedList<SearchTerm>();
	
	@BeforeClass
	public static void before() {
		SearchTerm a1 = new SearchTerm("a1", new DateTime());
		a1.setActive(true);
		SearchTerm b1 = new SearchTerm("b1", new DateTime());
		b1.setActive(false);
		SearchTerm c1 = new SearchTerm("c1", new DateTime());
		c1.setActive(true);
		SearchTerm d1 = new SearchTerm("d1", new DateTime());
		d1.setActive(true);
		SearchTerm e1 = new SearchTerm("e1", new DateTime());
		e1.setActive(false);
		
		_remote1.add(a1);
		_remote1.add(b1);
		_remote1.add(c1);
		_remote1.add(d1);
		_remote1.add(e1);

		SearchTerm a2 = new SearchTerm("a2", new DateTime());
		a2.setActive(true);
		SearchTerm c2 = new SearchTerm("c2", new DateTime());
		c2.setActive(false);
		
		_remote2.add(a2);
		_remote2.add(c2);
	}
	
	@Test
	public void makeSearchTermsConsistentTestWithInactive1() {
		List<SearchTerm> localTerms = new LinkedList<SearchTerm>();

		SearchTerm a1 = new SearchTerm("a1", new DateTime());
		a1.setActive(true);
		SearchTerm c1 = new SearchTerm("c1", new DateTime());
		c1.setActive(true);
		SearchTerm e1 = new SearchTerm("e1", new DateTime());
		e1.setActive(true);

		localTerms.add(a1);
		localTerms.add(c1);
		localTerms.add(e1);
		
		List<SearchTerm> newTerms = Master.makeSearchTermsConsistent(localTerms, _remote1, false);

		assertTrue("newTerms.size() == " + newTerms.size() + ", expected 4.", newTerms.size() == 4);
		assertTrue("newTerms[0].getTerm()/isActive() returned " + newTerms.get(0).getTerm() + "/" + newTerms.get(0).isActive() + ", expected 'a1'/true.", newTerms.get(0).getTerm().equals("a1") && newTerms.get(0).isActive());
		assertTrue("newTerms[1].getTerm()/isActive() returned " + newTerms.get(1).getTerm() + "/" + newTerms.get(1).isActive() + ", expected 'c1'/true.", newTerms.get(1).getTerm().equals("c1") && newTerms.get(1).isActive());
		assertTrue("newTerms[2].getTerm()/isActive() returned " + newTerms.get(2).getTerm() + "/" + newTerms.get(2).isActive() + ", expected 'd1'/true.", newTerms.get(2).getTerm().equals("d1") && newTerms.get(2).isActive());
		assertTrue("newTerms[3].getTerm()/isActive() returned " + newTerms.get(3).getTerm() + "/" + newTerms.get(3).isActive() + ", expected 'e1'/false.", newTerms.get(3).getTerm().equals("e1") && !newTerms.get(3).isActive());
	}
	
	@Test
	public void makeSearchTermsConsistentTestWithInactive2() {
		List<SearchTerm> localTerms = new LinkedList<SearchTerm>();

		SearchTerm a2 = new SearchTerm("a2", new DateTime());
		a2.setActive(true);
		SearchTerm c2 = new SearchTerm("c2", new DateTime());
		c2.setActive(true);
		SearchTerm d2 = new SearchTerm("d2", new DateTime());
		d2.setActive(true);

		localTerms.add(a2);
		localTerms.add(c2);
		localTerms.add(d2);
		
		List<SearchTerm> newTerms = Master.makeSearchTermsConsistent(localTerms, _remote2, false);

		assertTrue("newTerms.size() == " + newTerms.size() + ", expected 3.", newTerms.size() == 3);
		assertTrue("newTerms[0].getTerm()/isActive() returned " + newTerms.get(0).getTerm() + "/" + newTerms.get(0).isActive() + ", expected 'a2'/true.", newTerms.get(0).getTerm().equals("a2") && newTerms.get(0).isActive());
		assertTrue("newTerms[1].getTerm()/isActive() returned " + newTerms.get(1).getTerm() + "/" + newTerms.get(1).isActive() + ", expected 'c2'/false.", newTerms.get(1).getTerm().equals("c2") && !newTerms.get(1).isActive());
		assertTrue("newTerms[2].getTerm()/isActive() returned " + newTerms.get(2).getTerm() + "/" + newTerms.get(2).isActive() + ", expected 'd2'/false.", newTerms.get(2).getTerm().equals("d2") && !newTerms.get(2).isActive());
	}
	
	@Test
	public void makeSearchTermsConsistentTestWithoutInactive1() {
		List<SearchTerm> localTerms = new LinkedList<SearchTerm>();

		SearchTerm a1 = new SearchTerm("a1", new DateTime());
		a1.setActive(true);
		SearchTerm c1 = new SearchTerm("c1", new DateTime());
		c1.setActive(true);
		SearchTerm e1 = new SearchTerm("e1", new DateTime());
		e1.setActive(true);

		localTerms.add(a1);
		localTerms.add(c1);
		localTerms.add(e1);
		
		List<SearchTerm> newTerms = Master.makeSearchTermsConsistent(localTerms, _remote1, true);

		assertTrue("newTerms.size() == " + newTerms.size() + ", expected 3.", newTerms.size() == 3);
		assertTrue("newTerms[0].getTerm()/isActive() returned " + newTerms.get(0).getTerm() + "/" + newTerms.get(0).isActive() + ", expected 'a1'/true.", newTerms.get(0).getTerm().equals("a1") && newTerms.get(0).isActive());
		assertTrue("newTerms[1].getTerm()/isActive() returned " + newTerms.get(1).getTerm() + "/" + newTerms.get(1).isActive() + ", expected 'c1'/true.", newTerms.get(1).getTerm().equals("c1") && newTerms.get(1).isActive());
		assertTrue("newTerms[2].getTerm()/isActive() returned " + newTerms.get(2).getTerm() + "/" + newTerms.get(2).isActive() + ", expected 'd1'/true.", newTerms.get(2).getTerm().equals("d1") && newTerms.get(2).isActive());
		assertTrue("e1.isActive() returned " + e1.getTerm() + ", expected false.", !e1.isActive());
		assertTrue("newTerms.contains(e1) returned " + newTerms.contains(e1) + ", expected false.", !newTerms.contains(e1));
	}
	
	@Test
	public void makeSearchTermsConsistentTestWithoutInactive2() {
		List<SearchTerm> localTerms = new LinkedList<SearchTerm>();

		SearchTerm a2 = new SearchTerm("a2", new DateTime());
		a2.setActive(true);
		SearchTerm c2 = new SearchTerm("c2", new DateTime());
		c2.setActive(true);
		SearchTerm d2 = new SearchTerm("d2", new DateTime());
		d2.setActive(true);

		localTerms.add(a2);
		localTerms.add(c2);
		localTerms.add(d2);
		
		List<SearchTerm> newTerms = Master.makeSearchTermsConsistent(localTerms, _remote2, true);

		assertTrue("newTerms.size() == " + newTerms.size() + ", expected 1.", newTerms.size() == 1);
		assertTrue("newTerms[0].getTerm()/isActive() returned " + newTerms.get(0).getTerm() + "/" + newTerms.get(0).isActive() + ", expected 'a2'/true.", newTerms.get(0).getTerm().equals("a2") && newTerms.get(0).isActive());
		assertTrue("c2.isActive() returned " + c2.getTerm() + ", expected false.", !c2.isActive());
		assertTrue("newTerms.contains(c2) returned " + newTerms.contains(c2) + ", expected false.", !newTerms.contains(c2));
		assertTrue("d2.isActive() returned " + d2.getTerm() + ", expected false.", !d2.isActive());
		assertTrue("newTerms.contains(d2) returned " + newTerms.contains(d2) + ", expected false.", !newTerms.contains(d2));
	}
}
