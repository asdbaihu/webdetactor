package com.erinicv1.processor;

import com.erinicv1.Document;
import com.erinicv1.SegmentReader;
import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.data.*;
import org.apache.commons.lang.StringUtils;
import us.codecraft.webmagic.selector.Json;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
public class ZhihuMemmberDataProcessor implements DataProcessor<File,Document> {

    private DuplicateRemover<String> remover;

    public ZhihuMemmberDataProcessor(){
        this.remover = new HashSetDuplicateRemover<>();
    }

    @Override
    public List<Document> process(File file){
        List<Document> documents = null;

        String token = SegmentReader.readMember(file);
        if (!StringUtils.isEmpty(token)){
            documents = new ArrayList<>(1);
            Json json = new Json(token);
            String id = json.jsonPath("$.id").get();
            if (!remover.isDuplicate(id)){
                documents.add(new Document(id,token));
            }
        }
        return documents;
    }

    public static void main(String[] args) {
        ZhihuConfiguration configuration = new ZhihuConfiguration();
        String folder = configuration.getMemberDataPath();
        DataProcessor<File, Document> processor = new ZhihuMemmberDataProcessor();
        ConsoleOutPipeline<Document> outPipeline = new ConsoleOutPipeline<>();

        BaseAssembler.create(new FileRawInput(folder), processor)
                .addPipeline(outPipeline)
                .thread(10)
                .run();

        System.out.println("out sent :" + outPipeline.getCount());
    }
}
