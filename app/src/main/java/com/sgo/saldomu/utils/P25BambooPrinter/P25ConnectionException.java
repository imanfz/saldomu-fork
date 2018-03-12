package com.sgo.saldomu.utils.P25BambooPrinter;

/**
 * Created by Lenovo Thinkpad on 3/11/2018.
 */

public class P25ConnectionException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    String error = "";

    public P25ConnectionException(String msg) {
        super(msg);

        error = msg;
    }

    public String getError() {
        return error;
    }

}