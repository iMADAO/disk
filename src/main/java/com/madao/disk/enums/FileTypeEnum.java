package com.madao.disk.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {
    FILE((byte)0, "文件"),
    DIRECTORY((byte)1, "目录")

    ;
    private Byte code;
    private String message;

    FileTypeEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }
}
