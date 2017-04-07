package com.erinicv1.configure;

import com.erinicv1.utils.FileHepler;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public abstract class AbstractConfiguration {

    private final static String FIlE_PATH = "config.json";
    private final static String FILE_LOCATION = AbstractConfiguration.class.getResource("/").getPath() + "/";

    protected String config;

    public AbstractConfiguration(){
        this(FILE_LOCATION + "/" + FIlE_PATH);
    }
    public AbstractConfiguration(String path){
        config = FileHepler.getRaw(path);
        resolve();
    }
    protected abstract void resolve();

    public String getConfig(){
        return config;
    }
}
