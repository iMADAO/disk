package com.madao.disk.exception;

import com.madao.disk.enums.ResultEnum;
import lombok.Data;

@Data
public class ResultException extends RuntimeException{
    private int code;
    private Object id;

    public ResultException(int code, String mess){
        super(mess);
        this.code = code;
    }

    public ResultException(int code, String mess, Object id){
        super(mess);
        this.code = code;
        this.id = id;
    }

    public ResultException(String message){
        super(message);
        this.code = ResultEnum.FAIL.getCode();
    }
}
