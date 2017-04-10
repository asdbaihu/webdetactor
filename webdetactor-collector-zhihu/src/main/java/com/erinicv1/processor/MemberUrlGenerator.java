package com.erinicv1.processor;

import com.erinicv1.SegmentReader;
import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.data.BaseAssembler;
import com.erinicv1.data.DataProcessor;
import com.erinicv1.data.FileRawInput;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Json;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
public class MemberUrlGenerator implements DataProcessor<File,String>{

    private static Logger logger = LoggerFactory.getLogger(MemberUrlGenerator.class);

    private Set<String> urlTokens = Sets.newSetFromMap(new ConcurrentHashMap<>());

    private final static String FILE_NAME = "url_tokens";

    private final static String DEFAULT_PATH = new ZhihuConfiguration().getFolloweePath() + FILE_NAME;


    @Override
    public List<String> process(File item){
        String url = SegmentReader.readFolowee(item);

        if (!StringUtils.isEmpty(url)){
            Json json = new Json(url);
            List<String> urls = json.jsonPath("data[*].url_token").all();
            urlTokens.addAll(urls);
        }

        return null;
    }

    public void save(){
        save(DEFAULT_PATH);
    }


    public Set<String> extractUrlToken(String path){
        BaseAssembler.create(new FileRawInput(path),this)
                .thread(10)
                .run();

        return urlTokens;
    }

    public Set<String> getUrlTokens(){
        return getUrlTokens(DEFAULT_PATH);
    }

    public void save(String path){
        try{
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));

            for (String token : urlTokens){
                writer.println(token);
            }
            writer.close();
        }catch (IOException e){
            logger.error(" write file error : ",e);
        }
    }

    public Set<String> getUrlTokens(String path){
        Set<String> tokens = new HashSet<>();
        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader(path));
            String s;
            while ((s = br.readLine()) != null){
                tokens.add(s);
            }


        }catch (IOException e){
            logger.error(" read token file fail : ", e);
        }
        return tokens;
    }

    public static void main(String[] args){
        ZhihuConfiguration configuration = new ZhihuConfiguration();
        MemberUrlGenerator generator = new MemberUrlGenerator();
        generator.extractUrlToken(configuration.getFolloweeDataPath());
        generator.save();
    }
}
