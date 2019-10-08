package com.sgo.saldomu.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;

public class BillerDenomResponse {

	@SerializedName("error_message")
	private String errorMessage;

	@SerializedName("biller_data")
	private List<BillerItem> biller;

	@SerializedName("error_code")
	private String errorCode;

	@SerializedName("biller_info")
	private String billerInfo;

	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage(){
		return errorMessage;
	}

	public void setBiller(List<BillerItem> biller){
		this.biller = biller;
	}

	public List<BillerItem> getBiller(){
		return biller;
	}

	public void setErrorCode(String errorCode){
		this.errorCode = errorCode;
	}

	public String getErrorCode(){
		return errorCode;
	}

	public void setBillerInfo(String billerInfo){
		this.billerInfo = billerInfo;
	}

	public String getBillerInfo(){
		return billerInfo;
	}

	@Override
 	public String toString(){
		return 
			"Response{" + 
			"error_message = '" + errorMessage + '\'' + 
			",biller = '" + biller + '\'' + 
			",error_code = '" + errorCode + '\'' + 
			",biller_info = '" + billerInfo + '\'' +
			"}";
		}
}