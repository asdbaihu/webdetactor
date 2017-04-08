package com.erinicv1;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class SegmentReader {

    public static String readFolowee(File file){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
            br.readLine();
            String s = br.readLine();
            if (!StringUtils.isEmpty(s)){
                s = s.substring(s.indexOf("{"));
            }
            br.close();
            return s;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


    public static String readMember(File file){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
            br.readLine();
            String s = br.readLine();
            br.close();
            return s;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
