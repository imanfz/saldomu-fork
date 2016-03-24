package com.sgo.orimakardaya.interfaces;

import org.json.JSONObject;

public interface OnLoadDataListener {
	void onSuccess(Object deData);
	void onFail(String message);
	void onFailure();

}
