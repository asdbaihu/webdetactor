package com.erinicv1.configure;

import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Site;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class SiteConfiguration extends AbstractConfiguration {


    private Site site;

    public Site getSite(){
        return site;
    }

    public SiteConfiguration(){

    }

    public SiteConfiguration(String path){
        super(path);
    }

    public void resolve(){
        site = JSONObject.parseObject(config,Site.class);
    }


    public static void main(String[] args){
        SiteConfiguration s = new SiteConfiguration();
        Site site = s.getSite();
        System.out.print(site);
    }
}
