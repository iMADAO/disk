package com.madao.disk.dto;

import lombok.Data;

@Data
public class RecycleDTO {
    private Long recycleId;

    private String fileName;

    private String originCreateTime;

    private Byte fileType;

    private String fileSize;
}
