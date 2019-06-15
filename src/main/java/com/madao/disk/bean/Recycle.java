package com.madao.disk.bean;

public class Recycle {
    private Long recycleId;

    private Long userId;

    private String userName;

    private String fileName;

    private String originPath;

    private String filePath;

    private Long originCreateTime;

    private Byte fileType;

    private String fileSize;

    public Long getRecycleId() {
        return recycleId;
    }

    public void setRecycleId(Long recycleId) {
        this.recycleId = recycleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath == null ? null : originPath.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath == null ? null : filePath.trim();
    }

    public Long getOriginCreateTime() {
        return originCreateTime;
    }

    public void setOriginCreateTime(Long originCreateTime) {
        this.originCreateTime = originCreateTime;
    }

    public Byte getFileType() {
        return fileType;
    }

    public void setFileType(Byte fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize == null ? null : fileSize.trim();
    }
}