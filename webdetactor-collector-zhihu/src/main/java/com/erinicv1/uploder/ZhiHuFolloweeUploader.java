package com.erinicv1.uploder;

import com.erinicv1.Document;
import com.erinicv1.ElasticsearchUploader;
import com.erinicv1.data.OutPipeline;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class ZhiHuFolloweeUploader extends ElasticsearchUploader implements OutPipeline<Document>, AutoCloseable {

    private AtomicLong count = new AtomicLong(0);

    private String index;

    private String type;

    private long timeOut = 1L;

    private TimeUnit timeUnit = TimeUnit.MINUTES;

    public ZhiHuFolloweeUploader(String index, String type){
        this.index = index;
        this.type = type;
    }

    public void upload(Document document){
        upload(this.index,this.type,document);
    }

    @Override
    public void process(Document document){
        upload(document);
        count.incrementAndGet();
    }

    public void setTimeOut(long timeOut,TimeUnit timeUnit){
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
    }

    public AtomicLong getCount() {
        return count;
    }

    @Override
    public void close() throws InterruptedException{
        awatiClose(timeOut,timeUnit);
    }
}
