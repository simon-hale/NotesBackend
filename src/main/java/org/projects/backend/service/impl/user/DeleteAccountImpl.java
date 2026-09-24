package org.projects.backend.service.impl.user;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.user.DeleteAccount;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteAccountImpl implements DeleteAccount {
    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private UserMapper userMapper;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject deleteAccount(String curPassword, String language) {
        JSONObject resp = new JSONObject();
        User user = accessTokenExtractor.getCurrentUser();
        if (curPassword == null || curPassword.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "原密码不能为空"); break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "Original password cannot be empty.");
            }
            return resp;
        }
        if(!passwordEncoder.matches(curPassword, user.getPassword())){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "原密码不正确"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Incorrect original password.");
            }
            return resp;
        }
        List<File> fileList;
        try {
            fileList = fileMapper.selectList(new QueryWrapper<File>().eq("user_id", user.getId()));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "用户文件查询出错，删除操作已终止"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying user files. The delete operation was aborted.");
            }
            return resp;
        }

        OSS ossClient;

        try {
            ossClient = new OSSClientBuilder()
                    .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "OSS客户端创建失败，删除操作已终止"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error building OSS client. The delete operation was aborted.");
            }
            return resp;
        }

        String objectKey;
        try {
            for(File file:fileList){
                objectKey = "user/" + file.getUserId() + "/" + file.getStringOfPath() + file.getName();
                ossClient.deleteObject(bucket, objectKey);
                fileMapper.deleteById(file.getId());
            }
            directoryMapper.delete(new QueryWrapper<Directory>().eq("user_id", user.getId()));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "删除用户数据时出错，删除操作已终止，可能会有残留数据，请联系管理员"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error deleting user data. The delete operation was aborted. There may be residual data. Please contact the administrator.");
            }
            return resp;
        } finally {
            ossClient.shutdown();
        }

        int result;
        try {
            result = userMapper.deleteById(user.getId());
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库删除用户表项失败，但用户已无其他残留数据"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Failed to delete the user record from the database, but no other residual user data remains.");
            }
            return resp;
        }
        if (result == 0 || result == 1) {
            resp.put("error_message", "success");
        } else {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库错误，请联系管理员"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database error, please contact administrator.");
            }
        }
        return resp;
    }
}
