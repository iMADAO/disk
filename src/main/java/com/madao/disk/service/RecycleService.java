package com.madao.disk.service;

import com.madao.disk.bean.Recycle;
import com.madao.disk.bean.RecycleExample;
import com.madao.disk.dto.RecycleDTO;
import com.madao.disk.enums.FileTypeEnum;
import com.madao.disk.exception.ResultException;
import com.madao.disk.mapper.RecycleMapper;
import com.madao.disk.util.FileUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecycleService {
    private static final String BASE_DIR = "/datadir";

    private static final String TEMP_DIR = "/datadir/temp";

    private static final String TRASH_DIR = "/datadir/trash";
    @Autowired
    private RecycleMapper recycleMapper;

    public void deleteRecycle(Long recycleId, Long userId) {
        Recycle recycle = recycleMapper.selectByPrimaryKey(recycleId);
        if(!recycle.getUserId().equals(userId)){
            throw new ResultException("文件不属于该用户");
        }
        recycleMapper.deleteByPrimaryKey(recycleId);
        //todo 删除文件系统的文件
    }

    public void recoverRecycle(Long recycleId, Long userId) throws IOException {
        Recycle recycle = recycleMapper.selectByPrimaryKey(recycleId);
        if(!recycle.getUserId().equals(userId)){
            throw new ResultException("文件不属于该用户");
        }

        //将文件从回收站区域移动到用户的文件目录
        Path path = Paths.get(recycle.getFilePath());
        Path targetPath = Paths.get(recycle.getOriginPath());
        Files.move(path, targetPath);

        recycleMapper.deleteByPrimaryKey(recycleId);
    }

    public List<RecycleDTO> getRecycleFileList(Long userId) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm-ss");
        RecycleExample recycleExample = new RecycleExample();
        RecycleExample.Criteria criteria = recycleExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<Recycle> recycleList = recycleMapper.selectByExample(recycleExample);
        List<RecycleDTO> recycleDTOList = new ArrayList<>(recycleList.size());
        for(Recycle recycle: recycleList){
            RecycleDTO recycleDTO = new RecycleDTO();
            BeanUtils.copyProperties(recycle, recycleDTO);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(recycle.getOriginCreateTime()), ZoneId.systemDefault());
            String dateString = dateTimeFormatter.format(localDateTime);
            recycleDTO.setOriginCreateTime(dateString);
            recycleDTOList.add(recycleDTO);
        }
        return recycleDTOList;
    }

    public List<RecycleDTO> getRecycleFileListInLocation(Long userId, String location) throws IOException {
        Path path = Paths.get(TRASH_DIR, userId.toString(), location);
        if(Files.notExists(path)){
            throw new ResultException("文件不存在");
        }
        if(!Files.isDirectory(path))
            return null;
        List<RecycleDTO> recycleDTOList = new ArrayList<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
        for(Path p: directoryStream){
            RecycleDTO recycleDTO = new RecycleDTO();
            recycleDTO.setFileName(p.getFileName().toString());
            recycleDTO.setFileSize(FileUtil.getFileSize(p));
            recycleDTO.setFileType(Files.isDirectory(p) ? FileTypeEnum.DIRECTORY.getCode() : FileTypeEnum.FILE.getCode());
            recycleDTOList.add(recycleDTO);
        }
        return recycleDTOList;
    }

    public void deleteRecycleList(String[] recycleIdList, Long userId) {
        for(String recycleId: recycleIdList){
            if(recycleId.trim()!=""){
                deleteRecycle(Long.parseLong(recycleId), userId);
            }
        }
    }

    public void recoverRecycleList(String[] recycleIdList, Long userId) throws IOException {
        for(String recycleId: recycleIdList){
            recycleId = recycleId.trim();
            if(recycleId!=""){
                recoverRecycle(Long.parseLong(recycleId), userId);
            }
        }
    }
}
