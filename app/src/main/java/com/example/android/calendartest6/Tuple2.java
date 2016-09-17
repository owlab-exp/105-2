package com.example.android.calendartest6;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof Tuple2<?, ?>) {
            Tuple2<?, ?> other = (Tuple2<?, ?>)o;
            return Objects.equals(t, other.t) && Objects.equals(s, other.s);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, s);
    }

}
