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
import java.util.Map;

import org.apache.lucene.document.Document;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class Chemicals<E> implements ObservableList<E> {
	
	private ObservableList<E> list = FXCollections.observableArrayList();
	
	public Chemicals() {
		
	}
	
	public Chemicals(
			List<Document> documents) {
		loadViaDocuments(documents);
	}
	
	public void loadViaDocuments(
			List<Document> documents) {
		list.clear();
		for (Document document : documents) {
			Chemical chemical = new Chemical();
			chemical.setChmSeqId(new SimpleStringProperty(document.get("chmSeqId")));
			chemical.setRptAPI(new SimpleStringProperty(document.get("rptAPI")));
			chemical.setChmRow(new SimpleStringProperty(document.get("chmRow")));
			chemical.setChmTradeName(new SimpleStringProperty(document.get("chmTradeName")));
			chemical.setRptFractureDate(new SimpleStringProperty(document.get("rptFractureDate")));
			chemical.setChmSupplier(new SimpleStringProperty(document.get("chmSupplier")));
			chemical.setChmPurpose(new SimpleStringProperty(document.get("chmPurpose")));
			chemical.setChmIngredients(new SimpleStringProperty(document.get("chmIngredients")));
			chemical.setChmCasEdfId(new SimpleStringProperty(document.get("chmCasEdfId")));
			chemical.setChmAdditiveConcentration(new SimpleStringProperty(document.get("chmAdditiveConcentration")));
			chemical.setRptPdfSeqId(new SimpleStringProperty(document.get("rptPdfSeqId")));
			chemical.setChmHfFluidConcentration(new SimpleStringProperty(document.get("chmHfFluidConcentration")));
			chemical.setChmComments(new SimpleStringProperty(document.get("chmComments")));
			chemical.setChmCasType(new SimpleStringProperty(document.get("chmCasType")));
			chemical.setToxChemicalName(new SimpleStringProperty(document.get("toxChemicalName")));
			chemical.setToxRecognized(new SimpleStringProperty(document.get("toxRecognized")));
			chemical.setToxSuspected(new SimpleStringProperty(document.get("toxSuspected")));
			list.add((E)chemical);
		}
	}
	
	public List<TableColumn> getTableColumns() {
		List<TableColumn> list = new ArrayList<TableColumn>();
		String properties = "SeqId,chmSeqId;Pdf\nSeqId,rptPdfSeqId;API,rptAPI;Fracture\nDate,rptFractureDate;Row,chmRow;CAS\nNumber,chmCasEdfId;Trade\nName,chmTradeName;Supplier,chmSupplier;Purpose,chmPurpose;Ingredients,chmIngredients;Additive\nConcentration,chmAdditiveConcentration;HF Fluid\nConcentration,chmHfFluidConcentration;Comments,chmComments;CAS\nType,casType;Chemical\nName,toxChemicalName;Toxicity\nRecognized,toxRecognized;Toxicity\nSuspected,toxSuspected";
		String[] keyValuePairs = properties.split(";");
		for (String keyValuePair : keyValuePairs) {
			String[] kvs = keyValuePair.split(",");
			TableColumn tableColumn = new TableColumn(kvs[0]);
			tableColumn.setCellValueFactory(new PropertyValueFactory<Report, String>(kvs[1]));
			list.add(tableColumn);
		}
		return list;
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

}
