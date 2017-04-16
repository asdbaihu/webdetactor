package com.erinicv1.processor;

import com.erinicv1.SegmentReader;
import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.data.BaseAssembler;
import com.erinicv1.data.DataProcessor;
import com.erinicv1.data.FileRawInput;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Json;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2017/4/13 0013.
 */
public class MemberEncodeUrlGenerator implements DataProcessor<File,String> {

    private static Logger logger = LoggerFactory.getLogger(MemberEncodeUrlGenerator.class);

    private Set<String> encode_token = Sets.newSetFromMap(new ConcurrentHashMap<>());

    private final static String FILE_NAME = "urlEncode_token";

    private final static String CURSOR_NAME = new ZhihuConfiguration().getFolloweePath() + "cursor.txt";

    private final static String FILE_PATH = new ZhihuConfiguration().getFolloweePath() + FILE_NAME;

    private ScheduledExecutorService threadPool;

    private PrintWriter cursorWriter;

    private PrintWriter urlWriter;
    private AtomicLong cursor = new AtomicLong(0);

    private AtomicLong inneer = new AtomicLong(0);

    private final static String URL_TEMPLATE = "https://www.zhihu.com/api/v4/members/%s";

    private final static String QUERY_PARAMS = "?include=locations%2Cemployments%2Cgender%2Ceducations%2Cbusiness%2Cvoteup_count%2Cthanked_Count%2Cfollower_count%2Cfollowing_count%2Ccover_url%2Cfollowing_topic_count%2Cfollowing_question_count%2Cfollowing_favlists_count%2Cfollowing_columns_count%2Canswer_count%2Carticles_count%2Cpins_count%2Cquestion_count%2Cfavorite_count%2Cfavorited_count%2Clogs_count%2Cmarked_answers_count%2Cmarked_answers_text%2Cmessage_thread_token%2Caccount_status%2Cis_active%2Cis_force_renamed%2Cis_bind_sina%2Csina_weibo_url%2Csina_weibo_name%2Cshow_sina_weibo%2Cis_blocking%2Cis_blocked%2Cmutual_followees_count%2Cvote_to_count%2Cvote_from_count%2Cthank_to_count%2Cthank_from_count%2Cthanked_count%2Cdescription%2Chosted_live_count%2Cparticipated_live_count%2Callow_message%2Cindustry_category%2Corg_name%2Corg_homepage%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics";


    public MemberEncodeUrlGenerator(){
        readFile();
        initWriter();
        initThreadPool();
    }


    private void initWriter(){
        try{
            cursorWriter = new PrintWriter(new FileWriter(CURSOR_NAME,false));
            urlWriter = new PrintWriter(new FileWriter(FILE_PATH,true));
        }catch (IOException e){
            logger.error( " init writer fail ;{}", e);
        }
    }

    private void initThreadPool(){
        if (threadPool == null || threadPool.isShutdown()){
            threadPool = Executors.newScheduledThreadPool(1);
            threadPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    flush();
                }
            },10,10, TimeUnit.SECONDS);
        }
    }

    private void flush(){
        cursorWriter.flush();
        urlWriter.flush();
    }

    private void readFile(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(CURSOR_NAME));
            String s;
            while ((s = reader.readLine()) != null) {
                cursor.addAndGet(NumberUtils.toInt(s));
            }
        }catch (IOException e){
            logger.error(" read cursor fail: {}",e);
        }finally {
            IOUtils.closeQuietly(reader);
        }
    }
    @Override
    public List<String> process(File file){
        String data = SegmentReader.readFolowee(file);

        if (!StringUtils.isEmpty(data) && inneer.incrementAndGet() > cursor.get() ){
            Json json = new Json(data);
            List<String> tokens = json.jsonPath("$.data[*].url_token").all();
            for (String s : tokens){
                urlWriter.println(String.format(URL_TEMPLATE,s) + QUERY_PARAMS);
                cursorWriter.println(cursor.incrementAndGet());
            }
        }

        return null;
    }

    public Set<String> extractUrlToken(String path){
        BaseAssembler.create(new FileRawInput(path),this)
                .thread(20)
                .run();

        return encode_token;
    }

    public Set<String> getUrlTokens(){
        return getUrlTokens(FILE_PATH);
    }

    public void save(){
        save(FILE_PATH);
    }

    private void save(String file){
        PrintWriter reader = null;
        try{
            reader = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
            for (String url : encode_token){
                reader.println(url);
            }
            reader.close();
        }catch (IOException e){
            logger.error(" write file fail {}",e);
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

    public void close() throws IOException{
        threadPool.shutdown();
        urlWriter.close();
        cursorWriter.close();
    }

    public static void main(String[] args){
        ZhihuConfiguration configuration = new ZhihuConfiguration();
        MemberEncodeUrlGenerator generator = new MemberEncodeUrlGenerator();
        generator.extractUrlToken(configuration.getFolloweeDataPath());
//        generator.save();
        try{
            generator.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
