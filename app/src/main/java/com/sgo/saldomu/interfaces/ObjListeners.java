package com.sgo.saldomu.interfaces;

import com.google.gson.JsonObject;

import org.json.JSONObject;

public interface ObjListeners {
    void onResponses(JSONObject response);
    void onError(Throwable throwable);
    void onComplete();
}
