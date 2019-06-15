package com.madao.disk.service;

import com.madao.disk.bean.FileNode;
import com.madao.disk.bean.Share;
import com.madao.disk.bean.ShareExample;
import com.madao.disk.dto.ShareDTO;
import com.madao.disk.enums.FileTypeEnum;
import com.madao.disk.exception.ResultException;
import com.madao.disk.mapper.ShareMapper;
import com.madao.disk.util.FileUtil;
import com.madao.disk.util.KeyUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShareService {
    @Autowired
    private ShareMapper shareMapper;

    private static final String BASE_DIR = "/datadir";

    private static final String TEMP_DIR = "/datadir/temp";

    private static final String TRASH_DIR = "/datadir/trash";

    private static final String shareLink = "http://localhost/share/get";

    public String addShare(Long userId, String userName, String location, String fileName){
        Path filePath = Paths.get(BASE_DIR, userId.toString(), location, fileName);
        if(Files.notExists(filePath)){
            throw new ResultException("该文件不存在");
        }

        ShareExample shareExample = new ShareExample();
        ShareExample.Criteria criteria = shareExample.createCriteria();
        criteria.andFilePathEqualTo(filePath.toString());
        int count = shareMapper.countByExample(shareExample);
        if(count>0)
            throw new ResultException("该文件已分享过了");

        //保存分享的记录
        Share share = new Share();
        share.setShareId(KeyUtil.genUniquKeyOnLong());
        share.setShareLink(KeyUtil.genStringCode(20));
        share.setUserId(userId);
        share.setUserName(userName);
        share.setFilePath(filePath.toString());
        shareMapper.insertSelective(share);

        String link = shareLink + "/" + share.getShareLink();
        return link;
    }

    public List<ShareDTO> getShareListByUser(Long userId){
        ShareExample shareExample = new ShareExample();
        ShareExample.Criteria criteria = shareExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<Share> shareList = shareMapper.selectByExample(shareExample);

        List<ShareDTO> shareDTOList = new ArrayList<>(shareList.size());
        for(Share share: shareList){
            ShareDTO shareDTO = new ShareDTO();
            BeanUtils.copyProperties(share, shareDTO);
            File file = new File(share.getFilePath());
            Path path = file.toPath();
            shareDTO.setFileName(file.getName());
            shareDTO.setFileSize(FileUtil.getFileSize(path));
            shareDTO.setFileCreateTime(FileUtil.getStringDate(file.lastModified()));
            shareDTO.setShareLink(shareLink + "/" + share.getShareLink());
            shareDTO.setFileType(Files.isDirectory(path) ? FileTypeEnum.DIRECTORY.getCode() : FileTypeEnum.FILE.getCode());
            shareDTOList.add(shareDTO);
        }
        return shareDTOList;
    }

    public ShareDTO getShareFile(String linkCode) {
        ShareExample shareExample = new ShareExample();
        ShareExample.Criteria criteria = shareExample.createCriteria();
        criteria.andShareLinkEqualTo(linkCode);
        List<Share> shareList = shareMapper.selectByExample(shareExample);
        if(shareList==null || shareList.size()==0){
            throw new ResultException("该分享不存在");
        }
        Share share = shareList.get(0);
        Path filePath = Paths.get(share.getFilePath());

        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        FileNode fileNode = getFileNodeByPath(filePath);
        shareDTO.setFileSize(fileNode.getFileSize());
        shareDTO.setFileType(fileNode.getFileType());
        shareDTO.setShareLink(shareLink + "/" + share.getShareLink());
        shareDTO.setFileCreateTime(fileNode.getUpdateTime());
        shareDTO.setShareTime(FileUtil.getStringDate(share.getCreateTime().toInstant().toEpochMilli()));
        shareDTO.setFileName(fileNode.getFileName());

        return shareDTO;
    }

    public static FileNode getFileNodeByPath(Path path){
        File file = path.toFile();
        FileNode fileNode = new FileNode();
        fileNode.setFileName(file.getName());
        fileNode.setFileType(Files.isDirectory(file.toPath()) ? FileTypeEnum.DIRECTORY.getCode() : FileTypeEnum.FILE.getCode());
        fileNode.setUpdateTime(FileUtil.getStringDate(file.lastModified()));
        fileNode.setFileSize(FileUtil.getFileSize(path));
        fileNode.setFilePath(path.toString());
        return fileNode;
    }

    public String getFilePathByShareId(Long shareId) {
        Share share = shareMapper.selectByPrimaryKey(shareId);
        if(share==null)
            return null;
        else
            return share.getFilePath();
    }
}
