package com.khepry.frackhem.entities;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class ToxicityFacetRow {
	
	private SimpleStringProperty category = new SimpleStringProperty("");
	private SimpleDoubleProperty blood = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty cancer = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty cardiovascular = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty developmental = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty endocrine = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty gastrointestinal = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty immunotoxicity = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty kidney = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty liver = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty musculoskeletal = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty neurotoxicity = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty reproductive = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty respiratory = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty sense = new SimpleDoubleProperty(0);
	private SimpleDoubleProperty skin = new SimpleDoubleProperty(0);

	public ToxicityFacetRow() {
		
	}
	
	public String getCategory() {
		return category.get();
	}

	public void setCategory(SimpleStringProperty category) {
		this.category = category;
	}

	public Integer getBlood() {
		return ((Double)blood.get()).intValue();
	}

	public void setBlood(SimpleDoubleProperty blood) {
		this.blood = blood;
	}

	public Integer getCancer() {
		return ((Double)cancer.get()).intValue();
	}

	public void setCancer(SimpleDoubleProperty cancer) {
		this.cancer = cancer;
	}

	public Integer getCardiovascular() {
		return ((Double)cardiovascular.get()).intValue();
	}

	public void setCardiovascular(SimpleDoubleProperty cardiovascular) {
		this.cardiovascular = cardiovascular;
	}

	public Integer getDevelopmental() {
		return ((Double)developmental.get()).intValue();
	}

	public void setDevelopmental(SimpleDoubleProperty developmental) {
		this.developmental = developmental;
	}

	public Integer getEndocrine() {
		return ((Double)endocrine.get()).intValue();
	}

	public void setEndocrine(SimpleDoubleProperty endocrine) {
		this.endocrine = endocrine;
	}

	public Integer getGastrointestinal() {
		return ((Double)gastrointestinal.get()).intValue();
	}

	public void setGastrointestinal(SimpleDoubleProperty gastrointestinal) {
		this.gastrointestinal = gastrointestinal;
	}

	public Integer getImmunotoxicity() {
		return ((Double)immunotoxicity.get()).intValue();
	}

	public void setImmunotoxicity(SimpleDoubleProperty immunotoxicity) {
		this.immunotoxicity = immunotoxicity;
	}

	public Integer getKidney() {
		return ((Double)kidney.get()).intValue();
	}

	public void setKidney(SimpleDoubleProperty kidney) {
		this.kidney = kidney;
	}

	public Integer getLiver() {
		return ((Double)liver.get()).intValue();
	}

	public void setLiver(SimpleDoubleProperty liver) {
		this.liver = liver;
	}

	public Integer getMusculoskeletal() {
		return ((Double)musculoskeletal.get()).intValue();
	}

	public void setMusculoskeletal(SimpleDoubleProperty musculoskeletal) {
		this.musculoskeletal = musculoskeletal;
	}

	public Integer getNeurotoxicity() {
		return ((Double)neurotoxicity.get()).intValue();
	}

	public void setNeurotoxicity(SimpleDoubleProperty neurotoxicity) {
		this.neurotoxicity = neurotoxicity;
	}

	public Integer getReproductive() {
		return ((Double)reproductive.get()).intValue();
	}

	public void setReproductive(SimpleDoubleProperty reproductive) {
		this.reproductive = reproductive;
	}

	public Integer getRespiratory() {
		return ((Double)respiratory.get()).intValue();
	}

	public void setRespiratory(SimpleDoubleProperty respiratory) {
		this.respiratory = respiratory;
	}

	public Integer getSense() {
		return ((Double)sense.get()).intValue();
	}

	public void setSense(SimpleDoubleProperty sense) {
		this.sense = sense;
	}

	public Integer getSkin() {
		return ((Double)skin.get()).intValue();
	}

	public void setSkin(SimpleDoubleProperty skin) {
		this.skin = skin;
	}

}
