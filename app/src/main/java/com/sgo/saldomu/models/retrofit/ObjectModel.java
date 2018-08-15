package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.SerializedName;

public class ObjectModel extends jsonModel{

    @SerializedName("data")
    private Object data;

    public ObjectModel(String e){
        setMessage(e);
        setErrorCode(1000);
        setStatusCode(1000);
        setStatus(e);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
