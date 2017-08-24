package com.sgo.saldomu.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by yuddistirakiki on 8/7/17.
 */

public class LocalResultReceiver extends ResultReceiver {
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */

    private LocalResultInterface localResultInterface;

    public LocalResultReceiver(Handler handler) {
        super(handler);
    }

    public interface LocalResultInterface{
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(LocalResultInterface localResultInterface){
        this.localResultInterface = localResultInterface;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        localResultInterface.onReceiveResult(resultCode,resultData);
    }

}
