package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.SerializedName;

public class ReverseCardModel{

	@SerializedName("error_message")
	private String errorMessage;

	@SerializedName("mitra_code")
	private Object mitraCode;

	@SerializedName("update_card_key")
	private String updateCardKey;

	@SerializedName("card_attribute")
	private Object cardAttribute;

	@SerializedName("error_code")
	private String errorCode;

	@SerializedName("message")
	private Object message;

	@SerializedName("reverse_card_key")
	private Object reverseCardKey;

	@SerializedName("flag_finish")
	private Object flagFinish;

	@SerializedName("applet_type")
	private Object appletType;

	@SerializedName("merchant_type")
	private Object merchantType;

	@SerializedName("card_balance")
	private Object cardBalance;

	@SerializedName("card_uuid")
	private Object cardUuid;

	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage(){
		return errorMessage;
	}

	public void setMitraCode(Object mitraCode){
		this.mitraCode = mitraCode;
	}

	public Object getMitraCode(){
		return mitraCode;
	}

	public void setUpdateCardKey(String updateCardKey){
		this.updateCardKey = updateCardKey;
	}

	public String getUpdateCardKey(){
		return updateCardKey;
	}

	public void setCardAttribute(Object cardAttribute){
		this.cardAttribute = cardAttribute;
	}

	public Object getCardAttribute(){
		return cardAttribute;
	}

	public void setErrorCode(String errorCode){
		this.errorCode = errorCode;
	}

	public String getErrorCode(){
		return errorCode;
	}

	public void setMessage(Object message){
		this.message = message;
	}

	public Object getMessage(){
		return message;
	}

	public void setReverseCardKey(Object reverseCardKey){
		this.reverseCardKey = reverseCardKey;
	}

	public Object getReverseCardKey(){
		return reverseCardKey;
	}

	public void setFlagFinish(Object flagFinish){
		this.flagFinish = flagFinish;
	}

	public Object getFlagFinish(){
		return flagFinish;
	}

	public void setAppletType(Object appletType){
		this.appletType = appletType;
	}

	public Object getAppletType(){
		return appletType;
	}

	public void setMerchantType(Object merchantType){
		this.merchantType = merchantType;
	}

	public Object getMerchantType(){
		return merchantType;
	}

	public void setCardBalance(Object cardBalance){
		this.cardBalance = cardBalance;
	}

	public Object getCardBalance(){
		return cardBalance;
	}

	public void setCardUuid(Object cardUuid){
		this.cardUuid = cardUuid;
	}

	public Object getCardUuid(){
		return cardUuid;
	}

	@Override
 	public String toString(){
		return 
			"ReverseCardModel{" + 
			"error_message = '" + errorMessage + '\'' + 
			",mitra_code = '" + mitraCode + '\'' + 
			",update_card_key = '" + updateCardKey + '\'' + 
			",card_attribute = '" + cardAttribute + '\'' + 
			",error_code = '" + errorCode + '\'' + 
			",message = '" + message + '\'' + 
			",reverse_card_key = '" + reverseCardKey + '\'' + 
			",flag_finish = '" + flagFinish + '\'' + 
			",applet_type = '" + appletType + '\'' + 
			",merchant_type = '" + merchantType + '\'' + 
			",card_balance = '" + cardBalance + '\'' + 
			",card_uuid = '" + cardUuid + '\'' + 
			"}";
		}
}