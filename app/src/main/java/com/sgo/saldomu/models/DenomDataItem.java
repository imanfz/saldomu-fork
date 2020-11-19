package com.sgo.saldomu.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DenomDataItem extends RealmObject {

	@PrimaryKey
	@SerializedName("item_id")
	private String item_id;

	@SerializedName("item_price")
	private String itemPrice;

	@SerializedName("item_name")
	private String item_name;

	@SerializedName("item_description")
	private String item_description;

	@SerializedName("ccy_id")
	private String ccyId;

	public void setItemId(String item_id){
		this.item_id = item_id;
	}

	public String getItemId(){
		return item_id;
	}

	public void setItemPrice(String itemPrice){
		this.itemPrice = itemPrice;
	}

	public String getItemPrice(){
		return itemPrice;
	}

	public void setItemName(String itemName){
		this.item_name = itemName;
	}

	public String getItemName(){
		return item_name;
	}

	public String getItemDescription() {
		return item_description;
	}

	public void setItemDescription(String item_description) {
		this.item_description = item_description;
	}

	public void setCcyId(String ccyId){
		this.ccyId = ccyId;
	}

	public String getCcyId(){
		return ccyId;
	}

	@Override
 	public String toString(){
		return 
			"DenomDataItem{" + 
			"item_id = '" + item_id + '\'' +
			",item_price = '" + itemPrice + '\'' + 
			",item_name = '" + item_name + '\'' +
			",ccy_id = '" + ccyId + '\'' + 
			"}";
		}
}