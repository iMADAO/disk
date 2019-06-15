package com.madao.disk.util;
import lombok.Data;

@Data
public class ResultView<T> {
    private Byte code;
    private String hint;
    private T data;
}
