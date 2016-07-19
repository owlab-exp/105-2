package com.example.android.calendartest6;

import android.util.Log;

/**
 * Created by ernest on 7/19/16.
 */
public class LOG {
    private final String TAG;
    public LOG(Class klass) {
        this.TAG = klass.getSimpleName();
    }

    public void d(String logText) {
        Log.d(TAG, logText);
    }
}
