package com.sgo.hpku.interfaces;

public interface OnLoadDataListener {
	void onSuccess(Object deData);
	void onFail(String message);
	void onFailure();

}
