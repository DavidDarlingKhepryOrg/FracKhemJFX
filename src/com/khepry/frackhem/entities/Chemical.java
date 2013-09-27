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

public class Chemical {

	private SimpleStringProperty chmSeqId;
	private SimpleStringProperty rptPdfSeqId;
	private SimpleStringProperty rptAPI;
	private SimpleStringProperty rptFractureDate;
	private SimpleStringProperty chmRow;
	private SimpleStringProperty chmTradeName;
	private SimpleStringProperty chmSupplier;
	private SimpleStringProperty chmPurpose;
	private SimpleStringProperty chmIngredients;
	private SimpleStringProperty chmCasEdfId;
	private SimpleStringProperty chmAdditiveConcentration;
	private SimpleStringProperty chmHfFluidConcentration;
	private SimpleStringProperty chmComments;
	private SimpleStringProperty chmCasType;
	private SimpleStringProperty toxChemicalName;
	private SimpleStringProperty toxRecognized;
	private SimpleStringProperty toxSuspected;
	
	private String defaultSeparator = " ";
	private String defaultSplitter = ",";
	
	public Chemical() {
		
	}
	
	public Chemical(
			Document document) {
		this.setChmSeqId(new SimpleStringProperty(document.get("chmSeqId")));
		this.setRptAPI(new SimpleStringProperty(document.get("rptAPI")));
		this.setChmRow(new SimpleStringProperty(document.get("chmRow")));
		this.setChmTradeName(new SimpleStringProperty(document.get("chmTradeName")));
		this.setRptFractureDate(new SimpleStringProperty(document.get("rptFractureDate")));
		this.setChmSupplier(new SimpleStringProperty(document.get("chmSupplier")));
		this.setChmPurpose(new SimpleStringProperty(document.get("chmPurpose")));
		this.setChmIngredients(new SimpleStringProperty(document.get("chmIngredients")));
		this.setChmCasEdfId(new SimpleStringProperty(document.get("chmCasEdfId")));
		this.setChmAdditiveConcentration(new SimpleStringProperty(document.get("chmAdditiveConcentration")));
		this.setRptPdfSeqId(new SimpleStringProperty(document.get("rptPdfSeqId")));
		this.setChmHfFluidConcentration(new SimpleStringProperty(document.get("chmHfFluidConcentration")));
		this.setChmComments(new SimpleStringProperty(document.get("chmComments")));
		this.setChmCasType(new SimpleStringProperty(document.get("chmCasType")));
		this.setToxChemicalName(new SimpleStringProperty(document.get("toxChemicalName")));
		this.setToxRecognized(new SimpleStringProperty(document.get("toxRecognized")));
		this.setToxSuspected(new SimpleStringProperty(document.get("toxSuspected")));
	}
	
	public Chemical(
			Document document,
			String defaultSplitter) {
		this.setChmSeqId(new SimpleStringProperty(document.get("chmSeqId")));
		this.setRptAPI(new SimpleStringProperty(document.get("rptAPI")));
		this.setChmRow(new SimpleStringProperty(document.get("chmRow")));
		this.setChmTradeName(new SimpleStringProperty(document.get("chmTradeName")));
		this.setRptFractureDate(new SimpleStringProperty(document.get("rptFractureDate")));
		this.setChmSupplier(new SimpleStringProperty(document.get("chmSupplier")));
		this.setChmPurpose(new SimpleStringProperty(document.get("chmPurpose")));
		this.setChmIngredients(new SimpleStringProperty(document.get("chmIngredients")));
		this.setChmCasEdfId(new SimpleStringProperty(document.get("chmCasEdfId")));
		this.setChmAdditiveConcentration(new SimpleStringProperty(document.get("chmAdditiveConcentration")));
		this.setRptPdfSeqId(new SimpleStringProperty(document.get("rptPdfSeqId")));
		this.setChmHfFluidConcentration(new SimpleStringProperty(document.get("chmHfFluidConcentration")));
		this.setChmComments(new SimpleStringProperty(document.get("chmComments")));
		this.setChmCasType(new SimpleStringProperty(document.get("chmCasType")));
		this.setToxChemicalName(new SimpleStringProperty(document.get("toxChemicalName")));
		this.setToxRecognized(new SimpleStringProperty(document.get("toxRecognized")));
		this.setToxSuspected(new SimpleStringProperty(document.get("toxSuspected")));
		this.defaultSplitter = defaultSplitter;
	}
	
	public Chemical(
			Document document,
			String defaultSplitter,
			String defaultSeparator) {
		this.setChmSeqId(new SimpleStringProperty(document.get("chmSeqId")));
		this.setRptAPI(new SimpleStringProperty(document.get("rptAPI")));
		this.setChmRow(new SimpleStringProperty(document.get("chmRow")));
		this.setChmTradeName(new SimpleStringProperty(document.get("chmTradeName")));
		this.setRptFractureDate(new SimpleStringProperty(document.get("rptFractureDate")));
		this.setChmSupplier(new SimpleStringProperty(document.get("chmSupplier")));
		this.setChmPurpose(new SimpleStringProperty(document.get("chmPurpose")));
		this.setChmIngredients(new SimpleStringProperty(document.get("chmIngredients")));
		this.setChmCasEdfId(new SimpleStringProperty(document.get("chmCasEdfId")));
		this.setChmAdditiveConcentration(new SimpleStringProperty(document.get("chmAdditiveConcentration")));
		this.setRptPdfSeqId(new SimpleStringProperty(document.get("rptPdfSeqId")));
		this.setChmHfFluidConcentration(new SimpleStringProperty(document.get("chmHfFluidConcentration")));
		this.setChmComments(new SimpleStringProperty(document.get("chmComments")));
		this.setChmCasType(new SimpleStringProperty(document.get("chmCasType")));
		this.setToxChemicalName(new SimpleStringProperty(document.get("toxChemicalName")));
		this.setToxRecognized(new SimpleStringProperty(document.get("toxRecognized")));
		this.setToxSuspected(new SimpleStringProperty(document.get("toxSuspected")));
		this.defaultSplitter = defaultSplitter;
		this.defaultSeparator = defaultSeparator;
	}

	public String getChmSeqId() {
		return chmSeqId.get();
	}

	public void setChmSeqId(SimpleStringProperty chmSeqId) {
		this.chmSeqId = chmSeqId;
	}

	public String getRptPdfSeqId() {
		return rptPdfSeqId.get();
	}

	public void setRptPdfSeqId(SimpleStringProperty rptPdfSeqId) {
		this.rptPdfSeqId = rptPdfSeqId;
	}

	public String getRptAPI() {
		return rptAPI.get();
	}

	public void setRptAPI(SimpleStringProperty rptAPI) {
		this.rptAPI = rptAPI;
	}

	public String getRptFractureDate() {
		return rptFractureDate.get().substring(0,10);
	}

	public void setRptFractureDate(SimpleStringProperty rptFractureDate) {
		this.rptFractureDate = rptFractureDate;
	}

	public String getChmRow() {
		return chmRow.get();
	}

	public void setChmRow(SimpleStringProperty chmRow) {
		this.chmRow = chmRow;
	}

	public String getChmTradeName() {
		return chmTradeName.get();
	}

	public void setChmTradeName(SimpleStringProperty chmTradeName) {
		this.chmTradeName = chmTradeName;
	}

	public String getChmSupplier() {
		return chmSupplier.get();
	}

	public void setChmSupplier(SimpleStringProperty chmSupplier) {
		this.chmSupplier = chmSupplier;
	}

	public String getChmPurpose() {
		return chmPurpose.get();
	}

	public void setChmPurpose(SimpleStringProperty chmPurpose) {
		this.chmPurpose = chmPurpose;
	}

	public String getChmIngredients() {
		return chmIngredients.get();
	}

	public void setChmIngredients(SimpleStringProperty chmIngredients) {
		this.chmIngredients = chmIngredients;
	}

	public String getChmCasEdfId() {
		return chmCasEdfId.get();
	}

	public void setChmCasEdfId(SimpleStringProperty chmCasEdfId) {
		this.chmCasEdfId = chmCasEdfId;
	}

	public String getChmAdditiveConcentration() {
		return chmAdditiveConcentration.get();
	}

	public void setChmAdditiveConcentration(SimpleStringProperty chmAdditiveConcentration) {
		this.chmAdditiveConcentration = chmAdditiveConcentration;
	}

	public String getChmHfFluidConcentration() {
		return chmHfFluidConcentration.get();
	}

	public void setChmHfFluidConcentration(SimpleStringProperty chmHfFluidConcentration) {
		this.chmHfFluidConcentration = chmHfFluidConcentration;
	}

	public String getChmComments() {
		return chmComments.get();
	}

	public void setChmComments(SimpleStringProperty chmComments) {
		this.chmComments = chmComments;
	}

	public String getChmCasType() {
		return chmCasType.get();
	}

	public void setChmCasType(SimpleStringProperty chmCasType) {
		this.chmCasType = chmCasType;
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
