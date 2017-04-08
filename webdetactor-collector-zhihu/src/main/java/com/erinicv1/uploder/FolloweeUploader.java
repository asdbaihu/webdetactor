package com.erinicv1.uploder;

import com.erinicv1.Document;
import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.data.BaseAssembler;
import com.erinicv1.data.DataProcessor;
import com.erinicv1.data.FileRawInput;
import com.erinicv1.processor.ZhihuFolloweeDataProcessor;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class FolloweeUploader {

    public static void upload(){
        String index = "pprzhihu";
        String type = "followee";

        ZhihuConfiguration configuration = new ZhihuConfiguration();
        DataProcessor<File,Document> processor = new ZhihuFolloweeDataProcessor();

        ZhiHuFolloweeUploader uploader = new ZhiHuFolloweeUploader(index,type);
        uploader.setTimeOut(5, TimeUnit.MINUTES);

        BaseAssembler.create(new FileRawInput(configuration.getFolloweeDataPath()),processor)
                .addPipeline(uploader)
                .thread(10)
                .run();

        System.out.println("out sent :" + uploader.getCount());
        System.out.println(uploader.getBulkProcessor());
    }

    public static void main(String[] args) {
        FolloweeUploader.upload();
    }
}
