package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.SerializedName;

public class ObjectModel extends jsonModel{

    @SerializedName("data")
    private Object data;

    public ObjectModel(){

    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
