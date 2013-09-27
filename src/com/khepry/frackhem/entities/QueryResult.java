/*******************************************************************************
 * Copyright 2013 Khepry Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.khepry.frackhem.entities;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldCollector;

public class QueryResult {

	private TopFieldCollector topFieldCollector;
	private FacetsCollector facetsCollector;
	private List<Document> documents = new ArrayList<>();
	
	private Long bgnTime = System.currentTimeMillis();
	
	public QueryResult() {
		
	}
	
	public QueryResult(
			TopFieldCollector topFieldCollector,
			FacetsCollector facetsCollector) {
		this.topFieldCollector = topFieldCollector;
		this.facetsCollector = facetsCollector;
	}
	
	public void clear(TopFieldCollector topFieldCollector) {
		this.topFieldCollector = topFieldCollector;
		facetsCollector.reset();
		documents.clear();
	}
	
	public String getCommentary() {
		return "Query returned " + getScoreDocsCount() + " total hits and " + getFacetsResultsSize() + " facet results in " + getElapsedSeconds("0.0000") + " seconds.";
	}
	
	public String getCommentary(String preface) {
		return preface + " " + getCommentary().toLowerCase();
	}
	
	public List<FacetResult> getFacetResults() throws IOException {
		List<FacetResult> facetResults = new ArrayList<>();
		for (FacetResult facetResult : facetsCollector.getFacetResults()) {
			facetResults.add(facetResult);
		}
		return facetResults;
	}
	
	public List<ScoreDoc> getScoreDocs() {
		List<ScoreDoc> scoreDocs = new ArrayList<>();
		for (ScoreDoc scoreDoc : topFieldCollector.topDocs().scoreDocs) {
			scoreDocs.add(scoreDoc);
		}
		return scoreDocs;
	}
	
	public Integer getScoreDocsCount() {
		return topFieldCollector.topDocs().totalHits;
	}
	
	public TopFieldCollector getTopFieldCollector() {
		return topFieldCollector;
	}

	public void setTopFieldCollector(TopFieldCollector topFieldCollector) {
		this.topFieldCollector = topFieldCollector;
	}

	public FacetsCollector getFacetsCollector() {
		return facetsCollector;
	}

	public void setFacetsCollector(FacetsCollector facetsCollector) {
		this.facetsCollector = facetsCollector;
	}
	
	public Integer getTotalHits() {
		return topFieldCollector.getTotalHits();
	}
	
	public Integer getFacetsResultsSize() {
		Integer size = 0;
		try {
			size = facetsCollector.getFacetResults().size();
		}
		catch (Exception ex) {
			size = 0;
		}
		return size;
	}

	public Long getElapsedTime() {
		return System.currentTimeMillis() - bgnTime;
	}

	public Double getElapsedSeconds() {
		return ((System.currentTimeMillis() - bgnTime) * 1.0 / 1000);
	}

	public String getElapsedSeconds(String decimalFormat) {
		String result = "Invalid DecimalFormat string provided!";
		try {
			result = new DecimalFormat(decimalFormat).format((System.currentTimeMillis() - bgnTime) * 1.0 / 1000);
		}
		catch (Exception ex) {
		}
		return result;
	}
	
	public Long getBgnTime() {
		return bgnTime;
	}

	public void setBgnTime(Long bgnTime) {
		this.bgnTime = bgnTime;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
}
