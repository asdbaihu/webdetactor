package com.erinicv1.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class DemoDataProcessor implements DataProcessor<File,String> {

    private static Logger logger = LoggerFactory.getLogger(DemoDataProcessor.class);

    @Override
    public List<String> process(File initem){
        List<String> outitem = new ArrayList<>();

        try{
            BufferedReader br = new BufferedReader(new FileReader(initem));
            String s;
            br.readLine();
            s = br.readLine();
            if (s != null){
                s = s.substring(s.indexOf("{"));
                Json json = new Json(s);
                outitem = json.jsonPath("$.data[*].[*]").all();

            }
            br.close();

        }catch (IOException e){
            e.printStackTrace();
        }
        return outitem;
    }
}
