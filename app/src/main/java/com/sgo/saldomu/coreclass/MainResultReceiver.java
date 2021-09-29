package com.sgo.saldomu.coreclass;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Lenovo Thinkpad on 12/1/2016.
 */
@SuppressLint("ParcelCreator")
public class MainResultReceiver extends ResultReceiver
{
    private Receiver receiver;

    // Constructor takes a handler
    public MainResultReceiver (Handler handler)
    {
        super(handler);
    }

    // Setter for assigning the receiver
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    // Defines our event interface for communication
    public interface Receiver {
        public void onReceiveResult(int resultCode);
    }

    // Delegate method which passes the result to the receiver if the receiver has been assigned
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode);
        }
    }
}
