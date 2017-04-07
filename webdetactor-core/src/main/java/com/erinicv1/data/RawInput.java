package com.erinicv1.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class RawInput<T> extends DataFlow<T> {
    private static Logger logger = LoggerFactory.getLogger(RawInput.class);

    public int getLeftCount(){
        return queue.size();
    }

    public T poll(){
        return queue.poll();
    }
}
