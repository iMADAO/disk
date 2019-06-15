package com.madao.disk.controller;


import com.madao.disk.bean.FileNode;
import com.madao.disk.bean.User;
import com.madao.disk.dto.ShareDTO;
import com.madao.disk.exception.ResultException;
import com.madao.disk.service.ShareService;
import com.madao.disk.util.ResultUtil;
import com.madao.disk.util.ResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ShareController {
    @Autowired
    private ShareService shareService;

    @RequestMapping(value = "/toShare")
    public String toShare(HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            List<ShareDTO> shareDTOList = shareService.getShareListByUser(user.getUserId());
            request.setAttribute("shareList", shareDTOList);
        }catch (ResultException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "share";
    }

    @ResponseBody
    @RequestMapping(value="/share/add")
    public ResultView addShare(@RequestParam("location")String location, @RequestParam("fileName") String fileName, HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            String link = shareService.addShare(user.getUserId(), user.getUserName(), location, fileName);
            return ResultUtil.returnSuccess(link);
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @RequestMapping(value = "/share/get/{linkCode}")
    public String getShare(@PathVariable("linkCode") String linkCode, HttpServletRequest request){
        try {
            ShareDTO shareDTO = shareService.getShareFile(linkCode);
            request.setAttribute("shareDTO", shareDTO);
        }catch (ResultException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }
        return "shareDetail";
    }


    private User checkUserLogin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        if(user==null)
            throw new ResultException("用户未登录");
        return user;
    }
}
