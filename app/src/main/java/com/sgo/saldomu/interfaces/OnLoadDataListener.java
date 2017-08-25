package com.sgo.saldomu.interfaces;

import android.os.Bundle;

public interface OnLoadDataListener {
	void onSuccess(Object deData);
	void onFail(Bundle message);
	void onFailure(String message);

}
