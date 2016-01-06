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
package com.restservice.dto;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataGroupingResult {

	public class Series {
		@JsonProperty("name")
		private String name;
		@JsonProperty("data")
		ArrayList<double[]> data;
		@JsonProperty("ids")
		ArrayList<String> ids;
		
		public Series() {
			data = new ArrayList<double[]>();
			ids = new ArrayList<String>();
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public ArrayList<double[]> getData() {
			return data;
		}
		public void setData(ArrayList<double[]> data) {
			this.data = data;
		}
		public ArrayList<String> getIds() {
			return ids;
		}
		public void setIds(ArrayList<String> ids) {
			this.ids = ids;
		}
	}
	
	// Attributes
	@JsonProperty("series")
	private ArrayList<Series> series;
	@JsonProperty("total_count")
	private int total_count;
	
	
	
	public int getTotal_count() {
		return total_count;
	}

	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}

	public void setSeries(ArrayList<Series> series) {
		this.series = series;
	}

	public ArrayList<Series> getSeries() {
		return series;
	}

	public DataGroupingResult(int[] clusterResult, double[][] coordinatesMDS, ArrayList<String> tweetIds) {
		HashMap<Integer, Integer> clusterToIndex = new HashMap<Integer, Integer>();
		
		series = new ArrayList<Series>();
		
		for (int i = 0; i < clusterResult.length; ++i) {
			int currentCluster = clusterResult[i];
			if (clusterToIndex.containsKey(currentCluster)) {
				series.get(clusterToIndex.get(currentCluster)).getData().add(coordinatesMDS[i]);
				series.get(clusterToIndex.get(currentCluster)).getIds().add(tweetIds.get(i));
			}
			else {
				clusterToIndex.put(currentCluster, series.size());
				Series newSeries = new Series();
				newSeries.setName("Cluster " + (currentCluster + 1));
				newSeries.getData().add(coordinatesMDS[i]);
				newSeries.getIds().add(tweetIds.get(i));
				series.add(newSeries);
			}
		}
		
		total_count = clusterResult.length;
	}
}
