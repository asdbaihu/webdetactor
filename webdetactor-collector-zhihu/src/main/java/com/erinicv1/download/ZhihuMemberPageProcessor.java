package com.erinicv1.download;

import com.erinicv1.ZhihuConfiguration;
import com.erinicv1.processor.MemberEncodeUrlGenerator;
import com.erinicv1.processor.MemberUrlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/10 0010.
 */
public class ZhihuMemberPageProcessor implements PageProcessor {

    private Site site = new ZhihuConfiguration().getSite();

    @Override
    public void process(Page page){
       page.putField(ZhihuMemberPipeline.URL,page.getUrl());
       page.putField(ZhihuMemberPipeline.RESPONSE,page.getRawText());
    }


    @Override
    public Site getSite(){
        return site;
    }

    public static void main(String[] args){
        ZhihuConfiguration configuration = new ZhihuConfiguration();
        String path = configuration.getMemberPath();

        Spider spider = Spider.create(new ZhihuMemberPageProcessor())
                .addPipeline(new ZhihuMemberPipeline(path))
                .thread(20);
        MemberEncodeUrlGenerator getter = new MemberEncodeUrlGenerator();
        for (String token : getter.getUrlTokens()) {
            spider.addUrl(token);
        }


        spider.run();
    }
}
