package com.madao.disk.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HDFSUtil {
    private String HDFS_PATH = "hdfs://localhost:8020";

    FileSystem fileSystem = null;
    Configuration configuration = null;

    public HDFSUtil() throws URISyntaxException, IOException, InterruptedException {
        configuration = new Configuration();
        System.out.println("HDFS_PATH..." + HDFS_PATH);
        System.out.println(new URI(HDFS_PATH));
        System.out.println(configuration);
        fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration, "root");


    }

    public void mkdir(String pathStr) throws IOException {
        fileSystem.mkdirs(new Path(pathStr));
    }

    public void copyFromlocalFile(String localPathStr, String hdfsPathStr) throws IOException {
        Path localPath = new Path(localPathStr);
        Path hdfsPath = new Path(hdfsPathStr);
        fileSystem.copyFromLocalFile(localPath, hdfsPath);
        fileSystem.close();
    }

    public void copyToLocalFile(String localPathStr, String hdfsPathStr) throws IOException {
        Path localPath = new Path(localPathStr);
        Path hdfsPath = new Path(hdfsPathStr);
        fileSystem.copyToLocalFile(hdfsPath, localPath);
    }

    public boolean checkIfExist(String pathStr) throws IOException {
        return fileSystem.exists(new Path(pathStr));
    }


}
