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

import javafx.beans.property.SimpleStringProperty;

public class Blended {

	private SimpleStringProperty rptPdfSeqId;
	private SimpleStringProperty rptAPI;
	private SimpleStringProperty rptFractureDate;
	private SimpleStringProperty rptState;
	private SimpleStringProperty rptCounty;
	private SimpleStringProperty rptOperator;
	private SimpleStringProperty rptWellName;
	private SimpleStringProperty rptProdType;
	private SimpleStringProperty rptLatitude;
	private SimpleStringProperty rptLongitude;
	private SimpleStringProperty rptDatum;
	private SimpleStringProperty rptTVD;
	private SimpleStringProperty rptTWV;
	private SimpleStringProperty rptPublishedDate;
	private SimpleStringProperty rptLatLng;

	private SimpleStringProperty chmSeqId;
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
	
	public Blended() {
		
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

	public String getRptState() {
		return rptState.get();
	}

	public void setRptState(SimpleStringProperty rptState) {
		this.rptState = rptState;
	}

	public String getRptCounty() {
		return rptCounty.get();
	}

	public void setRptCounty(SimpleStringProperty rptCounty) {
		this.rptCounty = rptCounty;
	}

	public String getRptOperator() {
		return rptOperator.get();
	}

	public void setRptOperator(SimpleStringProperty rptOperator) {
		this.rptOperator = rptOperator;
	}

	public String getRptWellName() {
		return rptWellName.get();
	}

	public void setRptWellName(SimpleStringProperty rptWellName) {
		this.rptWellName = rptWellName;
	}

	public String getRptProdType() {
		return rptProdType.get();
	}

	public void setRptProdType(SimpleStringProperty rptProdType) {
		this.rptProdType = rptProdType;
	}

	public String getRptLatitude() {
		return rptLatitude.get();
	}

	public void setRptLatitude(SimpleStringProperty rptLatitude) {
		this.rptLatitude = rptLatitude;
	}

	public String getRptLongitude() {
		return rptLongitude.get();
	}

	public void setRptLongitude(SimpleStringProperty rptLongitude) {
		this.rptLongitude = rptLongitude;
	}

	public String getRptDatum() {
		return rptDatum.get();
	}

	public void setRptDatum(SimpleStringProperty rptDatum) {
		this.rptDatum = rptDatum;
	}

	public String getRptTVD() {
		return rptTVD.get();
	}

	public void setRptTVD(SimpleStringProperty rptTVD) {
		this.rptTVD = rptTVD;
	}

	public String getRptTWV() {
		return rptTWV.get();
	}

	public void setRptTWV(SimpleStringProperty rptTWV) {
		this.rptTWV = rptTWV;
	}

	public String getRptPublishedDate() {
		return rptPublishedDate.get().substring(0,19);
	}

	public void setRptPublishedDate(SimpleStringProperty rptPublishedDate) {
		this.rptPublishedDate = rptPublishedDate;
	}

	public String getRptLatLng() {
		return rptLatLng.get();
	}

	public void setRptLatLng(SimpleStringProperty rptLatLng) {
		this.rptLatLng = rptLatLng;
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

	public String getToxSuspected() {
		return toxSuspected.get();
	}

	public void setToxSuspected(SimpleStringProperty toxSuspected) {
		this.toxSuspected = toxSuspected;
	}
}
