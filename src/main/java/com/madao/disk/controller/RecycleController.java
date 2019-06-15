package com.madao.disk.controller;

import com.madao.disk.bean.User;
import com.madao.disk.dto.RecycleDTO;
import com.madao.disk.exception.ResultException;
import com.madao.disk.service.RecycleService;
import com.madao.disk.util.ResultUtil;
import com.madao.disk.util.ResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class RecycleController {

    @Autowired
    private RecycleService recycleService;

    @ResponseBody
    @DeleteMapping("/recycle/{recycleId}")
    public ResultView deleteRecycle(@PathVariable("recycleId") Long recycleId, HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            recycleService.deleteRecycle(recycleId, user.getUserId());
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @ResponseBody
    @DeleteMapping("/recycle/list")
    public ResultView deleteRecycleList(@RequestParam("recycleIdList") String recycleIdListStr, HttpServletRequest request){
        System.out.println(recycleIdListStr);
        String[] recycleIdList = recycleIdListStr.split(";");
        try {
            User user = checkUserLogin(request);
            recycleService.deleteRecycleList(recycleIdList, user.getUserId());
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }


    @ResponseBody
    @PostMapping("/recycle/recover")
    public ResultView recoverRecycle(@RequestParam("recycleId") Long recycleId, HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            recycleService.recoverRecycle(recycleId, user.getUserId());
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }

    }

    @ResponseBody
    @PostMapping("/recycle/recover/list")
    public ResultView recoverRecycleList(@RequestParam("recycleIdList") String recycleIdListStr, HttpServletRequest request){
        try {
            String[] recycleIdList = recycleIdListStr.split(";");
            User user = checkUserLogin(request);
            recycleService.recoverRecycleList(recycleIdList, user.getUserId());
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }

    }

    @RequestMapping("/toRecycle")
    public String toRecycle(@RequestParam(value = "location", defaultValue = "") String location, HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        if(user!=null){
            try {
                Long userId = user.getUserId();
                List<RecycleDTO> recycleList = null;
                if(location.equals("")){
                    recycleList = recycleService.getRecycleFileList(userId);
                }else{
                    recycleList = recycleService.getRecycleFileListInLocation(userId, location);
                }
                recycleList.stream().forEach(System.out::println);
                request.setAttribute("recycleList", recycleList);
                request.setAttribute("location", location);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "recycle";
    }

    private User checkUserLogin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        if(user==null)
            throw new ResultException("用户未登录");
        return user;
    }
}
