package com.erinicv1.data;

import java.util.List;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public interface OutPipeline<T> {

    void process(T item);

    default void process(List<T> items){
        for (T item : items){
            process(item);
        }
    }
}
