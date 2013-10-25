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

import com.npstrandberg.simplemq.MessageInput;
import com.npstrandberg.simplemq.MessageQueue;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class Chemicals<E> implements ObservableList<E> {
	
	private ObservableList<E> list = FXCollections.observableArrayList();
	
	private String indexFolderPath = "indexes/chemicals";
	private String indexFields = "rptPdfSeqId,rptAPI,rptState,rptCounty,rptOperator,rptWellName,chmCasEdfId,chmTradeName,chmSupplier,chmPurpose,chmIngredients,chmComments,rptFractureDate,toxChemicalName,toxRecognized,toxSuspected";
	private String statsFields = "rptCounty:County,rptOperator:Operator,rptProdType:ProdType,rptState:State";
	private String taxonomyFolderPath = "taxonomies/chemicals";
	private Integer progressInterval = 10000;
	
	private Boolean outputToSystemOut = Boolean.TRUE;
	private Boolean outputToSystemErr = Boolean.TRUE;
	private Boolean outputDebugInfo = Boolean.FALSE;
	private Boolean outputToMsgQueue = Boolean.FALSE;
	
	private MessageQueue progressMessageQueue;

	List<Document> documents = new ArrayList<>();
	
	public Chemicals() {
		
	}
	
	public Chemicals(
			List<Document> documents) {
		loadViaDocuments(documents);
	}
	
	public void loadViaDocuments(List<Document> documents) {
		list.clear();
		for (Document document : documents) {
			Chemical chemical = new Chemical(document);
			list.add((E)chemical);
		}
	}

	
	public List<TableColumn<?,?>> getTableColumns() {
		List<TableColumn<?,?>> list = new ArrayList<>();
		String properties = "SeqId,chmSeqId;Pdf\nSeqId,rptPdfSeqId;API,rptAPI;Fracture\nDate,rptFractureDate;Row,chmRow;CAS\nNumber,chmCasEdfId;Trade\nName,chmTradeName;Supplier,chmSupplier;Purpose,chmPurpose;Ingredients,chmIngredients;Additive\nConcentration,chmAdditiveConcentration;HF Fluid\nConcentration,chmHfFluidConcentration;Comments,chmComments;CAS\nType,casType;Chemical\nName,toxChemicalName;Toxicity\nRecognized,toxRecognized;Toxicity\nSuspected,toxSuspected";
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
			String textFilePath,
			String textColSeparator,
			String casEdfIdFieldName,
			Map<String, Toxicity> toxicities) throws IOException {

		String message;

		message = "Start Indexing Chemicals via Lucene...";
		if (outputToSystemOut) {
			System.out.println(message);
		}
		if (outputToMsgQueue) {
			progressMessageQueue.send(new MessageInput(message));
		}

		File textFile = new File(textFilePath);
		if (textFile.exists()) {

			File indexFolder = new File(indexFolderPath);
			if (!indexFolder.exists()) {
				indexFolder.mkdir();
			} else {
				deleteFolder(indexFolder);
				if (!indexFolder.exists()) {
					indexFolder.mkdir();
				}
			}

			File taxonomyFolder = new File(taxonomyFolderPath);
			if (!taxonomyFolder.exists()) {
				taxonomyFolder.mkdir();
			} else {
				deleteFolder(taxonomyFolder);
				if (!taxonomyFolder.exists()) {
					taxonomyFolder.mkdir();
				}
			}

			if (indexFolder.exists() && taxonomyFolder.exists()) {

				List<String> colHeaders = new ArrayList<>();
				Map<String, Integer> colIndexes = new LinkedHashMap<>();
				Map<String, String> mapIndexFields = new LinkedHashMap<>();
				Map<String, String> mapStatsFields = new LinkedHashMap<>();

				String[] pieces;
				String[] tuples;
				
				pieces = indexFields.split(",");
				for (String indexField : pieces) {
					mapIndexFields.put(indexField, indexField);
				}
				
				pieces = statsFields.split(",");
				for (String statField : pieces) {
					tuples = statField.split(":"); 
					mapStatsFields.put(tuples[0], tuples.length > 1 ? tuples[1] : tuples[0]);
				}

				SimpleFSDirectory indexDirectory = new SimpleFSDirectory(indexFolder);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_44, analyzer);
				IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);

				SimpleFSDirectory taxonomyDirectory = new SimpleFSDirectory(taxonomyFolder);
				TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDirectory, OpenMode.CREATE);
				FacetFields facetFields = new FacetFields(taxonomyWriter);

				List<CategoryPath> taxonomyCategories = new ArrayList<>();

				String line;
				Integer rcdCount = 0;
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(textFile));
				while ((line = br.readLine()) != null) {
					rcdCount++;
					pieces = line.split(textColSeparator);
					if (rcdCount == 1) {
						int i = 0;
						for (String colHeader : pieces) {
							colHeaders.add(colHeader.trim());
							colIndexes.put(colHeader, i);
						}
					} else {
						if (pieces.length == colHeaders.size()) {
							sb.setLength(0);
							Document document = new Document();
							for (int i = 0; i < pieces.length; i++) {
								Field field = new TextField(colHeaders.get(i), pieces[i].trim(), Store.YES);
								document.add(field);
								if (mapIndexFields.containsKey(colHeaders.get(i))) {
									if (!pieces[i].trim().equals("")) {
										sb.append(pieces[i].trim());
										sb.append(" ");
									}
								}
							}
							// append toxicity information to the document
							String toxCasEdfId = document.get(casEdfIdFieldName).trim();
							Toxicity toxicity = new Toxicity();
							if (toxicities.containsKey(toxCasEdfId)) {
								toxicity = toxicities.get(toxCasEdfId);
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

							String toxChemical = toxicity.getToxChemicalName().trim();

							// categorize recognized toxicities
							String toxRecognized = toxicity.getToxRecognized().trim();
							if (!toxRecognized.equals("")) {
								taxonomyCategories.add(new CategoryPath("toxRecognized", "CasEdfId", toxCasEdfId));
								taxonomyCategories.add(new CategoryPath("toxRecognized", "Chemical", toxChemical.replace("/", "|")));
								for (String value : toxRecognized.replace(" ", ",").split(",")) {
									if (!value.trim().equals("")) {
										taxonomyCategories.add(new CategoryPath("toxRecognized", "Toxicity", value));
									}
								}
							}

							// categorize suspected toxicities
							String toxSuspected = toxicity.getToxSuspected().trim();
							if (!toxSuspected.equals("")) {
								taxonomyCategories.add(new CategoryPath("toxSuspected",	"CasEdfId", toxCasEdfId));
								taxonomyCategories.add(new CategoryPath("toxSuspected", "Chemical", toxChemical.replace("/", "|")));
								for (String value : toxSuspected.replace(" ", ",").split(",")) {
									if (!value.trim().equals("")) {
										taxonomyCategories.add(new CategoryPath("toxSuspected",	"Toxicity", value));
									}
								}
							}
							
							// build up "stats" taxonomy categories
							for (String statsKey : mapStatsFields.keySet()) {
								if (mapIndexFields.containsKey(statsKey)) {
									String statsText = mapStatsFields.get(statsKey);
									String fieldValue = mapIndexFields.get(statsKey);
									if (!statsText.trim().equals("") && !fieldValue.trim().equals("")) {
										taxonomyCategories.add(new CategoryPath("Chemicals", statsText, fieldValue));
									}
								}
							}

							if (taxonomyCategories.size() > 0) {
								facetFields.addFields(document,	taxonomyCategories);
								// System.out.println("Taxonomies added: " +
								// taxonomyCategories.size());
							}

							indexWriter.addDocument(document);
							if (progressInterval > 0 && rcdCount % progressInterval == 0) {
								message = "Records indexed: " + rcdCount;
								if (outputToSystemOut) {
									System.out.println(message);
								}
								if (outputToMsgQueue) {
									progressMessageQueue.send(new MessageInput(message));
								}
							}

							taxonomyCategories.clear();
						}
					}
				}
				br.close();
				message = "Records indexed: " + rcdCount;
				if (outputToSystemOut) {
					System.out.println(message);
				}
				if (outputToMsgQueue) {
					progressMessageQueue.send(new MessageInput(message));
				}

				sb.setLength(0);
				sb.trimToSize();

				indexWriter.commit();
				indexWriter.forceMerge(1);
				indexWriter.close();

				taxonomyWriter.commit();
				taxonomyWriter.close();

				analyzer.close();

				indexDirectory.close();
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
			message = "Ended Indexing Chemicals via Lucene!";
			if (outputToSystemOut) {
				System.out.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
		}
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

		File indexFolder = new File(indexFolderPath);
		if (!indexFolder.exists()) {
			message = "Index path does not exist: " + indexFolderPath;
			if (outputToSystemErr) {
				System.err.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
			return queryResult;
		}

		File taxonomyFolder = new File(taxonomyFolderPath);
		if (!taxonomyFolder.exists()) {
			message = "Taxonomy path does not exist: " + indexFolderPath;
			if (outputToSystemErr) {
				System.err.println(message);
			}
			if (outputToMsgQueue) {
				progressMessageQueue.send(new MessageInput(message));
			}
			return queryResult;
		}

		if (indexFolder.exists() && taxonomyFolder.exists()) {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexFolder));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
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

			TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(taxonomyFolder));
			List<FacetRequest> facetRequests = new ArrayList<>();
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxRecognized", "CasEdfId"), 200));
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxRecognized", "Toxicity"), 200));
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxSuspected", "CasEdfId"), 200));
			facetRequests.add(new CountFacetRequest(new CategoryPath("toxSuspected", "Toxicity"), 200));
			FacetSearchParams facetSearchParams = new FacetSearchParams(facetRequests);
			queryResult.setFacetsCollector(FacetsCollector.create(facetSearchParams, indexReader, taxonomyReader));
			
			indexSearcher.search(query, MultiCollector.wrap(queryResult.getTopFieldCollector(), queryResult.getFacetsCollector()));

			List<Document> documents = new ArrayList<>();
			for (ScoreDoc scoreDoc : queryResult.getTopFieldCollector().topDocs().scoreDocs) {
				Document document = indexSearcher.doc(scoreDoc.doc);
				documents.add(document);
				Chemical chemical = new Chemical(document);
				list.add((E)chemical);
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
						if (node0.label.toString().indexOf("/Toxicity/") > -1) {
							if (node0.label.toString().indexOf(",") == -1) {
								System.out.println(node0.label + ": " + node0.value);
							}
						}
						else {
							System.out.println(node0.label + ": " + node0.value);
						}
	//					for (FacetResultNode node1 : node0.subResults) {
	//						System.out.println(node1.label + ": " + node1.value);
	//					}
					}
				}
			}
			
			analyzer.close();
			indexReader.close();
			taxonomyReader.close();
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

	public String getTaxonomyFolderPath() {
		return taxonomyFolderPath;
	}

	public void setTaxonomyFolderPath(String taxonomyFolderPath) {
		this.taxonomyFolderPath = taxonomyFolderPath;
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
