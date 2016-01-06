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
package com.news;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import com.restservice.dto.NewsItem;

/**
 * 
 * 
 * 
 * @author olaf
 * 
 */
public class FeedFetcherThread implements Callable<ArrayList<NewsItem>> {

	private String url;
	private String provider;

	private static final String RSS_encoding = "UTF-8";
	private static final String RSS_titleIdentifier = "title";
	private static final String RSS_urlIdentifier = "link";
	private static final String RSS_textIdentifier = "description";
	private static final String RSS_itemIdentifier = "item";

	public FeedFetcherThread() {

	}

	public FeedFetcherThread(String provider, String url) {
		this.provider = provider;
		this.url = url;
	}

	@Override
	public ArrayList<NewsItem> call() {
		try {
			return fetchFeeds();
		} catch (XMLStreamException e) {
			// XMLStreamException empty results
			return null;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<NewsItem> fetchFeeds() throws IOException,
			XMLStreamException {
		ArrayList<NewsItem> feedItems = new ArrayList<NewsItem>();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = new URL(url).openStream();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in,
				RSS_encoding);
		NewsItem item = new NewsItem(this.provider);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				if (event.asStartElement().getName().getLocalPart() == RSS_titleIdentifier) {
					item.setTitle(getEventString(eventReader));
				} else if (event.asStartElement().getName().getLocalPart() == RSS_urlIdentifier) {
					item.setUrl(getEventString(eventReader));
				} else if (event.asStartElement().getName().getLocalPart() == RSS_textIdentifier) {
					item.setText(getEventString(eventReader));
				}
			} else if (event.isEndElement()
					&& event.asEndElement().getName().getLocalPart() == RSS_itemIdentifier) {
				feedItems.add(item);
				//System.out.println(item.toString());
				item = new NewsItem(this.provider);
			}
		}
		return feedItems;
	}

	private String getEventString(XMLEventReader eventReader)
			throws XMLStreamException {
		XMLEvent e = eventReader.nextEvent();
		String s = "";
		while (!e.isEndElement()) {
			if (e instanceof Characters) {
				s = s + e.asCharacters().getData();
			}
			e = eventReader.nextEvent();
		}
		// System.out.println(e.toString());
		return s;
	}
}
