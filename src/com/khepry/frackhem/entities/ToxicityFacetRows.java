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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;

public class ToxicityFacetRows<E> implements ObservableList<ToxicityFacetRow> {
	
	private ObservableList<ToxicityFacetRow> list = FXCollections.observableArrayList();
	
	public ToxicityFacetRows() {
		
	}
	
	public List<TableColumn> getTableColumns() {
		List<TableColumn> list = new ArrayList<TableColumn>();
		String properties = "Category,category;Blood,blood;Cancer,cancer;Cardiovascular,cardiovascular;Developmental,developmental;Endocrine,endocrine;Gastrointestinal,gastrointestinal;Immunotoxicity,immunotoxicity;Liver,liver;Kidney,kidney;Musculoskeletal,musculoskeletal;Neurotoxicity,neurotoxicity;Reproductive,reproductive;Respiratory,respiratory;Sense,sense;Skin,skin";
		String[] keyValuePairs = properties.split(";");
		for (String keyValuePair : keyValuePairs) {
			String[] kvs = keyValuePair.split(",");
			TableColumn tableColumn = new TableColumn(kvs[0] + "\n(" + kvs[1] + ")");
			tableColumn.setCellValueFactory(new PropertyValueFactory<Report, String>(kvs[1]));
			list.add(tableColumn);
		}
		return list;
	}
	
	public void loadViaFacetResults(List<FacetResult> facetResults) {
		for (FacetResult facetResult : facetResults) {
			boolean addToList = false;
			ToxicityFacetRow toxicityFacetRow = new ToxicityFacetRow();
			for (FacetResultNode node0 : facetResult.getFacetResultNode().subResults) {
				if (node0.label.toString().indexOf("/Toxicity/") > -1) {
					String category = node0.label.toString().substring(3, node0.label.toString().indexOf("/"));
					toxicityFacetRow.setCategory(new SimpleStringProperty(category));
					String key = node0.label.toString().toLowerCase();
					if (key.indexOf(",") == -1) {
						addToList = true;
						switch (key.substring(key.lastIndexOf("/") + 1)) {
						case "blood":
							toxicityFacetRow.setBlood(new SimpleDoubleProperty(node0.value));
							break;
						case "cancer":
							toxicityFacetRow.setCancer(new SimpleDoubleProperty(node0.value));
							break;
						case "cardiovascular":
							toxicityFacetRow.setCardiovascular(new SimpleDoubleProperty(node0.value));
							break;
						case "developmental":
							toxicityFacetRow.setDevelopmental(new SimpleDoubleProperty(node0.value));
							break;
						case "endocrine":
							toxicityFacetRow.setEndocrine(new SimpleDoubleProperty(node0.value));
							break;
						case "gastrointestinal":
							toxicityFacetRow.setGastrointestinal(new SimpleDoubleProperty(node0.value));
							break;
						case "immunotoxicity":
							toxicityFacetRow.setImmunotoxicity(new SimpleDoubleProperty(node0.value));
							break;
						case "kidney":
							toxicityFacetRow.setKidney(new SimpleDoubleProperty(node0.value));
							break;
						case "liver":
							toxicityFacetRow.setLiver(new SimpleDoubleProperty(node0.value));
							break;
						case "musculoskeletal":
							toxicityFacetRow.setMusculoskeletal(new SimpleDoubleProperty(node0.value));
							break;
						case "neurotoxicity":
							toxicityFacetRow.setNeurotoxicity(new SimpleDoubleProperty(node0.value));
							break;
						case "reproductive":
							toxicityFacetRow.setReproductive(new SimpleDoubleProperty(node0.value));
							break;
						case "respiratory":
							toxicityFacetRow.setRespiratory(new SimpleDoubleProperty(node0.value));
							break;
						case "sense":
							toxicityFacetRow.setSense(new SimpleDoubleProperty(node0.value));
							break;
						case "skin":
							toxicityFacetRow.setSkin(new SimpleDoubleProperty(node0.value));
							break;
						default:
							System.out.println("Unknown toxicity value: " + key + ": " + node0.value);
							break;
						}
					}
				}
			}
			if (addToList) {
				list.add(toxicityFacetRow);
			}
		}
	}

	@Override
	public boolean add(ToxicityFacetRow toxicityFacetRow) {
		return list.add(toxicityFacetRow);
	}

	@Override
	public void add(int index, ToxicityFacetRow toxicityFacetRow) {
		list.add(index, toxicityFacetRow);
	}

	@Override
	public boolean addAll(Collection<? extends ToxicityFacetRow> toxicityFacetRows) {
		return list.addAll(toxicityFacetRows);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ToxicityFacetRow> toxicityFacetRows) {
		return list.addAll(index, toxicityFacetRows);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean contains(Object toxicityFacetRow) {
		return list.contains(toxicityFacetRow);
	}

	@Override
	public boolean containsAll(Collection<?> toxicityFacetRows) {
		return list.containsAll(toxicityFacetRows);
	}

	@Override
	public ToxicityFacetRow get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object toxicityFacetRow) {
		return list.indexOf(toxicityFacetRow);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<ToxicityFacetRow> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object toxicityFacetRow) {
		return list.lastIndexOf(toxicityFacetRow);
	}

	@Override
	public ListIterator<ToxicityFacetRow> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<ToxicityFacetRow> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public boolean remove(Object toxicityFacetRow) {
		return list.remove(toxicityFacetRow);
	}

	@Override
	public ToxicityFacetRow remove(int index) {
		return list.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> toxicityFacetRows) {
		return list.removeAll(toxicityFacetRows);
	}

	@Override
	public boolean retainAll(Collection<?> toxicityFacetRows) {
		return list.retainAll(toxicityFacetRows);
	}

	@Override
	public ToxicityFacetRow set(int index, ToxicityFacetRow toxicityFacetRow) {
		return list.set(index, toxicityFacetRow);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<ToxicityFacetRow> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] toxicityFacetRows) {
		return list.toArray(toxicityFacetRows);
	}

	@Override
	public void addListener(InvalidationListener invalidationListener) {
		list.addListener(invalidationListener);
	}

	@Override
	public void removeListener(InvalidationListener invalidationListener) {
		list.removeListener(invalidationListener);
	}

	@Override
	public boolean addAll(ToxicityFacetRow... toxicityFacetRows) {
		return list.addAll(toxicityFacetRows);
	}

	@Override
	public void addListener(ListChangeListener<? super ToxicityFacetRow> listChangeListener) {
		list.addListener(listChangeListener);
	}

	@Override
	public void remove(int fromIndex, int toIndex) {
		list.remove(fromIndex, toIndex);
	}

	@Override
	public boolean removeAll(ToxicityFacetRow... toxicityFacetRows) {
		return list.removeAll(toxicityFacetRows);
	}

	@Override
	public void removeListener(ListChangeListener<? super ToxicityFacetRow> listChangeListener) {
		list.removeListener(listChangeListener);
	}

	@Override
	public boolean retainAll(ToxicityFacetRow... toxicityFacetRows) {
		return list.retainAll(toxicityFacetRows);
	}

	@Override
	public boolean setAll(ToxicityFacetRow... toxicityFacetRows) {
		return list.setAll(toxicityFacetRows);
	}

	@Override
	public boolean setAll(Collection<? extends ToxicityFacetRow> toxicityFacetRows) {
		return list.setAll(toxicityFacetRows);
	}

}
