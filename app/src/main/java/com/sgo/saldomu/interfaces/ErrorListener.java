package com.sgo.saldomu.interfaces;

import com.google.gson.JsonObject;

public interface ErrorListener extends ObjListener {
    void onError(Throwable e);
}
