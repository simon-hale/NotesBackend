package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.service.file.InsertFileInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.aliyun.oss.model.SimplifiedObjectMeta;
import org.springframework.dao.DuplicateKeyException;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

@Service
public class InsertFileInfoServiceImpl implements InsertFileInfoService {
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

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject insertFileInfo(String stringOfPath, String fileName, String parentDirectoryId, String language){
        JSONObject resp = new JSONObject();

        if (stringOfPath == null || fileName == null || stringOfPath.isBlank() || fileName.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "文件名和路径均不能为空");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "The file name and path cannot be empty.");
            }
            return resp;
        }
        stringOfPath = stringOfPath.trim();
        fileName = fileName.trim();
        if (fileName.length() > 100) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件名长度不能大于100个字符"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "File name max 100 chars.");
            }
            return resp;
        }

        if (stringOfPath.length() > 1000) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件绝对路径长度不能大于1000个字符"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Path string max 1000 chars.");
            }
            return resp;
        }

        Integer userId = accessTokenExtractor.getCurrentUserId();

        if (parentDirectoryId == null || parentDirectoryId.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录 ID 不能为空。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The parent directory ID cannot be empty."
                    );
            }
            return resp;
        }
        Integer parentId;
        try {
            parentId = Integer.valueOf(parentDirectoryId.trim());
        } catch (NumberFormatException e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录 ID 格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The parent directory ID is invalid."
                    );
            }
            return resp;
        }
        Directory directory;
        try {
            directory = directoryMapper.selectById(parentId);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录查询出错。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Error querying the parent directory."
                    );
            }
            return resp;
        }
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录不存在");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Parent directory does not exist."
                    );
            }
            return resp;
        }
        if (!Objects.equals(directory.getUserId(), userId)) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "未授权的操作");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Unauthorized operation."
                    );
            }
            return resp;
        }

        // 路径必须由目录ID和“/”组成，例如：1/5/12/
        if (!stringOfPath.matches("\\d+(?:/\\d+)*/")) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件路径格式不正确"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "The file path format is invalid."
                );
            }
            return resp;
        }

        String[] pathParts = stringOfPath.split("/");
        String lastDirectoryId = pathParts[pathParts.length - 1];

        if (!lastDirectoryId.equals(directory.getId().toString())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件路径与父目录不匹配"); break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "The file path does not match the parent directory.");
            }
            return resp;
        }

        String objectKey = "user/" + userId + "/" + stringOfPath + fileName;

        OSS ossClient = null;
        LocalDateTime ossLastModifiedTime;

        try {
            ossClient = new OSSClientBuilder()
                    .build(
                            "https://" + ossRegion + domain,
                            accessKeyId,
                            accessKeySecret
                    );

            if (!ossClient.doesObjectExist(bucket, objectKey)) {
                switch (language) {
                    case LanguagesSelector.zh_CN:
                        resp.put("error_message", "文件不存在");
                        break;
                    case LanguagesSelector.en_US:
                    default:
                        resp.put("error_message", "File does not exist.");
                }
                return resp;
            }

            SimplifiedObjectMeta objectMeta =
                    ossClient.getSimplifiedObjectMeta(bucket, objectKey);

            if (objectMeta.getLastModified() == null) {
                switch (language) {
                    case LanguagesSelector.zh_CN:
                        resp.put(
                                "error_message",
                                "无法获取OSS文件最后修改时间"
                        );
                        break;
                    case LanguagesSelector.en_US:
                    default:
                        resp.put(
                                "error_message",
                                "Unable to obtain the OSS object last-modified time."
                        );
                }
                return resp;
            }

            ossLastModifiedTime = LocalDateTime.ofInstant(
                    objectMeta.getLastModified().toInstant(),
                    ZoneId.systemDefault()
            ).withNano(0);

        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "OSS客户端请求失败");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "Error requesting OSS client.");
            }
            return resp;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        List<File> curFileList;
        try {
            curFileList = fileMapper.selectList(new QueryWrapper<File>()
                    .eq("user_id", userId)
                    .eq("parent_id", parentId)
                    .eq("name", fileName));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "现有文件查询错误"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying existing files.");
            }
            return resp;
        }
        if (!curFileList.isEmpty()){
            if(curFileList.size()>1){
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "存在多个同名文件"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Multiple files with the same name exist.");
                }
            }else{
                try {
                    int updated = fileMapper.update(
                            null,
                            new UpdateWrapper<File>()
                                    .eq("id", curFileList.getFirst().getId())
                                    .set("last_modified_time", ossLastModifiedTime)
                    );
                    resp.put("error_message", updated == 1 ? "success" : switch (language) {
                        case LanguagesSelector.zh_CN -> "数据库更新错误";
                        case LanguagesSelector.en_US -> "SQL update error.";
                        default -> "SQL update error.";
                    });
                } catch (Exception e) {
                    switch (language) {
                        case LanguagesSelector.zh_CN: resp.put("error_message", "数据库更新出错"); break;
                        case LanguagesSelector.en_US:
                        default: resp.put("error_message", "SQL update error.");
                    }
                }
            }
            return resp;
        }

        LocalDateTime creationTime = ossLastModifiedTime;
        LocalDateTime lastModifiedTime = ossLastModifiedTime;
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

        int inserted;
        try {
            inserted = fileMapper.insert(file);
        } catch (DuplicateKeyException e) {
            resp.put("error_message", "success");
            return resp;
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库操作出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database operation error.");
            }
            return resp;
        }

        resp.put(
                "error_message",
                inserted == 1 ? "success" :
                        switch (language) {
                            case LanguagesSelector.zh_CN -> "数据库插入返回值非1";
                            case LanguagesSelector.en_US -> "Database insertion returned a non-1 value.";
                            default -> "Database insertion returned a non-1 value.";
                        }
        );

        return resp;
    }
}
