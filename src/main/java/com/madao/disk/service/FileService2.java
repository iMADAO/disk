package com.madao.disk.service;

import com.madao.disk.bean.FileNode;
import com.madao.disk.bean.Recycle;
import com.madao.disk.enums.FileTypeEnum;
import com.madao.disk.exception.ResultException;
import com.madao.disk.repository.FileNodeRepository;
import com.madao.disk.util.FileUtil;
import com.madao.disk.util.HDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//使用mongoDB存储文件基本信息，HDFS和本地文件系统配合存储文件
@Service
public class FileService2 {
    @Autowired
    private FileNodeRepository fileNodeRepository;

    private static final String BASE_DIR = "/datadir";

    private static final String TEMP_DIR = "/datadir/temp";

    private static final String TRASH_DIR = "/datadir/trash";

    private static final String HDFS_DIR = "/disk";

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

    //上传目录
    public List<String> uploadDir(Long userId, String location, MultipartFile[] multipartFiles) throws IOException {
        System.out.println(location);
        System.out.println(multipartFiles.length);
        Arrays.stream(multipartFiles).forEach(x->{
            System.out.println(x.getOriginalFilename());
        });

        //上传的目录名
        String uploadDirName = Paths.get(multipartFiles[0].getOriginalFilename()).subpath(0, 1).toString();
        System.out.println(uploadDirName);

        //上传到本地
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

        //上传到HDFS
        Path localDirPath = Paths.get(basePath.toString(), uploadDirName);
        Path hdfsPath = Paths.get(HDFS_DIR, userId.toString(), uploadDirName);
        System.out.println(localDirPath + "---" + hdfsPath);
        try {
            HDFSUtil hdfsUtil = new HDFSUtil();
            hdfsUtil.copyFromlocalFile(localDirPath.toString(), hdfsPath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResultException("上传失败");
        }

        //存储本目录节点
        addRecursive(localDirPath.toString());

        //更新父目录节点
        String parentPathStr = Paths.get(userId.toString(), location, uploadDirName).toString();
        FileNode parentNode = fileNodeRepository.findByFilePath(parentPathStr);
        if(parentNode!=null){
            List<FileNode> childNodeList = parentNode.getChildNode();
            if(childNodeList==null){
                childNodeList = new ArrayList<>();
            }
            childNodeList.add(getFileNodeByPath(localDirPath));
         }
        fileNodeRepository.insert(parentNode);
        return lineOfOutput;
    }

    public List<FileNode> getUserHomeFileList(Long userId) throws IOException {
        FileNode fileNode = fileNodeRepository.findByFilePath("");
        if(fileNode==null)
            return new ArrayList<>(0);
        List<FileNode> fileNodeList = fileNode.getChildNode();
        return fileNodeList;
    }

    public List<FileNode> getFileNodeList(Long userId, String location){
        Path path = Paths.get(userId.toString(), location);
        List<FileNode> fileNodeList = fileNodeRepository.findByFilePath(path.toString()).getChildNode();
        return fileNodeList;
    }

    //创建目录
    public FileNode createDirectory(Long userId, String location, String directoryName) throws IOException, URISyntaxException, InterruptedException {
        Path localParentPath = Paths.get(BASE_DIR, userId.toString(), location);
        if(Files.notExists(localParentPath)){
            Path hdfsParentPath = Paths.get(HDFS_DIR, userId.toString(), location);
            boolean flag = checkAndLoadHDFSDir(localParentPath.toString(), hdfsParentPath.toString());
            if(!flag)
                throw new ResultException("父目录不存在");
        }

        Path localPath = Paths.get(localParentPath.toString(), directoryName);
        Path hdfsPath = Paths.get(HDFS_DIR, userId.toString(), location, directoryName);
        if(Files.exists(localPath)){
            throw new ResultException("目录已存在");
        }

        //本地创建目录
        Files.createDirectory(localPath);

        //更新mongoDB
        FileNode fileNode = new FileNode();
        fileNode.setFileName(directoryName);
        fileNode.setFileType(FileTypeEnum.DIRECTORY.getCode());
        fileNode.setFilePath(Paths.get(userId.toString(), location, directoryName).toString());
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        fileNode.setUpdateTime(dateTimeFormatter.format(localDateTime));
        fileNode.setHdfsFilePath(hdfsPath.toString());

        //更新mongodb
        insertFileNode(fileNode);

        //HDFS 创建目录
        try {
            HDFSUtil hdfsUtil = new HDFSUtil();
            hdfsUtil.mkdir(hdfsPath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResultException("创建失败");
        }
        return fileNode;
    }

    //检查hdfs上是否有该文件，如果没有，返回false, 如果有，将该文件下载到本地，返回true
    public boolean checkAndLoadHDFSDir(String localPathStr, String hdfsPathStr) throws InterruptedException, IOException, URISyntaxException {
        HDFSUtil hdfsUtil = new HDFSUtil();
        if(!hdfsUtil.checkIfExist(hdfsPathStr)){
            return false;
        }
        hdfsUtil.copyToLocalFile(localPathStr, hdfsPathStr);
        return true;
    }

    //添加文件节点，更新父节点
    public void insertFileNode(FileNode fileNode){
        fileNodeRepository.insert(fileNode);
        String parentPath = Paths.get(fileNode.getFilePath()).getParent().toString();
        FileNode parentNode = fileNodeRepository.findByFilePath(parentPath);
        List<FileNode> childNodeList = parentNode.getChildNode();
        if(childNodeList==null || childNodeList.size()==0){
            childNodeList = new ArrayList<>(1);
        }
        childNodeList.add(fileNode);
        parentNode.setChildNode(childNodeList);
        fileNodeRepository.insert(parentNode);
    }


    //返回压缩后的目录文件路径或者文件路径
    public String checkDownloadFile(Long userId, String location, String fileName) throws IOException, URISyntaxException, InterruptedException {
        Path targetPath = Paths.get(BASE_DIR, userId.toString(), location, fileName);
        System.out.println("targetPath........." + targetPath);
        return checkDownloadFile(targetPath.toString(), userId);
    }

    public String checkDownloadFile(String path) throws IOException, URISyntaxException, InterruptedException {
        Path targetPath = Paths.get(path);
        String userId = path.split("/")[2];
        System.out.println("userId....." + userId);
        return checkDownloadFile(path, Long.parseLong(userId));
    }

    public String checkDownloadFile(String path, Long userId) throws IOException, URISyntaxException, InterruptedException {
        Path targetPath = Paths.get(path);
        String fileName = targetPath.getFileName().toString();
        if(Files.notExists(targetPath)){
            Path localPath = Paths.get(BASE_DIR, path);
            Path hdfsPath = Paths.get(HDFS_DIR, path);
            boolean flag = checkAndLoadHDFSDir(localPath.toString(), hdfsPath.toString());
            if(!flag) {
                throw new ResultException("该文件不存在");
            }
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

    //放入回收站
    public void moveToTrash(Long userId, String location, String fileName) throws IOException {
       //删除文件节点
        Path path = Paths.get(userId.toString(), location, fileName);
        String filePath = path.toString();
        String parentPath = path.getParent().toString();
        deleteRecursive(filePath, parentPath);

        //todo 添加回收站节点
//        Recycle recycle = new Recycle();
//        recycle.set

    }

    //递归地删除节点并更新父目录
    private void deleteRecursive(String filePath, String parentPath){
        FileNode fileNode = fileNodeRepository.findByFilePath(filePath);
        if(fileNode==null || !fileNode.getFileType().equals(FileTypeEnum.DIRECTORY.getCode()))
            return;
        LinkedList<FileNode> linkedList = new LinkedList<>();
        linkedList.add(fileNode);
        while(!linkedList.isEmpty()){
            FileNode tempNode = linkedList.pop();
            List<FileNode> childNodeList = tempNode.getChildNode();
            if(childNodeList!=null) {
                for (FileNode childNode : childNodeList) {
                    if (childNode.getFileType().equals(FileTypeEnum.DIRECTORY.getCode())) {
                        linkedList.push(childNode);
                    }
                }
            }
            fileNodeRepository.deleteById(tempNode.getFileId());
        }

        //更新父目录
        FileNode parentFileNode = fileNodeRepository.findByFilePath(parentPath);
        if(parentFileNode!=null){
            List<FileNode> childNodeList = parentFileNode.getChildNode();
            if(childNodeList!=null){
                for(FileNode childNode: childNodeList){
                    if(childNode.getFileId().equals(fileNode.getFileId())){
                        childNodeList.remove(childNode);
                    }
                }
            }
        }
    }

    //递归添加节点
    public void addRecursive(String path) throws IOException {
        Path localPath = Paths.get(BASE_DIR, path);
        if(Files.notExists(localPath))
            return;
        if(Files.isDirectory(localPath)){
           LinkedList<Path> pathList = new LinkedList<>();
           pathList.add(localPath);
           while (!pathList.isEmpty()){
               Path tempPath = pathList.pop();
               DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempPath);
               FileNode fileNode = getFileNodeByPath(tempPath);
               List<FileNode> childNodeList = new ArrayList<>();
               directoryStream.forEach(subPath->{
                   if(Files.isDirectory(subPath)){
                       pathList.push(subPath);
                   }
                    FileNode childNode = getFileNodeByPath(subPath);
                   childNode.setFileId("");
                   childNodeList.add(childNode);
               });
               fileNode.setChildNode(childNodeList);
               fileNodeRepository.insert(fileNode);
           }
        }
    }

    private FileNode getFileNodeByPath(Path path) {
        FileNode fileNode = new FileNode();
        fileNode.setUpdateTime(FileUtil.getStringDateNow());
        fileNode.setFilePath(path.toString().substring(BASE_DIR.length()-1));
        fileNode.setHdfsFilePath(Paths.get(HDFS_DIR, fileNode.getFilePath()).toString());
        fileNode.setFileType(Files.isDirectory(path) ? FileTypeEnum.DIRECTORY.getCode() : FileTypeEnum.FILE.getCode());
        if(Files.isDirectory(path)){
            fileNode.setFileSize(FileUtil.getFileSize(path));
        }
        fileNode.setFileName(path.getFileName().toString());
        return fileNode;
    }


    public void moveListToTrash(Long userId, String location, String[] fileNameList) throws IOException {
        for(String fileName: fileNameList){
            if(fileName.trim()!="") {
                moveToTrash(userId, location, fileName);
            }
        }
    }

}
