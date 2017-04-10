package com.erinicv1.uploder;

import com.erinicv1.Document;
import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.data.BaseAssembler;
import com.erinicv1.data.DataProcessor;
import com.erinicv1.data.FileRawInput;
import com.erinicv1.processor.ZhihuMemmberDataProcessor;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
public class MemberUploader {

    public static void upload(){
        String index = "mzhihu";
        String type = "memmber";

        ZhihuConfiguration configuration = new ZhihuConfiguration();
        ZhiHuFolloweeUploader outPipeline = new ZhiHuFolloweeUploader(index,type);
        outPipeline.setTimeOut(5, TimeUnit.MINUTES);
        DataProcessor<File,Document> processor = new ZhihuMemmberDataProcessor();

        BaseAssembler.create(new FileRawInput(configuration.getMemberDataPath()),processor)
                .addPipeline(outPipeline)
                .thread(10)
                .run();

        System.out.println("out sent :" + outPipeline.getCount());
        System.out.println(outPipeline.getBulkProcessor());
    }

    public static void main(String[] args) {
        MemberUploader.upload();
    }
}
