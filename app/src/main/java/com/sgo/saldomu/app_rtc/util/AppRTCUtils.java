package com.sgo.saldomu.app_rtc.util;

/**
 * Created by Lenovo on 02/05/2018.
 */

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 */
public final class AppRTCUtils {
    private AppRTCUtils() {
    }

    /**
     * Helper method which throws an exception  when an assertion has failed.
     */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /**
     * Helper method for building a string of thread information.
     */
    public static String getThreadInfo() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
                + "]";
    }

}
