package com.erinicv1;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class ElasticsearchUploader {

    private  Logger logger = LoggerFactory.getLogger(ElasticsearchUploader.class);

    private BulkProcessor bulkProcessor;

    private BulkProcessor.Listener listener;

    private TransportClient transportClient;

    public ElasticsearchUploader(){
        init();
    }

    public void init(){

        try{
            transportClient = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName("localhost"),9300));
        }catch (UnknownHostException e){
            e.printStackTrace();
        }

        listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest) {
                logger.info(" bulk_request number of actions : " + bulkRequest.numberOfActions());
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                logger.info(" bulk_response has failures : " + bulkResponse.hasFailures());
            }

            @Override
            public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                logger.error(" bulk error : " + throwable);
            }
        };

        bulkProcessor = BulkProcessor.builder(transportClient,listener)
                .setBulkActions(10000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueMillis(10))
                .setConcurrentRequests(10)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(
                        TimeValue.timeValueMillis(100),3
                ))
                .build();
    }

    public void upload(String index,String type,Document document){
        bulkProcessor.add(new IndexRequest(index,type,document.getId())
                .source(document.getContent()));
    }

    public void awatiClose(long awaitTime, TimeUnit timeUnit) throws InterruptedException{
        bulkProcessor.awaitClose(awaitTime,timeUnit);
    }

    public void closeNow(){
        bulkProcessor.close();
    }

    public BulkProcessor getBulkProcessor() {
        return bulkProcessor;
    }


    public static void main(String[] args){
        ElasticsearchUploader uploader = new ElasticsearchUploader();
        uploader.logger.info("aaa");
    }
}
