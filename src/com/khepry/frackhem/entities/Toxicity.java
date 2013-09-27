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

import org.apache.lucene.document.Document;

import javafx.beans.property.SimpleStringProperty;

public class Toxicity {
	
	private SimpleStringProperty toxCasEdfId = new SimpleStringProperty("");
	private SimpleStringProperty toxChemicalName = new SimpleStringProperty("");
	private SimpleStringProperty toxRecognized = new SimpleStringProperty("");
	private SimpleStringProperty toxSuspected = new SimpleStringProperty("");
	
	private String defaultSeparator = " ";
	private String defaultSplitter = ",";

	public Toxicity() {
	}
	
	public Toxicity(
			String toxCasEdfId,
			String toxChemicalName,
			String toxRecognized,
			String toxSuspected) {
		this.toxCasEdfId.setValue(toxCasEdfId);
		this.toxChemicalName.setValue(toxChemicalName);
		this.toxRecognized.setValue(toxRecognized);
		this.toxSuspected.setValue(toxSuspected);
	}
	
	public Toxicity(
			String toxCasEdfId,
			String toxChemicalName,
			String toxRecognized,
			String toxSuspected,
			String defaultSplitter) {
		this.toxCasEdfId.setValue(toxCasEdfId);
		this.toxChemicalName.setValue(toxChemicalName);
		this.toxRecognized.setValue(toxRecognized);
		this.toxSuspected.setValue(toxSuspected);
		this.defaultSplitter = defaultSplitter;
	}
	
	public Toxicity(
			String toxCasEdfId,
			String toxChemicalName,
			String toxRecognized,
			String toxSuspected,
			String defaultSplitter,
			String defaultSeparator) {
		this.toxCasEdfId.setValue(toxCasEdfId);
		this.toxChemicalName.setValue(toxChemicalName);
		this.toxRecognized.setValue(toxRecognized);
		this.toxSuspected.setValue(toxSuspected);
		this.defaultSplitter = defaultSplitter;
		this.defaultSeparator = defaultSeparator;
	}
	
	public Toxicity(
			Document document) {
		this.toxCasEdfId.setValue(document.get("toxCasEdfId").trim());
		this.toxChemicalName.setValue(document.get("toxChemicalName").trim());
		this.toxRecognized.setValue(document.get("toxRecognized").trim());
		this.toxSuspected.setValue(document.get("toxSuspected").trim());
	}
	
	public Toxicity(
			Document document,
			String defaultSplitter) {
		this.toxCasEdfId.setValue(document.get("toxCasEdfId").trim());
		this.toxChemicalName.setValue(document.get("toxChemicalName").trim());
		this.toxRecognized.setValue(document.get("toxRecognized").trim());
		this.toxSuspected.setValue(document.get("toxSuspected").trim());
		this.defaultSplitter = defaultSplitter;
	}
	
	public Toxicity(
			Document document,
			String defaultSplitter,
			String defaultSeparator) {
		this.toxCasEdfId.setValue(document.get("toxCasEdfId").trim());
		this.toxChemicalName.setValue(document.get("toxChemicalName").trim());
		this.toxRecognized.setValue(document.get("toxRecognized").trim());
		this.toxSuspected.setValue(document.get("toxSuspected").trim());
		this.defaultSplitter = defaultSplitter;
		this.defaultSeparator = defaultSeparator;
	}
	
	@Override
	public String toString() {
		return toxCasEdfId.get() + " :: " + toxChemicalName.get() + " :: " + toxRecognized.get() + " :: " + toxSuspected.get();
	}

	public String getToxCasEdfId() {
		return toxCasEdfId.get();
	}

	public void setToxCasEdfId(SimpleStringProperty toxCasEdfId) {
		this.toxCasEdfId = toxCasEdfId;
	}

	public String getToxChemicalName() {
		return toxChemicalName.get();
	}

	public void setToxChemicalName(SimpleStringProperty toxChemicalName) {
		this.toxChemicalName = toxChemicalName;
	}

	public String getToxRecognized() {
		return toxRecognized.get();
	}

	public void setToxRecognized(SimpleStringProperty toxRecognized) {
		this.toxRecognized = toxRecognized;
	}

	public String getToxRecognizedSeparated() {
		return toxRecognized.get().replace(defaultSplitter, defaultSeparator);
	}

	public String getToxRecognizedSeparated(String splitter) {
		return toxRecognized.get().replace(splitter, defaultSeparator);
	}

	public String getToxRecognizedSeparated(String splitter, String separator) {
		return toxRecognized.get().replace(splitter, separator);
	}
	
	public String[] getRecognizedToxicities() {
		return toxRecognized.get().split(defaultSplitter);
	}
	
	public String[] getRecognizedToxicities(String splitter) {
		return toxRecognized.get().split(splitter);
	}

	public String getToxSuspected() {
		return toxSuspected.get();
	}

	public void setToxSuspected(SimpleStringProperty toxSuspected) {
		this.toxSuspected = toxSuspected;
	}

	public String getToxSuspectedSeparated() {
		return toxSuspected.get().replace(defaultSplitter, defaultSeparator);
	}

	public String getToxSuspectedSeparated(String splitter) {
		return toxSuspected.get().replace(splitter, defaultSeparator);
	}

	public String getToxSuspectedSeparated(String splitter, String separator) {
		return toxSuspected.get().replace(splitter, separator);
	}
	
	public String[] getSuspectedToxicities() {
		return toxSuspected.get().split(defaultSplitter);
	}
	
	public String[] getSuspectedToxicities(String splitter) {
		return toxSuspected.get().split(splitter);
	}

	public String getDefaultSeparator() {
		return defaultSeparator;
	}

	public void setDefaultSeparator(String defaultSeparator) {
		this.defaultSeparator = defaultSeparator;
	}
	
	public String getDefaultSplitter() {
		return defaultSplitter;
	}

	public void setDefaultSplitter(String defaultSplitter) {
		this.defaultSplitter = defaultSplitter;
	}

}
