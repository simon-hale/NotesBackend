package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.file.UpdateFileInfoService;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpdateFileInfoServiceImpl implements UpdateFileInfoService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private DirectoryMapper directoryMapper;

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
    public JSONObject modifyFileNameById(String username, Integer parentId, Integer fileId, String filenameNew, String language){
        JSONObject resp = new JSONObject();

        if (filenameNew == null || filenameNew.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件名不能为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Filename is null or empty.");
            }
            return resp;
        }

        User user;
        try {
            user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if (user == null) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "用户不存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "User Not Exists.");
                }
                return resp;
            }
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "SQL错误（用户信息查询）"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "SQL error(User Info Enquiry).");
            }
            return resp;
        }

        Directory directory = directoryMapper.selectById(parentId);
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目标目录不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory Not Exists.");
            }
            return resp;
        } else if (!directory.getUserId().equals(user.getId())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "用户ID与目录不匹配"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "User Id Not Match With Directory.");
            }
            return resp;
        }

        File fileTarget = fileMapper.selectById(fileId);
        if (fileTarget == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "File Not Exists.");
            }
            return resp;
        }
        if (!fileTarget.getParentId().equals(directory.getId())
                || !fileTarget.getUserId().equals(user.getId())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件父目录ID与目录或用户不匹配"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "File Parent Id Mismatch With Directory Or User.");
            }
            return resp;
        }

        List<File> filesNewCheck = fileMapper.selectList(new QueryWrapper<File>().eq("name", filenameNew).eq("parent_id", parentId));
        if (!filesNewCheck.isEmpty()) {
            if (filesNewCheck.size() > 1)
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "MySQL中存在多个目标文件"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Target files more than once in MySQL.");
                }
            else
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "目标文件名已存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Target File Name Already Exists.");
                }
            return resp;
        }

        LocalDateTime newTime = LocalDateTime.now().withNano(0);
        LocalDateTime oldTime = fileTarget.getLastModifiedTime();

        if (oldTime.equals(newTime) && filenameNew.equals(fileTarget.getName())) {
            resp.put("error_message", "success");
            return resp;
        }

        String objectKeyOld = "user/" + fileTarget.getUserId() + "/" + fileTarget.getStringOfPath() + fileTarget.getName();
        String objectKeyNew = "user/" + fileTarget.getUserId() + "/" + fileTarget.getStringOfPath() + filenameNew;

        OSS ossClient = new OSSClientBuilder()
                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);

        try {
            if (!ossClient.doesObjectExist(bucket, objectKeyOld)) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "MySQL中存在源文件但OSS中不存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Origin file in MySQL but not in OSS.");
                }
                return resp;
            }
            if (ossClient.doesObjectExist(bucket, objectKeyNew)) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "OSS中存在目标文件但MySQL中不存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Target file in OSS but not in MySQL.");
                }
                return resp;
            }
            ossClient.copyObject(bucket, objectKeyOld, bucket, objectKeyNew);
            ossClient.deleteObject(bucket, objectKeyOld);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "OSS文件重命名失败"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "OSS file rename failed.");
            }
            return resp;
        } finally {
            ossClient.shutdown();
        }

        Pattern pattern = Pattern.compile("\\.([^.]*)$");
        Matcher matcher = pattern.matcher(filenameNew);
        String type = matcher.find() ? matcher.group(1) : "null";

        try {
            fileMapper.update(
                    null,
                    new UpdateWrapper<File>()
                            .eq("id", fileTarget.getId())
                            .set("name", filenameNew)
                            .set("last_modified_time", newTime)
                            .set("type", type)
            );
            resp.put("error_message", "success");
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "SQL错误，但OSS文件已重命名"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "SQL error, but file renamed in OSS.");
            }
        }

        return resp;
    }
}
