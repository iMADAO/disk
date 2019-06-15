package com.madao.disk.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileUtil {
    public static String[] sizeFlag = {"B", "K", "M", "G"};

    public static String getFileSize(Path path){
        try {
            double size = Files.size(path);
            int time = 0;
            for(int i=0; i<3; i++){
                if(size>1024) {
                    size /= 1024;
                    time++;
                }
                else
                    break;
            }
            DecimalFormat df = new DecimalFormat("#####0.00");
            String result = df.format(size);
            result += sizeFlag[time];
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getStringDate(Long timeMill){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm-ss");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMill), ZoneId.systemDefault());
        String dateString = dateTimeFormatter.format(localDateTime);
        return dateString;
    }

    public static String getStringDateNow() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm-ss");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        String dateString = dateTimeFormatter.format(localDateTime);
        return dateString;
    }
}
