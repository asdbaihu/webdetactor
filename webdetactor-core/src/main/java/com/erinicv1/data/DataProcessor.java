package com.erinicv1.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public interface DataProcessor<K,V> {
    Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    default void process(K inItem, DataFlow<V> out){
        List<V> outItems = process(inItem);
        if (outItems == null){
            logger.error("error: ", inItem);
            return ;
        }
        for (V item : outItems){
            out.push(item);
        }
    }

    List<V> process(K initem);
}
