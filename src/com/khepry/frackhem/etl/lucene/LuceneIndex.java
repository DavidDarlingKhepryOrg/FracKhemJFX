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
package com.khepry.frackhem.etl.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.scene.control.TextArea;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.search.FacetRequest;
import org.apache.lucene.facet.search.FacetResult;
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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.khepry.frackhem.entities.Toxicity;
import com.khepry.utilities.GenericUtilities;
import com.npstrandberg.simplemq.MessageInput;
import com.npstrandberg.simplemq.MessageQueue;


public class LuceneIndex {

	private String txtPath = "data/2013_FracKhem_Reports.txt";
	private String colSeparator = "\t";
	private String indexPath = "lucene/reports";
	private String indexFields = "rptAPI,rptState,rptCounty,rptOperator,rptWellName,rptPdfSeqId,rptSeqId";
	private Integer progressInterval = 100;
	
	private MessageQueue progressMessageQueue;
	
	
	public LuceneIndex() {
		
	}
	
	public LuceneIndex(
			MessageQueue progressMessageQueue) {
		this.progressMessageQueue = progressMessageQueue;
	}
	

	public static void main(String[] args) throws IOException, ParseException {

		String txtPath = "data/2013_FracKhem_Reports.txt";
		String colSeparator = "\t";
		String indexPath = "lucene/reports";
		String indexFields = "rptAPI,rptState,rptCounty,rptOperator,rptWellName,rptPdfSeqId,rptSeqId,rptFractureDate";
		Integer progressInterval = 100;
		
		LuceneIndex luceneIndex = new LuceneIndex();
		
		luceneIndex.load(txtPath, colSeparator, indexPath, indexFields, progressInterval, new TextArea());
		
		List<Document> documents = luceneIndex.query(indexPath, "text", "Arkansas", 100, false, "");
		System.out.println("Documents found: " + documents.size());
		
		for (Document document : documents) {
			int i = 0;
			for (IndexableField field : document.getFields()) {
				i++;
				System.out.print(field.stringValue());
				if (i < document.getFields().size()) {
					System.out.print("|");
				}
			}
			System.out.println();
		}
	}

	
	public void load(
			String txtPath,
			String txtColSeparator,
			String indexPath,
			String indexFields,
			Integer progressInterval,
			TextArea results) throws IOException {

		String message;
		
		File csvFile = new File(txtPath);
		if (csvFile.exists()) {

			File indexFolder = new File(indexPath);
			
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			}
			
			if (indexFolder.exists()) {
				
				deleteLuceneIndex(indexFolder);
				
				if (!indexFolder.exists()) {
					indexFolder.mkdir();
				}

				List<String> colHeaders = new ArrayList<>();
				Map<String,String> mapFields = new LinkedHashMap<>();
				
				String[] pieces;
				pieces = indexFields.split(",");
				for (String indexField : pieces) {
					mapFields.put(indexField, indexField);
				}
				
				
				SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(indexFolder);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
				IndexWriter indexWriter = new IndexWriter(simpleFSDirectory, indexWriterConfig);
			
				String line;
				Integer rcdCount = 0;
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					rcdCount++;
					pieces = line.split(txtColSeparator);
					if (rcdCount == 1) {
						for (String colHeader : pieces) {
							colHeaders.add(colHeader.trim());
						}
					}
					else {
						if (pieces.length == colHeaders.size()) {
							sb.setLength(0);
							Document document = new Document();
							for (int i = 0; i < pieces.length; i++) {
								Field field = new TextField(colHeaders.get(i), pieces[i].trim(), Store.YES);
								document.add(field);
								if (mapFields.containsKey(colHeaders.get(i))) {
									if (!pieces[i].trim().equals("")) {
										sb.append(pieces[i].trim());
										sb.append(" ");
									}
								}
							}
							Field field = new TextField("text", sb.toString().trim(), Store.NO);
							document.add(field);
							indexWriter.addDocument(document);
							if (progressInterval > 0 && rcdCount % progressInterval == 0) {
								message = "Records indexed: " + rcdCount;
//								System.out.println(message);
								// results.appendText(System.lineSeparator() + message);
								progressMessageQueue.send(new MessageInput(message));
							}
						}
					}
				}
				br.close();
				message = "Records indexed: " + rcdCount;
//				System.out.println(message);
				// results.appendText(System.lineSeparator() + message);
				progressMessageQueue.send(new MessageInput(message));
				
				sb.setLength(0);
				sb.trimToSize();

				indexWriter.commit();
				indexWriter.forceMerge(1);
				indexWriter.close();
				
				analyzer.close();
				simpleFSDirectory.close();
			}
			else {
				message = "Lucene Index Folder: " + indexFolder + " does not exist!";
//				System.err.println(message);
				// results.setText(message);
				progressMessageQueue.send(new MessageInput(message));
			}
		}
	}
	
	public void load(
			String txtPath,
			String txtColSeparator,
			String indexPath,
			String indexFields,
			Integer progressInterval,
			TextArea results,
			Map<String, Toxicity> toxicities) throws IOException {

		String message;
		
		File csvFile = new File(txtPath);
		if (csvFile.exists()) {

			File indexFolder = new File(indexPath);
			
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			}
			
			if (indexFolder.exists()) {
				
				deleteLuceneIndex(indexFolder);
				
				if (!indexFolder.exists()) {
					indexFolder.mkdir();
				}

				List<String> colHeaders = new ArrayList<>();
				Map<String,String> mapFields = new LinkedHashMap<>();
				
				String[] pieces;
				pieces = indexFields.split(",");
				for (String indexField : pieces) {
					mapFields.put(indexField, indexField);
				}
				
				
				SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(indexFolder);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
				IndexWriter indexWriter = new IndexWriter(simpleFSDirectory, indexWriterConfig);
			
				String line;
				Integer rcdCount = 0;
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					rcdCount++;
					pieces = line.split(txtColSeparator);
					if (rcdCount == 1) {
						for (String colHeader : pieces) {
							colHeaders.add(colHeader.trim());
						}
					}
					else {
						if (pieces.length == colHeaders.size()) {
							sb.setLength(0);
							Document document = new Document();
							for (int i = 0; i < pieces.length; i++) {
								Field field = new TextField(colHeaders.get(i), pieces[i].trim(), Store.YES);
								document.add(field);
								if (mapFields.containsKey(colHeaders.get(i))) {
									if (!pieces[i].trim().equals("")) {
										sb.append(pieces[i].trim());
										sb.append(" ");
									}
								}
							}
							// append toxicity information to the document
							String toxCasEdfId = document.get("chmCasEdfId").trim();
							if (toxicities.containsKey(toxCasEdfId)) {
								Toxicity toxicity = toxicities.get(toxCasEdfId);
								document.add(new TextField("toxChemicalName", toxicity.getToxChemicalName().trim(), Store.YES));
								sb.append(toxicity.getToxChemicalName().trim());
								sb.append(" ");
								document.add(new TextField("toxRecognized", toxicity.getToxRecognized().trim(), Store.YES));
								sb.append(toxicity.getToxRecognized().trim());
								sb.append(" ");
								document.add(new TextField("toxSuspected", toxicity.getToxSuspected().trim(), Store.YES));
								sb.append(toxicity.getToxSuspected().trim());
								sb.append(" ");
							}
							else {
								document.add(new TextField("toxChemicalName", "", Store.YES));
								document.add(new TextField("toxRecognized", "", Store.YES));
								document.add(new TextField("toxSuspected", "", Store.YES));
							}
							Field field = new TextField("text", sb.toString().trim(), Store.NO);
							document.add(field);
							indexWriter.addDocument(document);
							if (progressInterval > 0 && rcdCount % progressInterval == 0) {
								message = "Records indexed: " + rcdCount;
//								System.out.println(message);
								// results.appendText(System.lineSeparator() + message);
								progressMessageQueue.send(new MessageInput(message));
							}
						}
					}
				}
				br.close();
				message = "Records indexed: " + rcdCount;
//				System.out.println(message);
				// results.appendText(System.lineSeparator() + message);
				progressMessageQueue.send(new MessageInput(message));
				
				sb.setLength(0);
				sb.trimToSize();

				indexWriter.commit();
				indexWriter.forceMerge(1);
				indexWriter.close();
				
				analyzer.close();
				simpleFSDirectory.close();
			}
			else {
				message = "Lucene Index Folder: " + indexFolder + " does not exist!";
//				System.err.println(message);
				// results.setText(message);
				progressMessageQueue.send(new MessageInput(message));
			}
		}
	}
	
	public void load(
			String txtPath,
			String txtColSeparator,
			String indexPath,
			String indexFields,
			String casEdfIdFieldName,
			String taxonomyPath,
			Integer progressInterval,
			TextArea results,
			Map<String, Toxicity> toxicities,
			String levelFields,
			String...parseFields) throws IOException {

		String message;
		
		File csvFile = new File(txtPath);
		if (csvFile.exists()) {

			File indexFolder = new File(indexPath);
			
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			}
			
			File taxonomyFolder = new File(taxonomyPath);
			if (!taxonomyFolder.exists()) {
				taxonomyFolder.mkdir();
			}
			
			if (indexFolder.exists() && taxonomyFolder.exists()) {
				
				deleteLuceneIndex(indexFolder);
				if (!indexFolder.exists()) {
					indexFolder.mkdir();
				}
				
				deleteLuceneIndex(taxonomyFolder);
				if (!taxonomyFolder.exists()) {
					taxonomyFolder.mkdir();
				}

				Map<String, String> mapBreakFields = new LinkedHashMap<>();
				Map<String, String> mapIndexFields = new LinkedHashMap<>();
				Map<String, String> mapLevelFields = new LinkedHashMap<>();
				Map<String, Integer> mapColIndexes = new LinkedHashMap<>();
				
				String[] pieces;
				
				pieces = indexFields.split(",");
				for (String indexField : pieces) {
					mapIndexFields.put(indexField, "");
				}

				pieces = levelFields.split(",");
				for (String levelField : pieces) {
					mapBreakFields.put(levelField, "");
					mapLevelFields.put(levelField, "");
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

				BufferedReader br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					rcdCount++;
					pieces = line.split(txtColSeparator);
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
							}
							tgtDocument.add(new TextField("text", sbIndex.toString().trim(), Store.NO));
							if (taxonomyCategories.size() > 0) {
								facetFields.addFields(tgtDocument, taxonomyCategories);
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
											taxonomyCategories.add(new CategoryPath("toxRecognized", toxValue, casEdfIdFieldName));
										}
									}
								}
								// build up suspected toxicity values
								String [] toxSValues = toxicity.getToxSuspected().split(",");
								for (String toxValue : toxSValues) {
									if (!toxValue.equals("")) {
										if (!mapToxValues.get("toxSuspected").containsKey(toxValue)) {
											mapToxValues.get("toxSuspected").put(toxValue, toxValue);
											taxonomyCategories.add(new CategoryPath("toxSuspected", toxValue, casEdfIdFieldName));
										}
									}
								}
							}
						}
						if (progressInterval > 0 && rcdCount % progressInterval == 0) {
							message = "Records indexed: " + rcdCount;
//							System.out.println(message);
							// results.appendText(System.lineSeparator() + message);
							progressMessageQueue.send(new MessageInput(message));
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
				}
				tgtDocument.add(new TextField("text", sbIndex.toString().trim(), Store.NO));
				if (taxonomyCategories.size() > 0) {
					facetFields.addFields(tgtDocument, taxonomyCategories);
				}
				indexWriter.addDocument(tgtDocument);
				outCount++;
				message = "Records processed: " + rcdCount;
//				System.out.println(message);
				// results.appendText(System.lineSeparator() + message);
				progressMessageQueue.send(new MessageInput(message));
				message = "Records indexed: " + outCount;
//				System.out.println(message);
				// results.appendText(System.lineSeparator() + message);
				progressMessageQueue.send(new MessageInput(message));
				
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
			}
			else {
				message = "Lucene Index Folder: " + indexFolder + " does not exist!";
//				System.err.println(message);
				// results.setText(message);
				progressMessageQueue.send(new MessageInput(message));
			}
		}
	}
	
	public void load(
			String indexPath,
			List<Document> srcDocuments,
			String indexFields,
			Integer progressInterval,
			TextArea results,
			String levelFields,
			String...parseFields) throws IOException {

		String message;
		
		File indexFolder = new File(indexPath);
		
		if (!indexFolder.exists()) {
			indexFolder.mkdir();
		}
		
		if (indexFolder.exists()) {
			
			deleteLuceneIndex(indexFolder);
			
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			}

			List<String> colHeaders = new ArrayList<>();

			Map<String,String> mapIndexFields = new LinkedHashMap<>();
			Map<String,String> mapLevelFields = new LinkedHashMap<>();
			
			String[] pieces;
			pieces = indexFields.split(",");
			for (String indexField : pieces) {
				mapIndexFields.put(indexField, indexField);
			}

			pieces = levelFields.split(",");
			for (String levelField : pieces) {
				mapLevelFields.put(levelField, levelField);
			}

			Map<String, Map<String, String>> mapParseValues = new LinkedHashMap<>();
			for (String parseField : parseFields) {
				mapParseValues.put(parseField, new TreeMap<String, String>());
			}
			
			SimpleFSDirectory simpleFSDirectory = new SimpleFSDirectory(indexFolder);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
			IndexWriter indexWriter = new IndexWriter(simpleFSDirectory, indexWriterConfig);
			
			StringBuilder sbIndex = new StringBuilder();
			StringBuilder sbLevel = new StringBuilder();
			
			String stBreak = "";
			
			Integer outCount = 0;
			Integer rcdCount = 0;
			for (Document srcDocument : srcDocuments) {
				rcdCount++;
				if (rcdCount == 1) {
					for (IndexableField colHeader : srcDocument.getFields()) {
						colHeaders.add(colHeader.name());
					}
				}

				Document tgtDocument = new Document();

				sbIndex.setLength(0);
				sbLevel.setLength(0);
				
				for (int i = 0; i < srcDocument.getFields().size(); i++) {
					String value = srcDocument.get(colHeaders.get(i)).trim();
					if (mapLevelFields.containsKey(colHeaders.get(i))) {
						if (mapIndexFields.containsKey(colHeaders.get(i))) {
							if (!value.equals("")) {
								sbIndex.append(value);
								sbIndex.append(" ");
							}
						}
						if (mapLevelFields.containsKey(colHeaders.get(i))) {
							Field field = new TextField(colHeaders.get(i), value , Store.YES);
							tgtDocument.add(field);
							if (!value.equals("")) {
								sbLevel.append(value);
								sbLevel.append(" ");
							}
						}
					}
					// build up the toxicity values
					// for the specified field names
					if (mapParseValues.containsKey(colHeaders.get(i))) {
						pieces = value.split(",");
						for (String piece : pieces) {
							if (!mapParseValues.get(colHeaders.get(i)).containsKey(piece.trim())) {
								mapParseValues.get(colHeaders.get(i)).put(piece.trim(), piece.trim());
//								System.out.println(colHeaders.get(i) + ": " + piece.trim());
							}
						}
					}
				}

				// output Reports document on level-break
				if (!sbLevel.toString().equals(stBreak)) {
					for (String key : mapParseValues.keySet()) {
						String toxicitiesValue = GenericUtilities.joinString(mapParseValues.get(key).values(), ",");
						// eliminate leading comma in the string value
						// due to how the joinString routine appends values
						toxicitiesValue = toxicitiesValue.length() > 1 ? toxicitiesValue.substring(1) : toxicitiesValue;
						// if toxicity value is to be indexed
						if (mapIndexFields.containsKey(key)) {
							// append it to the value that
							// will appear in the "text" field
							if (!toxicitiesValue.trim().equals("")) {
								sbIndex.append(toxicitiesValue.trim());
								sbIndex.append(" ");
							}
						}
						// add the toxicity value to the document
						Field field = new TextField(key, toxicitiesValue , Store.YES);
						tgtDocument.add(field);
						// clear the toxicity values collection
						mapParseValues.get(key).clear();
					}
					tgtDocument.add(new TextField("text", sbIndex.toString().trim(), Store.NO));
					indexWriter.addDocument(tgtDocument);
					outCount++;
					stBreak = sbLevel.toString();
//					System.out.println(stBreak);
					if (progressInterval > 0 && outCount % progressInterval == 0) {
						message = "Records indexed: " + outCount;
//						System.out.println(message);
						// results.appendText(System.lineSeparator() + message);
						progressMessageQueue.send(new MessageInput(message));
					}
				}
			}
			message = "Records indexed: " + outCount;
//			System.out.println(message);
			// results.appendText(System.lineSeparator() + message);
			progressMessageQueue.send(new MessageInput(message));
			
			sbIndex.setLength(0);
			sbIndex.trimToSize();
			
			sbLevel.setLength(0);
			sbLevel.trimToSize();
			
			mapParseValues.clear();

			indexWriter.commit();
			indexWriter.forceMerge(1);
			indexWriter.close();
			
			analyzer.close();
			simpleFSDirectory.close();
		}
		else {
			message = "Lucene Index Folder: " + indexFolder + " does not exist!";
//			System.err.println(message);
			// results.setText(message);
			progressMessageQueue.send(new MessageInput(message));
		}
	}
	
	
	public List<Document> query(
			String field,
			String queryText,
			Integer maxDocs,
			Boolean allowLeadingWildcard) throws IOException, ParseException {
		return query(indexPath, field, queryText, maxDocs, allowLeadingWildcard, "");
	}

	
	public List<Document> query(
			String indexPath,
			String field,
			String queryText,
			Integer maxDocs,
			Boolean allowLeadingWildcard,
			String taxonomyPath,
			String... sortColumns ) throws IOException, ParseException {
		List<Document> documents = new ArrayList<>();
		// if no faceting path is specified
		if (taxonomyPath.equals("")) {
			System.out.println("Searching Without Faceting");
			File indexFile = new File(indexPath);
			if (indexFile.exists()) {
				IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexFile));
				IndexSearcher indexSearcher = new IndexSearcher(indexReader);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
				QueryParser parser = new QueryParser(Version.LUCENE_44, field, analyzer);
				parser.setAllowLeadingWildcard(allowLeadingWildcard);
				Query query = parser.parse(queryText);
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
					}
					else {
						sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
					}
				}
				TopDocs topDocs = sortFields.length > 0 ? indexSearcher.search(query, maxDocs, new Sort(sortFields)) : indexSearcher.search(query, maxDocs);
				for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
					documents.add(indexSearcher.doc(scoreDoc.doc));
				}
				analyzer.close();
				indexReader.close();
			}
		}
		else {
			System.out.println("Searching with Faceting");
			File taxonomyFile = new File(taxonomyPath);
			if (taxonomyFile.exists()) {
				File indexFile = new File(indexPath);
				if (indexFile.exists()) {
					IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexFile));
					IndexSearcher indexSearcher = new IndexSearcher(indexReader);
					Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
					QueryParser parser = new QueryParser(Version.LUCENE_44, field, analyzer);
					parser.setAllowLeadingWildcard(allowLeadingWildcard);
					Query query = parser.parse(queryText);
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
						}
						else {
							sortFields[i] = new SortField(sortColumns[i], SortField.Type.STRING);
						}
					}
					Sort sort = new Sort(sortFields);
					TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(taxonomyFile));
					List<FacetRequest> categories = new ArrayList<>();
					categories.add(new CountFacetRequest(new CategoryPath("toxRecognized"), 20));
					categories.add(new CountFacetRequest(new CategoryPath("toxSuspected"), 20));
					FacetSearchParams facetSearchParams = new FacetSearchParams(categories);
					FacetsCollector facetsCollector = FacetsCollector.create(facetSearchParams, indexReader, taxonomyReader);
					TopFieldCollector topFieldCollector = TopFieldCollector.create(sort, maxDocs, true, false, false, false);
					indexSearcher.search(query, MultiCollector.wrap(topFieldCollector, facetsCollector));
					for (ScoreDoc scoreDoc : topFieldCollector.topDocs().scoreDocs) {
						documents.add(indexSearcher.doc(scoreDoc.doc));
					}
					for (FacetResult facetResult : facetsCollector.getFacetResults()) {
						System.err.println(facetResult.getFacetResultNode().label + ": " + facetResult.getFacetResultNode().value);
					}
					analyzer.close();
					indexReader.close();
					taxonomyReader.close();
				}
			}
		}
		return documents;
	}


    public void deleteLuceneIndex(File indexFolder) {
        if (indexFolder.exists()) {
            File[] files = indexFolder.listFiles();
            for (File file : files) {
                file.delete();
            }
            indexFolder.delete();
        }
    }
	
	public String getTxtPath() {
		return txtPath;
	}

	public void setTxtPath(String txtPath) {
		this.txtPath = txtPath;
	}

	public String getColSeparator() {
		return colSeparator;
	}

	public void setColSeparator(String colSeparator) {
		this.colSeparator = colSeparator;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	public Integer getProgressInterval() {
		return progressInterval;
	}

	public void setProgressInterval(Integer progressInterval) {
		this.progressInterval = progressInterval;
	}

	public MessageQueue getProgressMessageQueue() {
		return progressMessageQueue;
	}

	public void setProgressMessageQueue(MessageQueue progressMessageQueue) {
		this.progressMessageQueue = progressMessageQueue;
	}

	
	public String getIndexFields() {
		return indexFields;
	}


	public void setIndexFields(String indexFields) {
		this.indexFields = indexFields;
	}

}
