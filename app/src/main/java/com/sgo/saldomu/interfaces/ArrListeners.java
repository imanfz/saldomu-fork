package com.sgo.saldomu.interfaces;

import org.json.JSONArray;

public interface ArrListeners {
    void onResponses(JSONArray response);
    void onError(Throwable throwable);
    void onComplete();
}
