package com.madao.disk.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
    SUCCESS((byte)0, "成功"),
    FAIL((byte)1, "失败"),
    ERROR((byte)2, "错误")

    ;
    private Byte code;
    private String message;

    ResultEnum(Byte code, String message){
        this.code = code;
        this.message = message;
    }
}
