package com.erinicv1.utils;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class FileHepler {

    public static String getRaw(String path) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try{

            br = new BufferedReader(new FileReader(path));
            String temp;
            while ( (temp = br.readLine()) != null){
                sb.append(temp).append("\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (br != null){
                try{
                    br.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }


}
