package com.erinicv1.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Created by Administrator on 2017/4/6 0006.
 */
public class FileRawInput extends RawInput<File> {

    private static Logger logger = LoggerFactory.getLogger(FileRawInput.class);

    public FileRawInput(String path){
        File file = new File(path);
        File[] listFile = file.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir,String name) {
                return name.endsWith(".html");
            }
        });
        if(listFile != null) {
            queue.addAll(Arrays.asList(listFile));
        }

    }

    public int getLeftCount(){
        return queue.size();
    }

    public File poll(){
        return queue.poll();
    }
}
