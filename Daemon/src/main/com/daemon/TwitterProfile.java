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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Simple class for containing relevant Twitter profile (login data) information.
 * @author Torsten
 */
public class TwitterProfile {
	private String _consumerKey;
	private String _consumerSecret;
	private String _accessToken;
	private String _accessTokenSecret;
	
	private DateTime _lastUsed = null;
	
	private int _usedRateLimit = 0;
	
	private boolean _isInUse = false;
	
	private String _profileName = null;
	
	private String _screenName = null;
	
	private Twitter _twitter = null;
	
	private DaemonProperties _props = null;
	
	public TwitterProfile(String name, String screenName, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, DaemonProperties properties) {
		_profileName = name;
		_screenName = screenName;
		_consumerKey = consumerKey;
		_consumerSecret = consumerSecret;
		_accessToken = accessToken;
		_accessTokenSecret = accessTokenSecret;
		_props = properties;
		
		// Create Twitter object
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setOAuthAccessToken(getAccessToken())
			.setOAuthAccessTokenSecret(getAccessTokenSecret())
			.setOAuthConsumerKey(getConsumerKey())
			.setOAuthConsumerSecret(getConsumerSecret());
		_twitter = new TwitterFactory(conf.build()).getInstance();
	}
	
	public String getConsumerKey() {
		return _consumerKey;
	}
	
	public String getConsumerSecret() {
		return _consumerSecret;
	}
	
	public String getAccessToken() {
		return _accessToken;
	}
	
	public String getAccessTokenSecret() {
		return _accessTokenSecret;
	}
	
	public void setLastUsed(DateTime lastUsed) {
		_lastUsed = lastUsed;
	}
	
	public DateTime getLastUsed() {
		return _lastUsed;
	}
	
	public void setUsedRateLimit(int usedRateLimit) {
		_usedRateLimit = usedRateLimit;
	}
	
	public int getUsedRateLimit() {
		return _usedRateLimit;
	}
	
	public void setIsInUse(boolean isInUse) {
		_isInUse = isInUse;
	}
	
	public boolean isInUse() {
		return _isInUse;
	}
	
	public void setName(String name) {
		_profileName = name;
	}
	
	public String getName() {
		return _profileName;
	}
	
	public void setScreenName(String name) {
		_screenName = name;
	}
	
	public String getScreenName() {
		return _screenName;
	}
	
	public Twitter getTwitterObject() {
		return _twitter;
	}

	/**
	 * Get Twitter search rate limit for endpoint /search/tweets minus
	 * the given rate limit buffer (mentioned in the properties file).
	 * 
	 * @return Rate limit left
	 * @throws TwitterException  Thrown if there is a problem with Twitter.
	 */
	public int getSearchRateLimit() throws TwitterException {
		// Check our rate limit status
		Map<String, RateLimitStatus> rateLimitStatus = _twitter.getRateLimitStatus();
		RateLimitStatus status = rateLimitStatus.get("/search/tweets");

		return status.getRemaining() - _props.rateLimitBuffer;
	}
	
	/**
	 * Creates a new Twitter profile from the data specified in the file reflected by
	 * the given filename.
	 * @param filename The file that contains the Twitter profile data.
	 * @param properties The properties file to be used.
	 * @return Returns a valid Twitter profile.
	 * @throws IllegalArgumentException Thrown, if it is impossible to create
	 * a valid Twitter profile from the given filename.
	 */
	public static TwitterProfile fromFilename(String filename, DaemonProperties properties) throws IllegalArgumentException {
		Properties props = new Properties();
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(filename);
			props.load(fis);
			fis.close();
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("The filename " + filename + " does not reflect a Twitter properties file");
		}

		TwitterProfile tp = new TwitterProfile(
			props.getProperty("profile.name"),
			props.getProperty("profile.screenName"),
			props.getProperty("oauth.consumerKey"),
			props.getProperty("oauth.consumerSecret"),
			props.getProperty("oauth.accessToken"),
			props.getProperty("oauth.accessTokenSecret"),
			properties
		);
		
		// If one property is null, then that property was missing in the properties file
		if (
				tp.getName() == null ||
				tp.getScreenName() == null ||
				tp.getConsumerKey() == null ||
				tp.getConsumerSecret() == null ||
				tp.getAccessToken() == null ||
				tp.getAccessTokenSecret() == null
		) {
			throw new IllegalArgumentException("The specified properties file does not contail all necessary data");
		}
		
		return tp;
	}
}
