package com.sgo.saldomu.interfaces;

import com.google.gson.JsonObject;

public interface ResponseListener  {
    void onResponses(JsonObject object);
    void onError(Throwable throwable);
    void onComplete();
}
