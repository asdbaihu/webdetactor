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
                urls.add(s);
                if (count > cursor.get()){
                    requests.add(new Request(s));
                }
                if (count > 2000){
                    break;
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
        urlWriter.println(request.getUrl());
    }

    public synchronized Request poll(Task task){
        if (!inited.get()){
            init(task);
        }
        cursorWriter.println(cursor.incrementAndGet());
        if(pointer.decrementAndGet() < 200){
            readUrlFile();
        }
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
