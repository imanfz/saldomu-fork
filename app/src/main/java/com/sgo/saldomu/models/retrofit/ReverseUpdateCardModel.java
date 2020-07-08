package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReverseUpdateCardModel{

	@SerializedName("error_message")
	@Expose
	private String errorMessage;

	@SerializedName("mitra_code")
	@Expose
	private String mitraCode;

	@SerializedName("update_card_key")
	@Expose
	private String updateCardKey;

	@SerializedName("card_no")
	@Expose
	private Object cardNo;

	@SerializedName("card_attribute")
	@Expose
	private String cardAttribute;

	@SerializedName("error_code")
	@Expose
	private String errorCode;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("reverse_card_key")
	@Expose
	private String reverseCardKey;

	@SerializedName("flag_finish")
	@Expose
	private String flagFinish;

	@SerializedName("applet_type")
	@Expose
	private String appletType;

	@SerializedName("card_balance")
	@Expose
	private String cardBalance;

	@SerializedName("card_uuid")
	@Expose
	private String cardUuid;

	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage(){
		return errorMessage;
	}

	public void setMitraCode(String mitraCode){
		this.mitraCode = mitraCode;
	}

	public String getMitraCode(){
		return mitraCode;
	}

	public void setUpdateCardKey(String updateCardKey){
		this.updateCardKey = updateCardKey;
	}

	public String getUpdateCardKey(){
		return updateCardKey;
	}

	public void setCardNo(Object cardNo){
		this.cardNo = cardNo;
	}

	public Object getCardNo(){
		return cardNo;
	}

	public void setCardAttribute(String cardAttribute){
		this.cardAttribute = cardAttribute;
	}

	public String getCardAttribute(){
		return cardAttribute;
	}

	public void setErrorCode(String errorCode){
		this.errorCode = errorCode;
	}

	public String getErrorCode(){
		return errorCode;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setReverseCardKey(String reverseCardKey){
		this.reverseCardKey = reverseCardKey;
	}

	public String getReverseCardKey(){
		return reverseCardKey;
	}

	public void setFlagFinish(String flagFinish){
		this.flagFinish = flagFinish;
	}

	public String getFlagFinish(){
		return flagFinish;
	}

	public void setAppletType(String appletType){
		this.appletType = appletType;
	}

	public String getAppletType(){
		return appletType;
	}

	public void setCardBalance(String cardBalance){
		this.cardBalance = cardBalance;
	}

	public String getCardBalance(){
		return cardBalance;
	}

	public void setCardUuid(String cardUuid){
		this.cardUuid = cardUuid;
	}

	public String getCardUuid(){
		return cardUuid;
	}

	@Override
 	public String toString(){
		return 
			"ReverseUpdateCardModel{" + 
			"error_message = '" + errorMessage + '\'' + 
			",mitra_code = '" + mitraCode + '\'' + 
			",update_card_key = '" + updateCardKey + '\'' + 
			",card_no = '" + cardNo + '\'' + 
			",card_attribute = '" + cardAttribute + '\'' + 
			",error_code = '" + errorCode + '\'' + 
			",message = '" + message + '\'' + 
			",reverse_card_key = '" + reverseCardKey + '\'' + 
			",flag_finish = '" + flagFinish + '\'' + 
			",applet_type = '" + appletType + '\'' + 
			",card_balance = '" + cardBalance + '\'' + 
			",card_uuid = '" + cardUuid + '\'' + 
			"}";
		}
}