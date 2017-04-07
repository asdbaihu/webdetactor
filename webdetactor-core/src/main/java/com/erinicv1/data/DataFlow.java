package com.erinicv1.data;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class DataFlow<T> {

    BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    protected void push(T item){
        queue.add(item);
    }

    protected T poll(){
        return queue.poll();
    }

    protected T take() throws InterruptedException{
        return queue.take();
    }
}
