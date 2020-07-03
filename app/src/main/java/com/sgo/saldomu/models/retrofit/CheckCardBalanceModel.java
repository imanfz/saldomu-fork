package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckCardBalanceModel {

	@SerializedName("error_message")
	@Expose
	private String errorMessage;

	@SerializedName("session")
	@Expose
	private String session;

	@SerializedName("merchant_data")
	@Expose
	private String merchantData;

	@SerializedName("card_attribute")
	@Expose
	private String cardAttribute;

	@SerializedName("applet_type")
	@Expose
	private String appletType;

	@SerializedName("pending_amount")
	@Expose
	private String pendingAmount;

	@SerializedName("card_balance")
	@Expose
	private String cardBalance;

	@SerializedName("card_uuid")
	@Expose
	private String cardUuid;

	@SerializedName("mitra_code")
	@Expose
	private String mitraCode;

	@SerializedName("source_of_account")
	@Expose
	private String sourceOfAccount;

	@SerializedName("update_card_key")
	@Expose
	private String updateCardKey;

	@SerializedName("error_code")
	@Expose
	private String errorCode;

	@SerializedName("institution_reff")
	@Expose
	private String institutionReff;

	@SerializedName("order_id")
	@Expose
	private String orderId;

	@SerializedName("card_info")
	@Expose
	private String cardInfo;

	@SerializedName("merchant_type")
	@Expose
	private String merchantType;

	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage(){
		return errorMessage;
	}

	public void setSession(String session){
		this.session = session;
	}

	public String getSession(){
		return session;
	}

	public void setMerchantData(String merchantData){
		this.merchantData = merchantData;
	}

	public String getMerchantData(){
		return merchantData;
	}

	public void setCardAttribute(String cardAttribute){
		this.cardAttribute = cardAttribute;
	}

	public String getCardAttribute(){
		return cardAttribute;
	}

	public void setAppletType(String appletType){
		this.appletType = appletType;
	}

	public String getAppletType(){
		return appletType;
	}

	public void setPendingAmount(String pendingAmount){
		this.pendingAmount = pendingAmount;
	}

	public String getPendingAmount(){
		return pendingAmount;
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

	public void setMitraCode(String mitraCode){
		this.mitraCode = mitraCode;
	}

	public String getMitraCode(){
		return mitraCode;
	}

	public void setSourceOfAccount(String sourceOfAccount){
		this.sourceOfAccount = sourceOfAccount;
	}

	public String getSourceOfAccount(){
		return sourceOfAccount;
	}

	public void setUpdateCardKey(String updateCardKey){
		this.updateCardKey = updateCardKey;
	}

	public String getUpdateCardKey(){
		return updateCardKey;
	}

	public void setErrorCode(String errorCode){
		this.errorCode = errorCode;
	}

	public String getErrorCode(){
		return errorCode;
	}

	public void setInstitutionReff(String institutionReff){
		this.institutionReff = institutionReff;
	}

	public String getInstitutionReff(){
		return institutionReff;
	}

	public void setOrderId(String orderId){
		this.orderId = orderId;
	}

	public String getOrderId(){
		return orderId;
	}

	public void setCardInfo(String cardInfo){
		this.cardInfo = cardInfo;
	}

	public String getCardInfo(){
		return cardInfo;
	}

	public void setMerchantType(String merchantType){
		this.merchantType = merchantType;
	}

	public String getMerchantType(){
		return merchantType;
	}

	@Override
 	public String toString(){
		return 
			"CheckCardBalanceModel{" + 
			"error_message = '" + errorMessage + '\'' + 
			",session = '" + session + '\'' + 
			",merchant_data = '" + merchantData + '\'' + 
			",card_attribute = '" + cardAttribute + '\'' + 
			",applet_type = '" + appletType + '\'' + 
			",pending_amount = '" + pendingAmount + '\'' + 
			",card_balance = '" + cardBalance + '\'' + 
			",card_uuid = '" + cardUuid + '\'' + 
			",mitra_code = '" + mitraCode + '\'' + 
			",source_of_account = '" + sourceOfAccount + '\'' + 
			",update_card_key = '" + updateCardKey + '\'' + 
			",error_code = '" + errorCode + '\'' + 
			",institution_reff = '" + institutionReff + '\'' + 
			",order_id = '" + orderId + '\'' + 
			",card_info = '" + cardInfo + '\'' + 
			",merchant_type = '" + merchantType + '\'' + 
			"}";
		}
}