package com.madao.disk.service;

import com.madao.disk.bean.FileNode;
import com.madao.disk.bean.Recycle;
import com.madao.disk.enums.FileTypeEnum;
import com.madao.disk.exception.ResultException;
import com.madao.disk.mapper.RecycleMapper;
import com.madao.disk.util.FileUtil;
import com.madao.disk.util.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {
    private static final String BASE_DIR = "/datadir";

    private static final String TEMP_DIR = "/datadir/temp";

    private static final String TRASH_DIR = "/datadir/trash";

    static{
        Path[] paths = new Path[3];
        paths[0] = Paths.get(BASE_DIR);
        paths[1] = Paths.get(TEMP_DIR);
        paths[2] = Paths.get(TRASH_DIR);

        try {
            for(Path path: paths) {
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RecycleMapper recycleMapper;

    public List<String> uploadDir(Long userId, String location, MultipartFile[] multipartFiles) throws IOException {
        System.out.println(location);
        System.out.println(multipartFiles.length);
        Arrays.stream(multipartFiles).forEach(x->{
            System.out.println(x.getOriginalFilename());
        });

        String uploadDirName = Paths.get(multipartFiles[0].getOriginalFilename()).toString();
        System.out.println(uploadDirName);

        Path basePath = Paths.get(BASE_DIR, userId.toString(), location);
        System.out.println("basePath: " + basePath);
        if(Files.notExists(basePath)){
            Files.createDirectory(basePath);
        }
        List<String> lineOfOutput = new ArrayList<>();
        for (MultipartFile multipartFile: multipartFiles) {
            File target = new File(basePath.toString(), multipartFile.getOriginalFilename());
            target.getParentFile().mkdirs();
            FileCopyUtils.copy(multipartFile.getBytes(),target);
            lineOfOutput.add(target.getPath());
        }
        return lineOfOutput;
    }

    public List<FileNode> getUserHomeFileList(Long userId) throws IOException {
        return getFileNodeList(userId, "");
    }

    public List<FileNode> getFileNodeList(Long userId, String location) throws IOException {
        Path targetPath = Paths.get(BASE_DIR, userId.toString(), location);
        System.out.println("targetPath......" + targetPath);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        if(Files.notExists(targetPath))
            return null;
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(targetPath);
        List<FileNode> fileNodeList = new ArrayList<>();
        for(Path path: directoryStream){
            File file = path.toFile();
            FileNode fileNode = new FileNode();
            fileNode.setFileName(file.getName());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
            fileNode.setUpdateTime(dateTimeFormatter.format(localDateTime));
            fileNode.setFilePath(path.toString());
            fileNode.setFileSize(FileUtil.getFileSize(path));
            fileNode.setFileType(file.isFile() ? FileTypeEnum.FILE.getCode() : FileTypeEnum.DIRECTORY.getCode());
            fileNodeList.add(fileNode);
        }
        return fileNodeList;
    }

    //创建目录
    public FileNode createDirectory(Long userId, String location, String directoryName) throws IOException {
        Path path = Paths.get(BASE_DIR, userId.toString(), location);
        if(Files.notExists(path)){
            throw new ResultException("父目录不存在");
        }
        Path targetPath = Paths.get(path.toString(), directoryName);
        if(Files.exists(targetPath)){
            throw new ResultException("目录已存在");
        }
        Files.createDirectory(targetPath);
        FileNode fileNode = new FileNode();
        fileNode.setFileName(targetPath.getFileName().toString());
        fileNode.setFileType(FileTypeEnum.DIRECTORY.getCode());
        fileNode.setFilePath(targetPath.toString());
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        fileNode.setUpdateTime(dateTimeFormatter.format(localDateTime));
        return fileNode;
    }

    //返回压缩后的目录文件路径或者文件路径
    public String checkDownloadFile(Long userId, String location, String fileName) throws IOException {
        Path targetPath = Paths.get(BASE_DIR, userId.toString(), location, fileName);
        System.out.println("targetPath........." + targetPath);
        return checkDownloadFile(targetPath.toString(), userId);
    }

    public String checkDownloadFile(String path) throws IOException {
        Path targetPath = Paths.get(path);
        String userId = path.split("/")[2];
        System.out.println("userId....." + userId);
        return checkDownloadFile(path, Long.parseLong(userId));
    }

    public String checkDownloadFile(String path, Long userId) throws IOException {
        Path targetPath = Paths.get(path);
        String fileName = targetPath.getFileName().toString();
        if(Files.notExists(targetPath)){
            throw new ResultException("该文件不存在");
        }
        //如果是目录
        if(Files.isDirectory(targetPath)){
            //检查并创建临时目录
            Path tempPath = Paths.get(TEMP_DIR);
            if(Files.notExists(tempPath)){
                Files.createDirectory(tempPath);
            }

            Path userTempPath = Paths.get(TEMP_DIR, userId.toString());
            if(Files.notExists(userTempPath)){
                Files.createDirectory(userTempPath);
            }
            Path newTargetPath = Paths.get(TEMP_DIR, userId.toString(), fileName + ".zip");
            //压缩目标目录到临时目录中
            zipMultiFile(targetPath, newTargetPath, true);
            targetPath = newTargetPath;
        }

        return targetPath.toString();
    }

    //压缩文件
    public static void zipMultiFile(Path filepath ,Path zippath, boolean dirFlag) {
        try {
            File file = filepath.toFile();// 要被压缩的文件夹
            File zipFile = zippath.toFile();
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File fileSec:files){
                    if(dirFlag){
                        recursionZip(zipOut, fileSec, file.getName() + File.separator);
                    }else{
                        recursionZip(zipOut, fileSec, "");
                    }
                }
            }
            zipOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void recursionZip(ZipOutputStream zipOut, File file, String baseDir) throws Exception{
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File fileSec:files){
                recursionZip(zipOut, fileSec, baseDir + file.getName() + File.separator);
            }
        }else{
            byte[] buf = new byte[1024];
            InputStream input = new FileInputStream(file);
            zipOut.putNextEntry(new ZipEntry(baseDir + file.getName()));
            int len;
            while((len = input.read(buf)) != -1){
                zipOut.write(buf, 0, len);
            }
            input.close();
        }
    }

    public void moveToTrash(Long userId, String location, String fileName) throws IOException {
        Path filePath = Paths.get(BASE_DIR, userId.toString(), location, fileName);
        System.out.println(filePath);
        if(Files.notExists(filePath)){
            throw new ResultException("该文件不存在");
        }
        Path trashPath = Paths.get(TRASH_DIR);
        if(Files.notExists(trashPath)){
            Files.createDirectory(trashPath);
        }

        Path userTrashPath = Paths.get(TRASH_DIR, userId.toString());
        if(Files.notExists(userTrashPath)){
            Files.createDirectory(userTrashPath);
        }

        Path targetPath = Paths.get(userTrashPath.toString(), fileName);
        if(Files.exists(targetPath)){
            targetPath = Paths.get(userTrashPath.toString(), fileName + Math.random()*100);
        }

        //保存回收站记录
        Recycle recycle = new Recycle();
        recycle.setRecycleId(KeyUtil.genUniquKeyOnLong());
        recycle.setFileName(fileName);
        recycle.setFilePath(targetPath.toString());
        recycle.setOriginPath(filePath.toString());
        recycle.setUserId(userId);
        recycle.setOriginCreateTime(filePath.toFile().lastModified());
        if(Files.isDirectory(filePath)){
            recycle.setFileType(FileTypeEnum.DIRECTORY.getCode());
        }else{
            recycle.setFileType(FileTypeEnum.FILE.getCode());
            String fileSize = FileUtil.getFileSize(filePath);
            recycle.setFileSize(fileSize);
        }
        recycleMapper.insertSelective(recycle);


        Files.move(filePath, targetPath);
    }


    public void moveListToTrash(Long userId, String location, String[] fileNameList) throws IOException {
        for(String fileName: fileNameList){
            if(fileName.trim()!="") {
                moveToTrash(userId, location, fileName);
            }
        }
    }
}
