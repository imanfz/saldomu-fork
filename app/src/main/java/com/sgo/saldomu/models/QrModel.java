package com.sgo.saldomu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class QrModel implements Parcelable{


    private String sourceAcct="";
    private String sourceName="";
    private String amount="";
    private String qrType="";
    private String messages="";

    public QrModel(){

    }

    public QrModel(String sourceAcct, String sourceName, String qrType) {
        this.sourceAcct = sourceAcct;
        this.sourceName = sourceName;
        this.qrType = qrType;
    }

    protected QrModel(Parcel in) {
        sourceAcct = in.readString();
        sourceName = in.readString();
        amount = in.readString();
        qrType = in.readString();
        messages = in.readString();
    }

    public static final Creator<QrModel> CREATOR = new Creator<QrModel>() {
        @Override
        public QrModel createFromParcel(Parcel in) {
            return new QrModel(in);
        }

        @Override
        public QrModel[] newArray(int size) {
            return new QrModel[size];
        }
    };

    public String getSourceAcct() {
        return sourceAcct;
    }

    public void setSourceAcct(String sourceAcct) {
        this.sourceAcct = sourceAcct;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getQrType() {
        return qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sourceAcct);
        dest.writeString(sourceName);
        dest.writeString(amount);
        dest.writeString(qrType);
        dest.writeString(messages);
    }
}
