package com.sgo.saldomu.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BillerItem extends RealmObject implements Parcelable {

    @SerializedName("denom_data")
    private RealmList<DenomDataItem> denomData;

    @SerializedName("comm_name")
    private String comm_name;

    @PrimaryKey
    @SerializedName("item_id")
    private String item_id;

    @SerializedName("api_key")
    private String apiKey;

    @SerializedName("comm_code")
    private String comm_code;

    //	@PrimaryKey
    @SerializedName("comm_id")
    private String comm_id;

    @SerializedName("bank_biller")
    private RealmList<BankBillerItem> bankBiller = new RealmList<>();

    @SerializedName("biller_type")
    private String billerType;

    @SerializedName("biller_id_patterns")
    private RealmList<PatternBillerItem> billerIdPatterns = new RealmList<>();

    public void setDenomData(RealmList<DenomDataItem> denomData) {
        this.denomData = denomData;
    }

    public RealmList<DenomDataItem> getDenomData() {
        return denomData;
    }

    public void setCommName(String comm_name) {
        this.comm_name = comm_name;
    }

    public String getCommName() {
        return comm_name;
    }

    public void setItemId(String itemId) {
        this.item_id = itemId;
    }

    public String getItemId() {
        return item_id;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setCommCode(String comm_code) {
        this.comm_code = comm_code;
    }

    public String getCommCode() {
        return comm_code;
    }

    public void setCommId(String comm_id) {
        this.comm_id = comm_id;
    }

    public String getCommId() {
        return comm_id;
    }

    public void setBankBiller(RealmList<BankBillerItem> bankBiller) {
        this.bankBiller = bankBiller;
    }

    public RealmList<BankBillerItem> getBankBiller() {
        return bankBiller;
    }

    public void setBillerType(String billerType) {
        this.billerType = billerType;
    }

    public String getBillerType() {
        return billerType;
    }

    public RealmList<PatternBillerItem> getBillerIdPatterns() {
        return billerIdPatterns;
    }

    public void setBillerIdPatterns(RealmList<PatternBillerItem> billerIdPatterns) {
        this.billerIdPatterns = billerIdPatterns;
    }

    @Override
    public String toString() {
        return
                "BillerItem{" +
                        "denom_data = '" + denomData + '\'' +
                        ",comm_name = '" + comm_name + '\'' +
                        ",item_id = '" + item_id + '\'' +
                        ",api_key = '" + apiKey + '\'' +
                        ",comm_code = '" + comm_code + '\'' +
                        ",comm_id = '" + comm_id + '\'' +
//			",bank_biller = '" + bankBiller + '\'' +
                        ",biller_type = '" + billerType + '\'' +
                        ",biller_id_patterns = '" + billerIdPatterns + '\'' +
                        "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}