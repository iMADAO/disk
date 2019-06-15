package com.madao.disk.controller;

import com.madao.disk.bean.FileNode;
import com.madao.disk.bean.User;
import com.madao.disk.exception.ResultException;
import com.madao.disk.service.FileService;
import com.madao.disk.service.FileService2;
import com.madao.disk.service.ShareService;
import com.madao.disk.util.ResultUtil;
import com.madao.disk.util.ResultView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private ShareService shareService;


    @ResponseBody
    @RequestMapping("/uploadDir")
    public ResultView multiFileUploadingAbleSorting(@RequestParam(value="location", defaultValue="") String location, @RequestParam("multipartFiles") MultipartFile[] multipartFiles, HttpServletRequest request)
            throws IOException {
        System.out.println("location....." + location);
        try {
            User user = checkUserLogin(request);
            List<String> result = fileService.uploadDir(user.getUserId(), location, multipartFiles);
            return ResultUtil.returnSuccess(result);
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @ResponseBody
    @RequestMapping("/createDirectory")
    public ResultView createDirectory(@RequestParam(value="location", defaultValue = "") String location, @RequestParam("directoryName") String directoryName, HttpServletRequest request){
        try{
            User user = checkUserLogin(request);
            FileNode fileNode = fileService.createDirectory(user.getUserId(), location, directoryName);
            return ResultUtil.returnSuccess(fileNode);
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }


    @RequestMapping("/download")
    public void download(@RequestParam(value = "location", defaultValue = "") String location, @RequestParam("fileName") String fileName, HttpServletRequest request, HttpServletResponse response){
        try{
            User user = checkUserLogin(request);
            String filePath = fileService.checkDownloadFile(user.getUserId(), location, fileName);

            System.out.println(filePath);

            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(new File(filePath));
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                System.out.println("Download successfully!");
            }
            catch (Exception e) {
                System.out.println("Download failed!");
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

//            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
//            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
//            return ResultUtil.returnFail();
        }
    }

    @RequestMapping("/download/share/{shareId}")
    public void downloadShare(@PathVariable("shareId")Long shareId, HttpServletRequest request, HttpServletResponse response){
        try{
            String filePath = shareService.getFilePathByShareId(shareId);
            filePath = fileService.checkDownloadFile(filePath);
            String fileName = Paths.get(filePath).getFileName().toString();
            System.out.println(filePath);
            if(filePath==null)
                return;

            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(new File(filePath));
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                System.out.println("Download successfully!");
            }
            catch (Exception e) {
                System.out.println("Download failed!");
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

//            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
//            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
//            return ResultUtil.returnFail();
        }
    }

    @ResponseBody
    @RequestMapping("/toTrash")
    public ResultView moveToTrash(@RequestParam(value = "/location", defaultValue = "") String location, @RequestParam("fileName")String fileName,  HttpServletRequest request){
        try{
            User user = checkUserLogin(request);
            fileService.moveToTrash(user.getUserId(), location, fileName);
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
    @RequestMapping("/toTrash/list")
    public ResultView moveListTrash(@RequestParam(value = "/location", defaultValue = "") String location, @RequestParam("fileNameList") String fileNameListStr,  HttpServletRequest request){
        System.out.println("fileNameStr::::" + fileNameListStr);
        try{
            String[] fileNameList = fileNameListStr.split(";");
            System.out.println(fileNameList);
            Arrays.stream(fileNameList).forEach(System.out::println);
            System.out.println("location.." + location);
            User user = checkUserLogin(request);
            fileService.moveListToTrash(user.getUserId(), location, fileNameList);
            return ResultUtil.returnSuccess();
        }catch (ResultException e){
            System.out.println(e.getMessage());
            return ResultUtil.returnFail(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.returnFail();
        }
    }

    @RequestMapping(value={"/index", "toIndex"})
    public String toIndex(@RequestParam(value = "location", defaultValue = "")String location,  HttpServletRequest request){
        System.out.println("location...." + location);
        User user = (User) request.getSession().getAttribute("user");
        if(user!=null){
            try {
                Long userId = user.getUserId();
                List<FileNode> fileList = null;
                if(location.equals("")){
                    fileList = fileService.getUserHomeFileList(userId);
                }else{
                    fileList = fileService.getFileNodeList(userId, location);
                }
                System.out.println(fileList.size());
                request.setAttribute("fileList", fileList);
                request.setAttribute("location", location);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "index";
    }

    private User checkUserLogin(HttpServletRequest request){
        User user = (User) request.getSession().getAttribute("user");
        if(user==null)
            throw new ResultException("用户未登录");
        return user;
    }

}
