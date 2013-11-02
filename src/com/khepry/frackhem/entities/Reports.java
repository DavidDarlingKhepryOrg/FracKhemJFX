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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetRequest;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.khepry.utilities.GenericUtilities;
//import com.npstrandberg.simplemq.MessageInput;



import com.npstrandberg.simplemq.MessageInput;
import com.npstrandberg.simplemq.MessageQueue;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class Reports<E> implements ObservableList<E> {
	
	private ObservableList<E> list = FXCollections.observableArrayList();
	
	private String indexFolderPath = "indexes/reports";
	private String indexFields = "rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,rptFractureDate,toxRecognized,toxSuspected";
	private String levelFields = "rptAPI,rptCounty,rptDatum,rptFractureDate,rptLatLng,rptLatitude,rptLongitude,rptOperator,rptProdType,rptPdfSeqId,rptPublishedDate,rptSeqId,rptState,rptTWV,rptTVD,rptWellName";
	private String statsFields = "rptCounty:County,rptOperator:Operator,rptProdType:ProdType,rptState:State";
	private String taxonomyFolderPath = "taxonomies/reports";
	private Integer progressInterval = 10000;

	private String casEdfIdFieldName =  "chmCasEdfId";

	private Boolean outputToSystemOut = Boolean.TRUE;
	private Boolean outputToSystemErr = Boolean.TRUE;
	private Boolean outputDebugInfo = Boolean.FALSE;
	private Boolean outputToMsgQueue = Boolean.FALSE;
	
	private MessageQueue progressMessageQueue;

	List<Document> documents = new ArrayList<>();
	
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;
	private Analyzer analyzer;
	private TaxonomyReader taxonomyReader;
	
	private Boolean initialized = Boolean.FALSE;

	public Reports() {
		
	}
	
	public Reports(List<Document> documents) {
		loadViaDocuments(documents);
	}
	
	public QueryResult initialize(
			String indexFolderPath,
			String taxonomyFolderPath)
	{
		QueryResult queryResult = new QueryResult();
		
		String message = "";
		
		File indexFolder = new File(indexFolderPath);
		if (!indexFolder.exists()) {
			message = "Index path does not exist: " + indexFolderPath;
			queryResult.setNotValid(true);
			queryResult.setMessage(message);
			if (outputToSystemErr) {
				System.err.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
		}

		File taxonomyFolder = new File(taxonomyFolderPath);
		if (!taxonomyFolder.exists()) {
			message = "Taxonomy path does not exist: " + taxonomyFolderPath;
			queryResult.setNotValid(true);
			queryResult.setMessage(message);
			if (outputToSystemErr) {
				System.err.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
		}

		if (indexFolder.exists() && taxonomyFolder.exists()) {
			try {
				indexReader = DirectoryReader.open(FSDirectory.open(indexFolder));
				indexSearcher = new IndexSearcher(indexReader);
				analyzer = new StandardAnalyzer(Version.LUCENE_44);
				taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(taxonomyFolder));
				initialized = true;
			} catch (IOException e) {
				message = e.getMessage();
				if (outputToSystemErr) {
					System.err.println(message);
				}
				if (outputToMsgQueue) {
					progressMessageQueue.send(new MessageInput(message));
				}
			}
		}
		
		return queryResult;
	}
	
	public void terminate() {
		if (analyzer != null) {
			analyzer.close();
		}
		try {
			if (indexReader != null) {
				indexReader.close();
			}
			if (taxonomyReader != null) {
				taxonomyReader.close();
			}
		} catch (IOException e) {
			String message = e.getMessage();
			if (outputToSystemErr) {
				System.err.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
		}
	}
	
	public void loadViaDocuments(List<Document> documents) {
		list.clear();
		for (Document document : documents) {
			Report report = new Report(document);
			list.add((E)report);
		}
	}

	
	public List<TableColumn<?,?>> getTableColumns() {
		List<TableColumn<?,?>> list = new ArrayList<>();
		String properties = "SeqId,rptSeqId;Pdf\nSeqId,rptPdfSeqId;API,rptAPI;Fracture\nDate,rptFractureDate;State,rptState;County,rptCounty;Operator,rptOperator;Well Name,rptWellName;Prod\nType,rptProdType;TVD,rptTVD;TWV,rptTWV;Published Date,rptPublishedDate;Lat/Lng,rptLatLng;Datum,rptDatum;Toxicity\nRecognized,toxRecognized;Toxicity\nSuspected,toxSuspected";
		String[] keyValuePairs = properties.split(";");
		for (String keyValuePair : keyValuePairs) {
			String[] kvs = keyValuePair.split(",");
			TableColumn tableColumn = new TableColumn(kvs[0] + "\n(" + kvs[1] + ")");
			tableColumn.setCellValueFactory(new PropertyValueFactory<Report, String>(kvs[1]));
			list.add(tableColumn);
		}
		return list;
	}

	
	public void indexViaLucene(
			String textPath,
			String textColSeparator,
			Map<String, Toxicity> toxicities,
			String...parseFields) throws IOException {

		String message;
		
		message = "Start Indexing Reports via Lucene...";
		if (outputToSystemOut) {
			System.out.println(message);
		}
		if (outputToMsgQueue) {
			progressMessageQueue.send(new MessageInput(message));
		}
		
		File textFile = new File(textPath);
		if (textFile.exists()) {

			File indexFolder = new File(indexFolderPath);
			
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			}
			
			File taxonomyFolder = new File(taxonomyFolderPath);
			if (!taxonomyFolder.exists()) {
				taxonomyFolder.mkdir();
			}
			
			if (indexFolder.exists() && taxonomyFolder.exists()) {
				
				deleteFolder(indexFolder);
				if (!indexFolder.exists()) {
					indexFolder.mkdir();
				}
				
				deleteFolder(taxonomyFolder);
				if (!taxonomyFolder.exists()) {
					taxonomyFolder.mkdir();
				}

				Map<String, String> mapBreakFields = new LinkedHashMap<>();
				Map<String, String> mapIndexFields = new LinkedHashMap<>();
				Map<String, String> mapLevelFields = new LinkedHashMap<>();
				Map<String, String> mapStatsFields = new LinkedHashMap<>();
				Map<String, Integer> mapColIndexes = new LinkedHashMap<>();
				
				String[] pieces;
				String[] tuples;
				
				pieces = indexFields.split(",");
				for (String indexField : pieces) {
					mapIndexFields.put(indexField, "");
				}

				pieces = levelFields.split(",");
				for (String levelField : pieces) {
					mapBreakFields.put(levelField, "");
					mapLevelFields.put(levelField, "");
				}

				pieces = statsFields.split(",");
				for (String statField : pieces) {
					tuples = statField.split(":"); 
					mapStatsFields.put(tuples[0], tuples.length > 1 ? tuples[1] : tuples[0]);
				}

				Map<String, Map<String, String>> mapToxValues = new LinkedHashMap<>();
				for (String parseField : parseFields) {
					mapToxValues.put(parseField, new TreeMap<String, String>());
				}
				
				SimpleFSDirectory indexDirectory = new SimpleFSDirectory(indexFolder);
				SimpleFSDirectory taxonomyDirectory = new SimpleFSDirectory(taxonomyFolder);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
				IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
				TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDirectory, OpenMode.CREATE);
				FacetFields facetFields = new FacetFields(taxonomyWriter);
				
				List<CategoryPath> taxonomyCategories = new ArrayList<>();
			
				String line;
				
				StringBuilder sbIndex = new StringBuilder();
				StringBuilder sbLevel = new StringBuilder();
				
				Integer outCount = 0;
				Integer rcdCount = 0;
				
				Boolean firstDataRecordHandled = false;

				BufferedReader br = new BufferedReader(new FileReader(textFile));
				while ((line = br.readLine()) != null) {
					rcdCount++;
					pieces = line.split(textColSeparator);
					if (rcdCount == 1) {
						int i = 0;
						for (String colHeader : pieces) {
							mapColIndexes.put(colHeader.trim(), i);
							i++;
						}
					}
					else {
						for (String key : mapLevelFields.keySet()) {
							if (mapColIndexes.containsKey(key)) {
								String value = pieces[mapColIndexes.get(key)].trim();
								// build up level-break values
								if (mapLevelFields.containsKey(key)) {
									mapLevelFields.put(key, value);
								}
							}
						}
						if (!firstDataRecordHandled) {
							mapBreakFields.putAll(mapLevelFields);
							firstDataRecordHandled = true;
						}
						// if there is a "level break"
						if (!mapLevelFields.equals(mapBreakFields)) {
							Document tgtDocument = new Document();
							for (Map.Entry<String, String> entry : mapBreakFields.entrySet()) {
								Field field = new TextField(entry.getKey(), entry.getValue() , Store.YES);
								tgtDocument.add(field);
							}
							for (Map.Entry<String, Map<String, String>> toxEntry : mapToxValues.entrySet()) {
								String fieldName = toxEntry.getKey();
								String fieldValue = GenericUtilities.joinString(toxEntry.getValue().values(), " ");
								// System.out.println(fieldName + ": " + fieldValue);
								sbIndex.append(fieldValue);
								sbIndex.append(" ");
								tgtDocument.add(new TextField(fieldName, fieldValue, Store.YES));
								// build up "Toxicity" taxonomy categories
								for (String value : fieldValue.replace(" ", ",").split(",")) {
									if (!value.trim().equals("")) {
										taxonomyCategories.add(new CategoryPath(fieldName, "Toxicity", value));
									}
								}
								// build up "stats" taxonomy categories
								for (String statsKey : mapStatsFields.keySet()) {
									if (mapLevelFields.containsKey(statsKey)) {
										String levelValue = mapLevelFields.get(statsKey);
										if (!statsKey.trim().equals("") && !levelValue.trim().equals("")) {
											taxonomyCategories.add(new CategoryPath("Reports", statsKey, levelValue));
										}
									}
								}
							}
							tgtDocument.add(new TextField("text", sbIndex.toString().trim(), Store.NO));
							if (taxonomyCategories.size() > 0) {
								facetFields.addFields(tgtDocument,	taxonomyCategories);
								// System.out.println("Taxonomies added: " +
								// taxonomyCategories.size());
							}
							indexWriter.addDocument(tgtDocument);
							outCount++;
							sbIndex.setLength(0);
							for (String key : mapToxValues.keySet()) {
								mapToxValues.get(key).clear();
							}
							taxonomyCategories.clear();
							mapBreakFields.putAll(mapLevelFields);
						}
						// build up text index values
						for (String key : mapLevelFields.keySet()) {
							if (mapColIndexes.containsKey(key)) {
								String value = pieces[mapColIndexes.get(key)].trim();
								if (!value.equals("")) {
									// build up 'text' field index value
									if (mapIndexFields.containsKey(key)) {
										sbIndex.append(value);
										sbIndex.append(" ");
									}
								}
							}
						}
						// build up toxicity values for later level-break use
						if (mapColIndexes.containsKey(casEdfIdFieldName)) {
							Toxicity toxicity = toxicities.get(pieces[mapColIndexes.get(casEdfIdFieldName)].trim());
							if (toxicity != null) {
								// build up recognized toxicity values
								String [] toxRValues = toxicity.getToxRecognized().split(",");
								for (String toxValue : toxRValues) {
									if (!toxValue.equals("")) {
										if (!mapToxValues.get("toxRecognized").containsKey(toxValue)) {
											mapToxValues.get("toxRecognized").put(toxValue, toxValue);
										}
									}
								}
								// build up suspected toxicity values
								String [] toxSValues = toxicity.getToxSuspected().split(",");
								for (String toxValue : toxSValues) {
									if (!toxValue.equals("")) {
										if (!mapToxValues.get("toxSuspected").containsKey(toxValue)) {
											mapToxValues.get("toxSuspected").put(toxValue, toxValue);
										}
									}
								}
							}
						}
						if (progressInterval > 0 && rcdCount % progressInterval == 0) {
							message = "Records indexed: " + rcdCount;
							if (outputToSystemOut) {
								System.out.println(message);
							}
							if (outputToMsgQueue) {
								progressMessageQueue.send(new MessageInput(message));
							}
						}
					}
				}
				br.close();
				// handle end-of-file processing
				Document tgtDocument = new Document();
				for (Map.Entry<String, String> entry : mapBreakFields.entrySet()) {
					Field field = new TextField(entry.getKey(), entry.getValue() , Store.YES);
					tgtDocument.add(field);
				}
				for (Map.Entry<String, Map<String, String>> toxEntry : mapToxValues.entrySet()) {
					String fieldName = toxEntry.getKey();
					String fieldValue = GenericUtilities.joinString(toxEntry.getValue().values(), " ");
					// System.out.println(fieldName + ": " + fieldValue);
					sbIndex.append(fieldValue);
					sbIndex.append(" ");
					tgtDocument.add(new TextField(fieldName, fieldValue, Store.YES));
					// build up "Toxicity" taxonomy categories
					for (String value : fieldValue.replace(" ", ",").split(",")) {
						if (!value.trim().equals("")) {
							taxonomyCategories.add(new CategoryPath(fieldName, "Toxicity", value));
						}
					}
					// build up "stats" taxonomy categories
					for (String statsKey : mapStatsFields.keySet()) {
						if (mapLevelFields.containsKey(statsKey)) {
							String levelValue = mapLevelFields.get(statsKey);
							if (!statsKey.trim().equals("") && !levelValue.trim().equals("")) {
								taxonomyCategories.add(new CategoryPath("Reports", statsKey, levelValue));
							}
						}
					}
				}
				tgtDocument.add(new TextField("text", sbIndex.toString().trim(), Store.NO));
				if (taxonomyCategories.size() > 0) {
					facetFields.addFields(tgtDocument,	taxonomyCategories);
					// System.out.println("Taxonomies added: " +
					// taxonomyCategories.size());
				}
				indexWriter.addDocument(tgtDocument);
				outCount++;
				message = "Records processed: " + rcdCount;
				if (outputToSystemOut) {
					System.out.println(message);
				}
				if (outputToMsgQueue) {
					progressMessageQueue.send(new MessageInput(message));
				}
				message = "Records indexed: " + outCount;
				if (outputToSystemOut) {
					System.out.println(message);
				}
				if (outputToMsgQueue) {
					progressMessageQueue.send(new MessageInput(message));
				}
				
				sbIndex.setLength(0);
				sbIndex.trimToSize();
				
				sbLevel.setLength(0);
				sbLevel.trimToSize();

				mapToxValues.clear();

				indexWriter.commit();
				indexWriter.forceMerge(1);
				indexWriter.close();
				
				analyzer.close();
				indexDirectory.close();
				
				taxonomyWriter.commit();
				taxonomyWriter.close();
				taxonomyDirectory.close();
			} else {
				message = "Lucene Index Folder: " + indexFolder	+ " or Lucene Taxonomy folder: " + taxonomyFolder + " does not exist!";
				if (outputToSystemErr) {
					System.err.println(message);
				}
				if (outputToMsgQueue) {
					progressMessageQueue.send(new MessageInput(message));
				}
			}
			message = "Ended Indexing Reports via Lucene!";
			if (outputToSystemOut) {
				System.out.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
		}
	}

	public QueryResult facetViaLucene(
			String queryField,
			String queryValue,
			Integer maxDocs,
			String sortOrder,
			Boolean allowLeadingWildcard) throws IOException, ParseException {

		QueryResult queryResult = new QueryResult();
		
		list.clear();
		documents.clear();

		String message;
		
		String[] sortColumns = sortOrder.split(",");

		if (!initialized) {
			queryResult = initialize(indexFolderPath, taxonomyFolderPath);
		}
		
		if (!queryResult.isNotValid()) {
			QueryParser parser = new QueryParser(Version.LUCENE_44,	queryField, analyzer);
			parser.setAllowLeadingWildcard(allowLeadingWildcard);
			Query query = parser.parse(queryValue);
			SortField[] sortFields = new SortField[sortColumns.length];
			String[] pieces;
			for (int i = 0; i < sortColumns.length; i++) {
				pieces = sortColumns[i].split(":");
				if (pieces.length > 1) {
					switch (pieces[1].toLowerCase()) {
					case "integer":
						sortFields[i] = new SortField(sortColumns[i], SortField.Type.INT);
						break;
					default:
						sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
						break;
					}
				} else {
					sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
				}
			}
			Sort sort = new Sort(sortFields);
			queryResult.setTopFieldCollector(TopFieldCollector.create(sort, maxDocs, true, false, false, false));

			List<FacetRequest> facetRequests = new ArrayList<>();
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxRecognized", "Toxicity"), 200));
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxSuspected", "Toxicity"), 200));
			FacetSearchParams facetSearchParams = new FacetSearchParams(facetRequests);
			queryResult.setFacetsCollector(FacetsCollector.create(facetSearchParams, indexReader, taxonomyReader));
			
			indexSearcher.search(query, MultiCollector.wrap(queryResult.getTopFieldCollector(), queryResult.getFacetsCollector()));
//			
//			List<Document> documents = new ArrayList<>();
//			for (ScoreDoc scoreDoc : queryResult.getTopFieldCollector().topDocs().scoreDocs) {
//				Document document = indexSearcher.doc(scoreDoc.doc);
//				documents.add(document);
//				Report report = new Report(document);
//				list.add((E)report);
//				if (outputDebugInfo) {
//					for (IndexableField field : document.getFields()) {
//						System.out.print(field.stringValue());
//						System.out.print("\t");
//					}
//					System.out.println();
//				}
//			}
//			queryResult.setDocuments(documents);
			
			for (FacetResult facetResult : queryResult.getFacetsCollector().getFacetResults()) {
				if (outputDebugInfo) {
					for (FacetResultNode node0 : facetResult.getFacetResultNode().subResults) {
						if (node0.label.toString().indexOf(",") == -1) {
							System.out.println(node0.label + ": " + node0.value);
						}
	//					for (FacetResultNode node1 : node0.subResults) {
	//						System.out.println(node1.label + ": " + node1.value);
	//					}
					}
				}
			}
		}
		
		return queryResult;
	}

	
	public QueryResult queryViaLucene(
			String queryField,
			String queryValue,
			Integer maxDocs,
			String sortOrder,
			Boolean allowLeadingWildcard) throws IOException, ParseException {

		QueryResult queryResult = new QueryResult();
		
		list.clear();
		documents.clear();

		String message;
		
		String[] sortColumns = sortOrder.split(",");

		if (!initialized) {
			queryResult = initialize(indexFolderPath, taxonomyFolderPath);
		}
		
		if (!queryResult.isNotValid()) {
			QueryParser parser = new QueryParser(Version.LUCENE_44,	queryField, analyzer);
			parser.setAllowLeadingWildcard(allowLeadingWildcard);
			Query query = parser.parse(queryValue);
			SortField[] sortFields = new SortField[sortColumns.length];
			String[] pieces;
			for (int i = 0; i < sortColumns.length; i++) {
				pieces = sortColumns[i].split(":");
				if (pieces.length > 1) {
					switch (pieces[1].toLowerCase()) {
					case "integer":
						sortFields[i] = new SortField(sortColumns[i], SortField.Type.INT);
						break;
					default:
						sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
						break;
					}
				} else {
					sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
				}
			}
			Sort sort = new Sort(sortFields);
			queryResult.setTopFieldCollector(TopFieldCollector.create(sort, maxDocs, true, false, false, false));

			List<FacetRequest> facetRequests = new ArrayList<>();
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxRecognized", "Toxicity"), 200));
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxSuspected", "Toxicity"), 200));
			FacetSearchParams facetSearchParams = new FacetSearchParams(facetRequests);
			queryResult.setFacetsCollector(FacetsCollector.create(facetSearchParams, indexReader, taxonomyReader));
			
			indexSearcher.search(query, MultiCollector.wrap(queryResult.getTopFieldCollector(), queryResult.getFacetsCollector()));
			
			List<Document> documents = new ArrayList<>();
			for (ScoreDoc scoreDoc : queryResult.getTopFieldCollector().topDocs().scoreDocs) {
				Document document = indexSearcher.doc(scoreDoc.doc);
				documents.add(document);
				Report report = new Report(document);
				list.add((E)report);
				if (outputDebugInfo) {
					for (IndexableField field : document.getFields()) {
						System.out.print(field.stringValue());
						System.out.print("\t");
					}
					System.out.println();
				}
			}
			queryResult.setDocuments(documents);
			
			for (FacetResult facetResult : queryResult.getFacetsCollector().getFacetResults()) {
				if (outputDebugInfo) {
					for (FacetResultNode node0 : facetResult.getFacetResultNode().subResults) {
						if (node0.label.toString().indexOf(",") == -1) {
							System.out.println(node0.label + ": " + node0.value);
						}
	//					for (FacetResultNode node1 : node0.subResults) {
	//						System.out.println(node1.label + ": " + node1.value);
	//					}
					}
				}
			}
		}
		
		return queryResult;
	}
	
	
	public static void deleteFolder(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				file.delete();
			}
			folder.delete();
		}
	}
	
	
	public String getIndexFolderPath() {
		return indexFolderPath;
	}

	public void setIndexFolderPath(String indexFolderPath) {
		this.indexFolderPath = indexFolderPath;
		initialized = false;
	}

	public String getIndexFields() {
		return indexFields;
	}

	public void setIndexFields(String indexFields) {
		this.indexFields = indexFields;
	}

	public String getStatsFields() {
		return statsFields;
	}

	public void setStatsFields(String statsFields) {
		this.statsFields = statsFields;
	}

	public String getLevelFields() {
		return levelFields;
	}

	public void setLevelFields(String levelFields) {
		this.levelFields = levelFields;
	}

	public String getTaxonomyFolderPath() {
		return taxonomyFolderPath;
	}

	public void setTaxonomyFolderPath(String taxonomyFolderPath) {
		this.taxonomyFolderPath = taxonomyFolderPath;
		initialized = false;
	}
	
	public String getCasEdfIdFieldName() {
		return casEdfIdFieldName;
	}

	public void setCasEdfIdFieldName(String casEdfIdFieldName) {
		this.casEdfIdFieldName = casEdfIdFieldName;
	}

	public Integer getProgressInterval() {
		return progressInterval;
	}

	public void setProgressInterval(Integer progressInterval) {
		this.progressInterval = progressInterval;
	}

	public Boolean getOutputToSystemErr() {
		return outputToSystemErr;
	}

	public void setOutputToSystemErr(Boolean outputToSystemErr) {
		this.outputToSystemErr = outputToSystemErr;
	}

	public Boolean getOutputToSystemOut() {
		return outputToSystemOut;
	}

	public void setOutputToSystemOut(Boolean outputToSystemOut) {
		this.outputToSystemOut = outputToSystemOut;
	}

	public Boolean getOutputDebugInfo() {
		return outputDebugInfo;
	}

	public void setOutputDebugInfo(Boolean outputDebugInfo) {
		this.outputDebugInfo = outputDebugInfo;
	}

	public Boolean getOutputToMsgQueue() {
		return outputToMsgQueue;
	}

	public void setOutputToMsgQueue(Boolean outputToMsgQueue) {
		this.outputToMsgQueue = outputToMsgQueue;
	}
	
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}


	@Override
	public boolean add(E e) {
		return list.add(e);
	}

	@Override
	public void add(int index, E element) {
		list.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return list.addAll(index, c);
	}

	@Override
	public void clear() {
		list.clear();
		documents.clear();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public E remove(int index) {
		return list.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		return list.set(index, element);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@Override
	public void addListener(InvalidationListener listener) {
		list.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		list.removeListener(listener);
	}

	@Override
	public boolean addAll(E... elements) {
		return list.addAll(elements);
	}

	@Override
	public void addListener(ListChangeListener<? super E> listener) {
		list.addListener(listener);
	}

	@Override
	public void remove(int from, int to) {
		list.remove(from, to);
	}

	@Override
	public boolean removeAll(E... elements) {
		return list.removeAll(elements);
	}

	@Override
	public void removeListener(ListChangeListener<? super E> listener) {
		list.removeListener(listener);
	}

	@Override
	public boolean retainAll(E... elements) {
		return list.retainAll(elements);
	}

	@Override
	public boolean setAll(E... elements) {
		return list.setAll(elements);
	}

	@Override
	public boolean setAll(Collection<? extends E> col) {
		return list.setAll(col);
	}

	public MessageQueue getProgressMessageQueue() {
		return progressMessageQueue;
	}

	public void setProgressMessageQueue(MessageQueue progressMessageQueue) {
		this.progressMessageQueue = progressMessageQueue;
	}

}
