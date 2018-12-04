package com.sgo.saldomu.interfaces;

import android.os.Bundle;

public interface OnLoadDataListeners {
	void onSuccess(int attempts, int failed);
	void onFail(Bundle message);
	void onFailure(String message);
}
