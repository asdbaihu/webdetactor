package com.erinicv1.data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class ConsoleOutPipeline<T> implements OutPipeline<T> {

    private AtomicLong count = new AtomicLong(0);
    @Override
    public void process(T item){
        count.incrementAndGet();
        System.out.println(item);
    }

    public AtomicLong getCount(){
        return count;
    }
}
