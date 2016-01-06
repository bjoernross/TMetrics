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
package com.tmetrics.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.tmetrics.util.Localization;

/**
 * A thread-safe class for logging information to a file. The file
 * will be truncated if it gets to long.
 * @author Torsten
 */
public class Logger {
	private String _filename;
	
	private FileWriter _writer;
	
	private int _maxEntries;
	
	// The number of lines that should be truncated if the file gets to long.
	private int _truncEntries;
	
	/**
	 * Creates a new logging for the given file name. If the name contains '/'s,
	 * a directory will be created as expected.
	 * @param filename The filename of the logging file.
	 */
	public Logger(String filename) {
		this(filename, 1024 * 128, 1024 * 16);
	}
	
	/**
	 * Creates a new logging for the given file name. If the name contains '/'s,
	 * a directory will be created as expected.
	 * @param filename The filename of the logging file.
	 * @param maxEntries The number of maximum lines that should be contained
	 * in the logging file.
	 * @param truncEntries The number of lines that should be truncated.
	 * from the logging file.
	 */
	public Logger(String filename, int maxEntries, int truncEntries) {
		_filename = filename;
		
		try {
			int indexLastDirDelim = filename.lastIndexOf("/");
			String dirNames = filename.substring(0, indexLastDirDelim);
			
			File dirs = new File(dirNames);
			
			// Create directories if they are missing
			// The last filename is the file itself and not a directory
			dirs.mkdirs();
			
			// Create the logging file
			_writer = new FileWriter(filename, true);
		}
		catch (IOException ex) {
			System.err.println("Error: Logger " + _filename + " will not work.");
		}
		
		_maxEntries = maxEntries;
		
		_truncEntries = truncEntries;
	}
	
	/**
	 * Logs a message to the file. If a name is specified, the name will be added
	 * in front of the message.
	 * @param msg The message to be logged.
	 * @param name A name to be associated with the message. Can be null.
	 */
	public synchronized void log(String msg, String name) {
		if (_writer == null) {
			System.err.println("Logger for " + _filename + " has already been closed.");
		}
		
		Scanner s = null;
		
		try {
			// Load the file and count its lines
			s = new Scanner(new File(_filename));
			List<String> lineList = new LinkedList<String>();
			
			while(s.hasNextLine()) {
				lineList.add(s.nextLine());
			}
			
			// Do we have to truncate?
			if (lineList.size() > _maxEntries) {
				// Truncate
				List<String> truncList = new LinkedList<String>();
				
				for (int i = _truncEntries; i < lineList.size(); ++i) {
					truncList.add(lineList.get(i));
				}
				
				_writer.close();
				
				// Delete the file and recreate it
				new File(_filename).delete();
				
				// Reopen and fill it with data
				_writer = new FileWriter(_filename);
				
				for (String line : truncList) {
					_writer.write(line + "\r\n");
				}
			}
			
			_writer.write(formatMessage(msg, name) + "\r\n");
			_writer.write("\r\n");
		}
		catch (IOException ex) {
			System.err.println("Error: Cannot close " + _filename + ".");
		}
		finally {
			if (s != null) {
				s.close();
			}
		
			try {
				_writer.flush();
			}
			catch (IOException _) {}
		}
	}
	
	/**
	 * Logs a message to the file.
	 * @param msg The message to be logged.
	 */
	public synchronized void log(String msg) {
		log(msg, null);
	}
	
	/**
	 * Logs a stack trace to the file. If a name is specified, the name will be added
	 * in front of the stack trace.
	 * @param ex The stack trace to be logged.
	 * @param name A name to be associated with the message. Can be null.
	 */
	public synchronized void logStackTrace(Exception ex, String name) {
		String text = formatMessage("Stack trace:\r\n" + ex.getClass().getName() + ": " + ex.getMessage(), name) + "\r\n";
		StackTraceElement[] stackTrace = ex.getStackTrace();
		
		// Rebuild java stack trace format
		for (StackTraceElement elem : stackTrace) {
			text += "  at " + elem.getClassName() + "." + elem.getMethodName() + "(" + elem.getFileName() + ":" + elem.getLineNumber() + ")\r\n";
		}
		
		log(text + "\r\n");
		
		try {
			_writer.write("\r\n");
		}
		catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	/**
	 * Closes the file handle for this logging file.
	 */
	public synchronized void close() {
		try {
			_writer.close();
			_writer = null;
		}
		catch (IOException ex) {}
	}
	
	/**
	 * Logs a stack trace to the file.
	 * @param ex The stack trace to be logged.
	 */
	public synchronized void logStackTrace(Exception ex) {
		logStackTrace(ex, null);
	}

	/**
	 * Formats the given message by adding a date time.
	 * @param message The message to be formatted.
	 * @param name The name to be added to the message. Can be null.
	 * @return The formatted message.
	 */
	private String formatMessage(String message) {
		return formatMessage(message, null);
	}
	
	/**
	 * Formats the given message by adding a date time and a name (if given).
	 * @param message The message to be formatted.
	 * @param name The name to be added to the message. Can be null.
	 * @return The formatted message.
	 */
	private String formatMessage(String message, String name) {
		String text = "";
		
		// Add the date
		text += Localization.DATEANDTIME_FORMATTER.format(new Date());
		
		// Add a name if specified
		if (name != null) {
			text += " [" + name + "]";
		}
		
		text += " " + message;
		
		return text;
	}
}
