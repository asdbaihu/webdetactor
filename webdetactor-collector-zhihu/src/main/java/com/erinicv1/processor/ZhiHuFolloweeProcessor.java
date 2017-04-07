package com.erinicv1.processor;

import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.download.FixedFileCacheQueueScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.selector.Json;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/7 0007.
 */
public class ZhiHuFolloweeProcessor implements PageProcessor {

    private static Logger logger = LoggerFactory.getLogger(ZhiHuFolloweeProcessor.class);

    private static String URL_TEMPLATE = "https://www.zhihu.com/api/v4/members/%s/followees";

    private static String QUERY_PARAMS = "?include=data%5B*%5D.url_token&offset=0&per_page=30&limit=30";

     private Site site = new ZhihuConfiguration().getSite();



    @Override
    public void process(Page page){
        Json json = page.getJson();
        page.putField("seg",json);

        String isEnd = json.jsonPath("$paging.is_end").get();
        if (!Boolean.parseBoolean(isEnd)){
            page.addTargetRequest(json.jsonPath("$paging.next").get());
        }
        List<String> tokenUrls = json.jsonPath("$data[*].url_token").all();
        List<String> urls = generateMemberUrls(tokenUrls);
        page.addTargetRequests(urls);
    }

    @Override
    public Site getSite(){
        return site;
    }

    private static String generateMemberUrl(String tokenUrl){
        String encode = null;
        try{
            encode = URLEncoder.encode(tokenUrl,"UTF-8").replace("+","20%");
        }catch (Exception e){
            logger.error(" url encoding error",e);
        }

        return String.format(URL_TEMPLATE , encode) + QUERY_PARAMS;
    }

    private static List<String> generateMemberUrls(List<String> tokenUrls){
        List<String> urls = new ArrayList<>();
        String url;
        for (String tokenUrl : tokenUrls){
            url = generateMemberUrl(tokenUrl);
            urls.add(url);
        }

        return urls;
    }

    public static void main(String[] args){
        String path = new ZhihuConfiguration().getFolloweePath();
        int crawSize = 1000000;
        Spider.create(new ZhiHuFolloweeProcessor())
                .setScheduler(new FixedFileCacheQueueScheduler(path)
                .setDuplicateRemover(new BloomFilterDuplicateRemover(crawSize)))
                .addPipeline(new FilePipeline(path))
                .addUrl(generateMemberUrl("hydro-ding"))
                .thread(20)
                .run();
    }
}
