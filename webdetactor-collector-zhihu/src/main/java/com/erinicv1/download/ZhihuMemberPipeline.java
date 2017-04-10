package com.erinicv1.download;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.FilePipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
public class ZhihuMemberPipeline extends FilePipeline {

    private Logger logger = LoggerFactory.getLogger(ZhihuMemberPipeline.class);

    final static String URL = "url";

    final static String RESPONSE = "response";

    public ZhihuMemberPipeline(){
        setPath("/data/webmegic");
    }

    public ZhihuMemberPipeline(String path){
        setPath(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task){
        String path = this.path + PATH_SEPERATOR + task.getUUID() + PATH_SEPERATOR;
        try{
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getFile(path + DigestUtils.md5Hex(resultItems.getRequest().getUrl())) + ".html"),"UTF-8"));
            Map<String,Object> map = resultItems.getAll();
            writer.println(map.get(URL));
            writer.println(map.get(RESPONSE));
            writer.close();
        }catch (IOException e){
            logger.warn(" write file fail:", e);
        }
    }
}
