package com.sgo.saldomu.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;


public class BankBillerItem extends RealmObject {

	@SerializedName("bank_code")
	private String bankCode;

	@SerializedName("product_h2h")
	private String productH2h;

	@SerializedName("product_type")
	private String productType;

	@SerializedName("bank_name")
	private String bankName;

	@SerializedName("product_code")
	private String productCode;

	@SerializedName("product_name")
	private String productName;

	public void setBankCode(String bankCode){
		this.bankCode = bankCode;
	}

	public String getBankCode(){
		return bankCode;
	}

	public void setProductH2h(String productH2h){
		this.productH2h = productH2h;
	}

	public String getProductH2h(){
		return productH2h;
	}

	public void setProductType(String productType){
		this.productType = productType;
	}

	public String getProductType(){
		return productType;
	}

	public void setBankName(String bankName){
		this.bankName = bankName;
	}

	public String getBankName(){
		return bankName;
	}

	public void setProductCode(String productCode){
		this.productCode = productCode;
	}

	public String getProductCode(){
		return productCode;
	}

	public void setProductName(String productName){
		this.productName = productName;
	}

	public String getProductName(){
		return productName;
	}

	@Override
 	public String toString(){
		return 
			"BankBillerItem{" + 
			"bank_code = '" + bankCode + '\'' + 
			",product_h2h = '" + productH2h + '\'' + 
			",product_type = '" + productType + '\'' + 
			",bank_name = '" + bankName + '\'' + 
			",product_code = '" + productCode + '\'' + 
			",product_name = '" + productName + '\'' + 
			"}";
		}
}