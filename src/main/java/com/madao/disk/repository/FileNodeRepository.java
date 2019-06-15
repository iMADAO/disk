package com.madao.disk.repository;

import com.madao.disk.bean.FileNode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileNodeRepository extends MongoRepository<FileNode, String> {
    public FileNode findByFilePath(String filePath);

}
