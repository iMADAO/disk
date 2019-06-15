package com.madao.disk.util;

import com.madao.disk.enums.ResultEnum;
import com.madao.disk.exception.ResultException;

/**
 * Date: 2018/1/9
 * Author: Richard
 */

public class ResultUtil {
    public static ResultView returnSuccess(){
        ResultView resultView = new ResultView();
        resultView.setCode(ResultEnum.SUCCESS.getCode());
        resultView.setHint(ResultEnum.SUCCESS.getMessage());
        return resultView;
    }

    public static ResultView returnSuccess(Object data){
        ResultView resultView = new ResultView();
        resultView.setCode(ResultEnum.SUCCESS.getCode());
        resultView.setHint(ResultEnum.SUCCESS.getMessage());
        resultView.setData(data);
        return resultView;
    }

    public static ResultView returnException(ResultException e){
        ResultView resultView = new ResultView();
        resultView.setCode((byte)e.getCode());
        resultView.setHint(e.getMessage());
        return resultView;
    }

    public static ResultView returnFail(String hint) {
        ResultView resultView = new ResultView();
        resultView.setCode(ResultEnum.FAIL.getCode());
        resultView.setHint(hint);
        return resultView;
    }

    public static ResultView returnFail() {
        ResultView resultView = new ResultView();
        resultView.setHint("请稍后重试");
        resultView.setCode(ResultEnum.ERROR.getCode());
        return resultView;
    }
}
