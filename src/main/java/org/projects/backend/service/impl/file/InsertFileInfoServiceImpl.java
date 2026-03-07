package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.file.InsertFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@Service
public class InsertFileInfoServiceImpl implements InsertFileInfoService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMapper fileMapper;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.region}")
    private String ossRegion;

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.domain}")
    private String domain;

    @Override
    public JSONObject insertFileInfo(String username, String stringOfPath, String fileName, Integer parentId){
        JSONObject resp = new JSONObject();

        if (fileName == null || fileName.isEmpty()) {
            resp.put("error_message", "Filename is null or empty.");
            return resp;
        }

        Integer userId;
        try {
            userId = userMapper.selectOne(new QueryWrapper<User>().eq("username", username)).getId();
        } catch (Exception e) {
            resp.put("error_message", "SQL error(User Info Enquiry).");
            return resp;
        }

        String objectKey = "user/" + userId + "/" + stringOfPath + fileName;

        OSS ossClient = new OSSClientBuilder()
                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);

        boolean exists;
        try {
            exists = ossClient.doesObjectExist(bucket, objectKey);
        } finally {
            ossClient.shutdown();
        }

        if (!exists) {
            resp.put("error_message", "File Not Exists.");
            return resp;
        }

        List<File> curFileList = fileMapper.selectList(new QueryWrapper<File>()
                .eq("user_id", userId)
                .eq("parent_id", parentId)
                .eq("name", fileName));
        if (!curFileList.isEmpty()){
            if(curFileList.size()>1){
                resp.put("error_message", "Same files more than once.");
            }else {
                if (curFileList.getFirst().getStringOfPath().equals(stringOfPath)) {
                    try {
                        fileMapper.update(
                                null,
                                new UpdateWrapper<File>()
                                        .eq("id", curFileList.getFirst().getId())
                                        .set("last_modified_time", LocalDateTime.now())
                        );
                        resp.put("error_message", "success");
                    } catch (Exception e) {
                        resp.put("error_message", "SQL error.");
                    }
                }else {
                    try {
                        OSS ossClientpdateDelete = new OSSClientBuilder()
                                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);
                        String objectKeyUpdateDelete = "user/" + userId + "/" + curFileList.getFirst().getStringOfPath() + curFileList.getFirst().getName();
                        ossClientpdateDelete.deleteObject(bucket, objectKeyUpdateDelete);
                    } finally {
                        ossClient.shutdown();
                    }
                    try {
                        fileMapper.update(
                                null,
                                new UpdateWrapper<File>()
                                        .eq("id", curFileList.getFirst().getId())
                                        .set("last_modified_time", LocalDateTime.now())
                                        .set("string_of_path", stringOfPath)
                        );
                        resp.put("error_message", "success");
                    } catch (Exception e) {
                        resp.put("error_message", "SQL error.");
                    }
                }
            }
            return resp;
        }

        LocalDateTime creationTime = java.time.LocalDateTime.now();
        LocalDateTime lastModifiedTime = java.time.LocalDateTime.now();
        File file = new File();
        file.setName(fileName);
        file.setParentId(parentId);
        file.setCreationTime(creationTime);
        file.setLastModifiedTime(lastModifiedTime);
        file.setUserId(userId);
        file.setStringOfPath(stringOfPath);

        Pattern pattern = Pattern.compile("\\.([^.]*)$");
        Matcher matcher = pattern.matcher(fileName);
        String type = matcher.find() ? matcher.group(1) : "null";
        file.setType(type);

        if(fileMapper.insert(file) == 1){
            resp.put("error_message", "success");
        }else{
            resp.put("error_message", "SQL error.");
        }

        return resp;
    }
}
