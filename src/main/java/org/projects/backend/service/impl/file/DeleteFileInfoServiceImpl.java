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

import java.util.HashMap;
import java.util.Map;
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
    public JSONObject deleteFileById(String idString, String language) {
        JSONObject resp = new JSONObject();
        if (idString == null || idString.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "文件 ID 不能为空。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The file ID cannot be empty."
                    );
            }
            return resp;
        }
        Integer id;
        try {
            id = Integer.valueOf(idString.trim());
        } catch (NumberFormatException e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "文件 ID 格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The file ID is invalid."
                    );
            }
            return resp;
        }
        File file;
        try {
            file = fileMapper.selectById(id);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "目标文件查询出错。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Error querying the target file."
                    );
            }
            return resp;
        }
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
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库操作出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database operation error.");
            }
            return resp;
        }

        if (deleted != 1) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库删除返回值非1"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database deletion returned a non-1 value.");
            }
            return resp;
        }

        String objectKey = "user/" + file.getUserId() + "/" + file.getStringOfPath() + file.getName();

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder()
                    .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);
            ossClient.deleteObject(bucket, objectKey);
            resp.put("error_message", "success");
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库记录已删除，但OSS中旧文件清理失败"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database deleted, but OSS cleanup failed.");
            }
        } finally {
            if (ossClient != null) ossClient.shutdown();
        }

        return resp;
    }
}
