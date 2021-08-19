package com.sgo.saldomu.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class PatternBillerItem extends RealmObject {

	@SerializedName("id")
	private String id;

	@SerializedName("label")
	private String label;

	@SerializedName("type")
	private String type;

	@SerializedName("type_value")
	private String typeValue;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

	@Override
 	public String toString(){
		return 
			"PatternBillerItem{" +
			"id = '" + id + '\'' +
			",label = '" + label + '\'' +
			",type = '" + type + '\'' +
			",type_value = '" + typeValue + '\'' +
			"}";
		}
}