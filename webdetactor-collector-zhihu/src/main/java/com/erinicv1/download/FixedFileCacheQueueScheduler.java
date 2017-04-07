package com.erinicv1.download;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.DuplicateRemovedScheduler;
import us.codecraft.webmagic.scheduler.MonitorableScheduler;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/4/7 0007.
 */
public class FixedFileCacheQueueScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler,Closeable{

    private String filePath = System.getProperty("java.io.mdir");

    private Task task;

    private PrintWriter urlsWriter;

    private PrintWriter cursorsWriter;

    private final static String URL_PATH = ".urls.txt";

    private final static String CURSOR_PATH = ".cursor.txt";

    private ScheduledExecutorService threadPool;

    private Set<String> ulrs;

    private BlockingQueue<Request> requests;

    private AtomicInteger cursor = new AtomicInteger();

    private AtomicBoolean inited = new AtomicBoolean(false);

    public FixedFileCacheQueueScheduler(String path){
        if (!path.endsWith("/") && !path.endsWith("\\")){
            path += "/";
        }
        this.filePath = path;
        initDuplicateRemover();
    }

    public void init(Task task){
        this.task = task;
        File file = new File(filePath);
        if (!file.exists()){
            file.mkdir();
        }
        readFile();
        initWriter();
        initThreadPool();
        inited.set(true);
        logger.info(" file cache start success");
    }

    public void flush(){
        urlsWriter.flush();
        cursorsWriter.flush();
    }
    private void initWriter(){
        try{
            urlsWriter = new PrintWriter(new FileWriter(getFileName(URL_PATH),true));
            cursorsWriter = new PrintWriter(new FileWriter(getFileName(CURSOR_PATH),false));
        }catch (IOException e){
            throw new RuntimeException(" init cache scheduler fail: ", e);
        }
    }

    private void initDuplicateRemover(){
        setDuplicateRemover(new DuplicateRemover() {
            @Override
            public boolean isDuplicate(Request request, Task task) {
               if (!inited.get()){
                   init(task);
               }
               return !ulrs.add(request.getUrl());
            }

            @Override
            public void resetDuplicateCheck(Task task) {
                ulrs.clear();
            }

            @Override
            public int getTotalRequestsCount(Task task) {
                return ulrs.size();
            }
        });
    }
    private void readFile(){
        try{
            requests = new LinkedBlockingQueue<>();
            ulrs = new LinkedHashSet<>();
            readCursorFile();
            readUrlFile();
        }catch (FileNotFoundException e){

            logger.info(" not fund file ", getFileName(URL_PATH));
        }catch (IOException e){
            logger.error(" init file error ", e);
        }
    }

    private void initThreadPool(){
        threadPool = Executors.newScheduledThreadPool(1);
        threadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        },10,10,TimeUnit.SECONDS);
    }
    private void readUrlFile() throws IOException{
        String url;
        BufferedReader reader = null;
        try{
            int lineReaded = 0;
            reader = new BufferedReader(new FileReader(getFileName(URL_PATH)));
            while ((url = reader.readLine()) != null){
                ulrs.add(url.trim());
                lineReaded++;
                if (lineReaded > cursor.get()){
                    requests.add(new Request(url));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (reader != null){
                IOUtils.closeQuietly(reader);
            }
        }
    }

    private void readCursorFile() throws IOException{
        BufferedReader reader = null;
        try{
            String line;
            reader = new BufferedReader(new FileReader(getFileName(CURSOR_PATH)));
            while ((line = reader.readLine()) != null){
                cursor = new AtomicInteger(NumberUtils.toInt(line));
            }
        }finally {
            if (reader != null){
                IOUtils.closeQuietly(reader);
            }
        }
    }

    public void close() throws IOException{
        threadPool.shutdown();
        urlsWriter.close();
        cursorsWriter.close();
    }

    @Override
    public void pushWhenNoDuplicate(Request request, Task task){
        if (!inited.get()){
            init(task);
        }
        requests.add(request);
        urlsWriter.println(request.getUrl());
    }

    public synchronized Request poll(Task task){
        if (!inited.get()){
            init(task);
        }
        cursorsWriter.println(cursor.incrementAndGet());
        return requests.poll();
    }

    public String getFileName(String fileName){
        return filePath + task.getUUID()+ fileName;
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
