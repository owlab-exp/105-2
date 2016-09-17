package com.example.android.calendartest6;

import java.io.Serializable;

/**
 * Created by ernest on 16. 9. 17.
 */
public class Tuple2<T, S> implements Serializable {
    public final T t;
    public final S s;
    public Tuple2(T t, S s) {
        this.t = t;
        this.s = s;
    }
}
