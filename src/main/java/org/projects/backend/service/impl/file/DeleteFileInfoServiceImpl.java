package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.File;
import org.projects.backend.service.file.DeleteFileInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeleteFileInfoServiceImpl implements DeleteFileInfoService {
    @Autowired
    private FileMapper fileMapper;

    @Value("${aliyun.oss.region}")
    private String ossRegion;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.domain}")
    private String domain;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject deleteFileById(Integer id, String language) {
        JSONObject resp = new JSONObject();
        File file = fileMapper.selectById(id);
        if (file == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "File does not exist.");
            }
            return resp;
        }
        Integer userId = accessTokenExtractor.getCurrentUserId();
        if (!Objects.equals(userId, file.getUserId())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "权限不足"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Permission denied.");
            }
            return resp;
        }

        int deleted;

        try {
            deleted = fileMapper.deleteById(id);
        } catch (Exception e) {
            resp.put("error_message", "SQL error.");
            return resp;
        }

        if (deleted != 1) {
            resp.put("error_message", "SQL delete error.");
            return resp;
        }

        String objectKey = "user/" + file.getUserId() + "/" + file.getStringOfPath() + file.getName();

        OSS ossClient = new OSSClientBuilder()
                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);

        try {
            ossClient.deleteObject(bucket, objectKey);
            resp.put("error_message", "success");
        } catch (Exception e) {
            resp.put("error_message", "Database deleted, but OSS cleanup failed.");
        } finally {
            ossClient.shutdown();
        }

        return resp;
    }
}
