package com.madao.disk.bean;

import com.sun.javafx.beans.IDProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
public class FileNode {
    @Id
    private String fileId;

    private String fileName;

    private String filePath;

    private String hdfsFilePath;

    private Byte fileType;

    private String fileSize;

    private String updateTime;

    private List<FileNode> childNode;
}