package com.sgo.saldomu.interfaces;

import com.google.gson.JsonObject;
import com.sgo.saldomu.models.retrofit.ObjectModel;

public interface ObjListener {
    void onResponses(JsonObject object);
}
