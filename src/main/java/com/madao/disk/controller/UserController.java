package com.madao.disk.controller;

import com.madao.disk.bean.FileUploadResult;
import com.madao.disk.bean.User;
import com.madao.disk.exception.ResultException;
import com.madao.disk.service.UserService;
import com.madao.disk.util.KeyUtil;
import com.madao.disk.util.ResultUtil;
import com.madao.disk.util.ResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${PIC_PREFIX}")
    private String PICPATH_PREFIX;

    @Value("${PICLOCAL_PATH}")
    private String PICLOCAL_PATH;

    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/toRegister")
    public String toRegister(){
        return "register";
    }

    @ResponseBody
    @PostMapping("/user/login")
    public ResultView login(@RequestParam("userName") String userName, @RequestParam("password") String password, HttpServletRequest request){
        try {
            User user = userService.checkUserLogin(userName, password);
            request.getSession().setAttribute("user", user);
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            return ResultUtil.returnFail();
        }
    }


    @ResponseBody
    @PostMapping("/user/register")
    public ResultView login(@RequestParam("userName") String userName, @RequestParam("password") String password, @RequestParam("email")String email){
        try {
            userService.addUser(userName, password, email);
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            return ResultUtil.returnFail();
        }
    }

    //上传图片
    @ResponseBody
    @RequestMapping("/pic/upload")
    public ResultView upload(@RequestParam("file") MultipartFile imageFile, @RequestParam(value = "num", required = false) Integer num) {
        if (imageFile.isEmpty()) {
            return ResultUtil.returnFail("未选择图片");
        }
        String filename = imageFile.getOriginalFilename();

        String ext= null;
        if(filename.contains(".")){
            ext = filename.substring(filename.lastIndexOf("."));
        }else{
            ext = "";
        }

        String nfileName = KeyUtil.genUniquKeyOnLong() + ext;
        Path picPath = Paths.get(PICLOCAL_PATH);
        if(Files.notExists(picPath)){
            try {
                System.out.println("creating path........." + picPath);
                Files.createDirectory(picPath);
            } catch (IOException e) {
                e.printStackTrace();
                return ResultUtil.returnFail("请稍后重试");
            }
        }
        File targetFile = Paths.get(PICLOCAL_PATH, nfileName).toFile();
        try {
            imageFile.transferTo(targetFile);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            return ResultUtil.returnFail("请稍后重试");
        }

        String accessUrl =  PICPATH_PREFIX + "/" + nfileName;

        FileUploadResult result = new FileUploadResult();
        result.setImgPath(accessUrl);
        result.setNum(num);
        result.setImgName(nfileName);
        return ResultUtil.returnSuccess(result);
    }

    @GetMapping("/checkUserName/{userName}")
    @ResponseBody
    public ResultView testUserName(@PathVariable(value="userName") String userName){
        System.out.println(userName);
        try {
            Boolean result = userService.checkIfUserNameExist(userName);
            return ResultUtil.returnSuccess(result);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @ResponseBody
    @RequestMapping("/user/change/password")
    public ResultView changePassword(@RequestParam("password") String password, @RequestParam("newPassword")String newPassword, HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            userService.changeUserPassword(user.getUserId(), password, newPassword);
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @PostMapping("/user/userName/change")
    @ResponseBody
    public ResultView changeUserName(@RequestParam("userName") String userName, HttpServletRequest request){
        try {
            User user = checkUserLogin(request);
            Long userId = user.getUserId();
            userService.changeUserName(userId, userName);
            //更新session中的用户信息
            user = userService.getUserById(userId);
            if (user != null) {
                request.getSession().setAttribute("user", user);
            }
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
    @PostMapping("/user/pic/change")
    public ResultView changeUserPic(@RequestParam("picPath") String picPath, HttpServletRequest request){
        System.out.println("picPath...." + picPath);
        try {
            User user = checkUserLogin(request);
            Long userId = user.getUserId();
            userService.changeUserPic(userId, picPath);
            User newUser = userService.getUserById(userId);
            if(newUser!=null){
                request.getSession().setAttribute("user", newUser);
            }
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
    @RequestMapping("/user/logout")
    public ResultView logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return ResultUtil.returnSuccess();
    }

    @RequestMapping("/toHomePage")
    public String toHomePage(){
        return "homePage";
    }

    private User checkUserLogin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        if(user==null)
            throw new ResultException("用户未登录");
        return user;
    }
}
