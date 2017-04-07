package com.erinicv1;

import com.alibaba.fastjson.JSONObject;
import com.erinicv1.configure.AbstractConfiguration;
import us.codecraft.webmagic.Site;

import java.io.File;

/**
 * Created by Administrator on 2017/4/7 0007.
 */
public class ZhihuConfiguration extends AbstractConfiguration {

    private Site site;

    private String baseDir;

    public ZhihuConfiguration(){

    }

    public ZhihuConfiguration(String path){
        super(path);
    }

    public  void resolve(){
        JSONObject jsonObject = JSONObject.parseObject(config);
        site = JSONObject.parseObject(jsonObject.getString("site"),Site.class);
        checkAndMakeDir(jsonObject.getString("base_dir"));
    }

    public void checkAndMakeDir(String directiona){
        baseDir = directiona;
        if (!baseDir.endsWith("/")){
            baseDir += "/";
        }
        File file = new File(baseDir);
        if (!file.exists()){
            file.mkdir();
        }
    }

    public String getBaseDir(){
        return baseDir;
    }

    public String getFoloweePath(){
        return baseDir + "folowee/";
    }

    public Site getSite() {
        return site;
    }

    public String getMemberPath() {
        return getBaseDir() + "member/";
    }

    public String getFolloweePath() {
        return getBaseDir() + "followee/";
    }

    public String getMemberDataPath() {
        return getMemberPath() + site.getDomain() + "/";
    }

    public String getFolloweeDataPath() {
        return getFolloweePath() + site.getDomain() + "/";
    }

    public static void main(String[] args) {
        ZhihuConfiguration configuration = new ZhihuConfiguration();
        System.out.println(configuration.getSite());
        System.out.println(configuration.getBaseDir());
        System.out.println(configuration.getFolloweePath());
    }
}
