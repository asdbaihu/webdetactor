package com.erinicv1.processor;

import com.erinicv1.Document;
import com.erinicv1.SegmentReader;
import com.erinicv1.data.DataProcessor;
import com.erinicv1.data.DuplicateRemover;
import com.erinicv1.data.HashSetDuplicateRemover;
import org.apache.commons.lang.StringUtils;
import us.codecraft.webmagic.selector.Json;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class ZhihuFolloweeDataProcessor implements DataProcessor<File,Document> {

    private DuplicateRemover<String> remover;

    public ZhihuFolloweeDataProcessor(){
        this.remover = new HashSetDuplicateRemover<>();
    }
    @Override
    public List<Document> process(File file){
        List<Document> documents = new ArrayList<>(20);
        String data = SegmentReader.readFolowee(file);
        if (!StringUtils.isEmpty(data)) {
            Json json = new Json(data);
            List<String> ids = json.jsonPath("$.data[*].id").all();
            List<String> users = json.jsonPath("$.data[*].[*]").all();
            for (int i = 0; i < ids.size(); i++){
                if (!remover.isDuplicate(ids.get(i))){
                    documents.add(new Document(ids.get(i),users.get(i)));
                }
            }
        }

        return documents;
    }
}
