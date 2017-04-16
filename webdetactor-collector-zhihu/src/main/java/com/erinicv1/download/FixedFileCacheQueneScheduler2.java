package com.erinicv1.download;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/4/13 0013.
 */
public class FixedFileCacheQueneScheduler2 extends DuplicateRemovedScheduler implements MonitorableScheduler,Closeable {

    private PrintWriter cursorWriter;

    private PrintWriter urlWriter;

    private String base_dir = System.getProperty("java.io.mdir");

    private ScheduledExecutorService threadPool;

    private BlockingQueue<Request> requests ;

    private Set<String> urls;

    private AtomicInteger cursor = new AtomicInteger(0);

    private AtomicBoolean inited = new AtomicBoolean(false);

    private AtomicInteger pointer = new AtomicInteger(500);

    private Task task;

    private final static String URL_FILE = ".urls.txt";

    private final static String CUR_FILE = ".cursor.txt";

    private BufferedReader readerUrl;

    private final static String URL_TEMPLATE = "https://www.zhihu.com/api/v4/members/";

    private final static String QUERY_PARAMS = "?include=locations%2Cemployments%2Cgender%2Ceducations%2Cbusiness%2Cvoteup_count%2Cthanked_Count%2Cfollower_count%2Cfollowing_count%2Ccover_url%2Cfollowing_topic_count%2Cfollowing_question_count%2Cfollowing_favlists_count%2Cfollowing_columns_count%2Canswer_count%2Carticles_count%2Cpins_count%2Cquestion_count%2Cfavorite_count%2Cfavorited_count%2Clogs_count%2Cmarked_answers_count%2Cmarked_answers_text%2Cmessage_thread_token%2Caccount_status%2Cis_active%2Cis_force_renamed%2Cis_bind_sina%2Csina_weibo_url%2Csina_weibo_name%2Cshow_sina_weibo%2Cis_blocking%2Cis_blocked%2Cmutual_followees_count%2Cvote_to_count%2Cvote_from_count%2Cthank_to_count%2Cthank_from_count%2Cthanked_count%2Cdescription%2Chosted_live_count%2Cparticipated_live_count%2Callow_message%2Cindustry_category%2Corg_name%2Corg_homepage%2Cbadge%5B%3F(type%3Dbest_answerer)%5D.topics";


    public FixedFileCacheQueneScheduler2(String path){
        if (!path.endsWith("/") && !path.endsWith("\\")){
            path += "/";
        }
        this.base_dir = path;
        initDuplicationRemover();
    }

    private void init(Task task){
        this.task = task;

        File file = new File(base_dir);
        if (!file.exists()){
            file.mkdir();
        }


        initWriter();
        readFile();
        initThreadPool();
        inited.set(true);
    }

    private void initWriter(){
        try{
            urlWriter = new PrintWriter(new FileWriter(getFileName(URL_FILE),true));
            cursorWriter = new PrintWriter(new FileWriter(getFileName(CUR_FILE),false));
            readerUrl = new BufferedReader(new FileReader("E:/TestMe/followee/urlEncode_token"));
        }catch (IOException e){
            logger.error(" init writer fail ;{}",e);
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
            },10,10,TimeUnit.SECONDS);
        }
    }

    private void readFile(){
        requests = new LinkedBlockingQueue<>();
        urls = new HashSet<>();
        try{
            readCursorFile();
            readUrlFile();

        }catch (FileNotFoundException e){
            logger.warn(" file not found " + getFileName(URL_FILE));
        }catch (IOException e){
            logger.error(" init reader fail:{} ", e);
        }
    }

    private void readCursorFile() throws IOException{
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(getFileName(CUR_FILE)));
            String s;
            while ((s = reader.readLine()) != null){
                cursor.addAndGet(NumberUtils.toInt(s));
            }
        }finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void readUrlFile(){

        try{
            String s;
            int  count = 0;
            while ((s = readerUrl.readLine()) != null){
                count++;
//                s = URL_TEMPLATE + s + QUERY_PARAMS;
                urls.add(s);
                if (count > cursor.get()){
//                    encode = URLEncoder.encode(s,"UTF-8").replace("+","20%");
                    requests.add(new Request(s));
                }
            }

        }catch (IOException e){
            logger.error(" read url fail:{}",e);
        }
    }


    private void flush(){
        cursorWriter.flush();
        urlWriter.flush();
    }
    private void initDuplicationRemover(){
        setDuplicateRemover(new DuplicateRemover() {
            @Override
            public boolean isDuplicate(Request request, Task task) {
                if (!inited.get()){
                    init(task);
                }
                return !urls.add(request.getUrl());
            }

            @Override
            public void resetDuplicateCheck(Task task) {
                urls.clear();
            }

            @Override
            public int getTotalRequestsCount(Task task) {
                return urls.size();
            }
        });
    }

    private String getFileName(String name){
        return base_dir + task.getUUID() + name;
    }

    public void close() throws IOException{
        threadPool.shutdown();
        urlWriter.close();
        cursorWriter.close();
        readerUrl.close();
    }

    @Override
    public void pushWhenNoDuplicate(Request request, Task task){
        if (!inited.get()){
            init(task);
        }
        requests.add(request);
//        cursor.decrementAndGet();
        urlWriter.println(request.getUrl());
    }

    public synchronized Request poll(Task task){
        if (!inited.get()){
            init(task);
        }
        cursorWriter.println(cursor.incrementAndGet());
//        if(pointer.decrementAndGet() < 100){
//            readUrlFile();
//        }
        return requests.poll();
    }


    @Override
    public int getLeftRequestsCount(Task var1){
        return requests.size();
    }

    @Override
    public int getTotalRequestsCount(Task var1){
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
}
