package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateCardModel {

	@SerializedName("error_message")
	@Expose
	private String errorMessage;

	@SerializedName("mitra_code")
	@Expose
	private Object mitraCode;

	@SerializedName("update_card_key")
	@Expose
	private String updateCardKey;

	@SerializedName("card_attribute")
	@Expose
	private Object cardAttribute;

	@SerializedName("error_code")
	@Expose
	private String errorCode;

	@SerializedName("message")
	@Expose
	private Object message;

	@SerializedName("card_info")
	@Expose
	private Object cardInfo;

	@SerializedName("flag_finish")
	@Expose
	private Object flagFinish;

	@SerializedName("applet_type")
	@Expose
	private Object appletType;

	@SerializedName("merchant_type")
	@Expose
	private Object merchantType;

	@SerializedName("card_balance")
	@Expose
	private Object cardBalance;

	@SerializedName("card_uuid")
	@Expose
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

	public void setCardInfo(Object cardInfo){
		this.cardInfo = cardInfo;
	}

	public Object getCardInfo(){
		return cardInfo;
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
			"UpdateCardModel{" +
			"error_message = '" + errorMessage + '\'' + 
			",mitra_code = '" + mitraCode + '\'' + 
			",update_card_key = '" + updateCardKey + '\'' + 
			",card_attribute = '" + cardAttribute + '\'' + 
			",error_code = '" + errorCode + '\'' + 
			",message = '" + message + '\'' + 
			",card_info = '" + cardInfo + '\'' + 
			",flag_finish = '" + flagFinish + '\'' + 
			",applet_type = '" + appletType + '\'' + 
			",merchant_type = '" + merchantType + '\'' + 
			",card_balance = '" + cardBalance + '\'' + 
			",card_uuid = '" + cardUuid + '\'' + 
			"}";
		}
}