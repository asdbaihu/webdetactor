package com.erinicv1.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.thread.CountableThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class BaseAssembler<IN,OUT> {

    protected int threadCount = 1;

    protected static Logger logger = LoggerFactory.getLogger(BaseAssembler.class);

    protected List<OutPipeline<OUT>> outPipelineList = new ArrayList<>();

    private DataProcessor<IN,OUT> dataProcessor;

    private AtomicLong inItemCount = new AtomicLong(0);

    private  AtomicLong outItemCount = new AtomicLong(0);

    protected CountableThreadPool countableThreadPool;

    protected RawInput<IN> rawInput;

    protected AtomicInteger stat = new AtomicInteger(START_INIT);

    protected final static int START_INIT = 0;

    protected final static int START_RUNNING = 1;

    protected final static int START_STOPED = 2;



    public static <IN,OUT> BaseAssembler<IN,OUT> create(RawInput<IN> rawInput,
                                                           DataProcessor<IN,OUT> dataProcessor){

        return new BaseAssembler<>(rawInput,dataProcessor);
    }


    public BaseAssembler(RawInput rawInput,DataProcessor dataProcessor){
        this.rawInput = rawInput;
        this.dataProcessor = dataProcessor;
    }

    public void initComponent(){
        if (rawInput == null){
            throw new RuntimeException("");
        }
        if (countableThreadPool == null || countableThreadPool.isShutdown()){
            countableThreadPool = new CountableThreadPool(threadCount);
        }
        if (outPipelineList.isEmpty()){
            outPipelineList.add(new ConsoleOutPipeline<OUT>());
        }

    }

    public void checkRunStat(){
        while (true){
            int statCount = stat.get();
            if (statCount == START_RUNNING){
                throw new IllegalStateException("Assembler is already running");
            }
            if (stat.compareAndSet(statCount,START_RUNNING)){
                break;
            }
        }
    }

    public void run(){
        long startTime = System.currentTimeMillis();

        checkRunStat();
        initComponent();
        while (!Thread.currentThread().isInterrupted() && stat.get() == START_RUNNING){
            final IN item = rawInput.poll();
            if (item == null){
                if (countableThreadPool.getThreadAlive() == 0){
                    break;
                }
            }else {
                countableThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processInItem(item);
                        }catch (Exception e){
                            logger.error("error " + item, e);
                        }
                        inItemCount.incrementAndGet();
                    }
                });
            }

        }
        stat.set(START_STOPED);
        long endTime = System.currentTimeMillis();
        logger.info("Process end. spent {} ms", (endTime - startTime));
        // release some resources
        close();

        endTime = System.currentTimeMillis();
        logger.info("Total time: {}", endTime - startTime);
        logger.info("Total outItemCount: {}", outItemCount);
    }

    protected void processInItem(IN inItem){
        List<OUT> outItem = dataProcessor.process(inItem);
        if (outItem == null || outItem.isEmpty()){
            return;
        }

        outItemCount.addAndGet(outItem.size());

        for (OutPipeline outPipeline : outPipelineList){
            outPipeline.process(outItem);
        }
    }

    protected void checkIfRunning(){
        if (stat.get() == START_RUNNING){
            throw new IllegalStateException("Assembler is already running");
        }
    }

    public void close(){
        closeEach(dataProcessor);

        for (OutPipeline outPipeline : outPipelineList){
            closeEach(outPipeline);
        }
    }

    public void closeEach(Object object){
        if (object instanceof AutoCloseable){
            try {
                ((AutoCloseable) object).close();
            }catch (Exception e){
                logger.warn("closeEach:{} ", e);
            }
        }
    }

    public BaseAssembler<IN,OUT> thread(int threadCount){
        this.threadCount = threadCount;
        return this;
    }


    public BaseAssembler<IN,OUT> setOutPipeline(List<OutPipeline<OUT>> outPipeline){
        checkIfRunning();
        this.outPipelineList = outPipeline;
        return this;
    }

    public BaseAssembler<IN,OUT> addPipeline(OutPipeline<OUT> outOutPipeline){
        checkIfRunning();
        outPipelineList.add(outOutPipeline);
        return this;
    }

    public static void main(String[] args){

        String path = "E:\\TestMe";

        OutPipeline<String> pipeline = new ConsoleOutPipeline<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                BaseAssembler.<File, String>create(new FileRawInput(path),
                        new DemoDataProcessor())
                        .addPipeline(pipeline)
                        .thread(10)
                        .run();
            }
        }).start();

    }
}
