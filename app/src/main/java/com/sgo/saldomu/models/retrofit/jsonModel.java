package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.SerializedName;

public class jsonModel {
    @SerializedName("status")
    private String status;
    @SerializedName("statusCode")
    private Integer statusCode;
    @SerializedName("errorCode")
    private Integer errorCode;
    @SerializedName("message")
    private String message;
//    @SerializedName("payload")
//    private List<Payload> payload;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

//    public List<Payload> getPayload() {
//        return payload;
//    }
//
//    public void setPayload(List<Payload> payload) {
//        this.payload = payload;
//    }
}
