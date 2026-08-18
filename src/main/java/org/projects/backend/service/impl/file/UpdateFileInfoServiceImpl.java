package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.service.file.UpdateFileInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpdateFileInfoServiceImpl implements UpdateFileInfoService {

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
    public JSONObject modifyFileNameById(String parentDirectoryId, String fileIdString, String filenameNew, String language){
        JSONObject resp = new JSONObject();

        if (filenameNew == null || filenameNew.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "文件名不能为空");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "The file name cannot be empty.");
            }
            return resp;
        }
        filenameNew = filenameNew.trim();
        if (filenameNew.length() > 100) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "文件名长度不能大于100个字符"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "File name max 100 chars.");
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

        if (fileIdString == null || fileIdString.isBlank()) {
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
        Integer fileId;
        try {
            fileId = Integer.valueOf(fileIdString.trim());
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
        File fileTarget;
        try {
            fileTarget = fileMapper.selectById(fileId);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目标文件查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying target file.");
            }
            return resp;
        }
        if (fileTarget == null) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "目标文件查询结果为空");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "No results found for the target file."
                    );
            }
            return resp;
        }
        if (!Objects.equals(fileTarget.getParentId(), directory.getId())
                || !Objects.equals(fileTarget.getUserId(), userId)) {
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

        List<File> filesNewCheck;
        try {
            filesNewCheck = fileMapper.selectList(new QueryWrapper<File>().eq("name", filenameNew).eq("parent_id", parentId).eq("user_id", userId));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目标文件检查列表查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error in target file check list query.");
            }
            return resp;
        }
        if (!filesNewCheck.isEmpty()) {
            if (filesNewCheck.size() > 1) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "MySQL中存在多个目标文件"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Target files more than once in MySQL.");
                }
            } else if (!filesNewCheck.getFirst().getId().equals(fileId)) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "已存在同名文件"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "The file with the same name already exists.");
                }
            } else resp.put("error_message", "success");
            return resp;
        }

        LocalDateTime newTime = LocalDateTime.now().withNano(0);

        String objectKeyOld = "user/" + fileTarget.getUserId() + "/" + fileTarget.getStringOfPath() + fileTarget.getName();
        String objectKeyNew = "user/" + fileTarget.getUserId() + "/" + fileTarget.getStringOfPath() + filenameNew;

        OSS ossClient;
        try {
            ossClient = new OSSClientBuilder()
                    .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "OSS客户端创建失败"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error building OSS client.");
            }
            return resp;
        }

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

            try {
                ossClient.copyObject(bucket, objectKeyOld, bucket, objectKeyNew);
            } catch (Exception e) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "OSS文件复制失败"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "OSS file copy failed.");
                }
                return resp;
            }

            Pattern pattern = Pattern.compile("\\.([^.]*)$");
            Matcher matcher = pattern.matcher(filenameNew);
            String type = matcher.find() ? matcher.group(1) : "null";
            int updated;
            try {
                updated = fileMapper.update(
                        null,
                        new UpdateWrapper<File>()
                                .eq("id", fileTarget.getId())
                                .set("name", filenameNew)
                                .set("last_modified_time", newTime)
                                .set("type", type)
                );
            } catch (Exception e) {
                try {
                    ossClient.deleteObject(bucket, objectKeyNew);
                } catch (Exception cleanupException) {
                    switch (language) {
                        case LanguagesSelector.zh_CN: resp.put("error_message", "SQL更新异常，且新OSS副本清理失败"); break;
                        case LanguagesSelector.en_US:
                        default: resp.put("error_message", "SQL update exception, and the new OSS copy could not be cleaned up.");
                    }
                    return resp;
                }
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "SQL更新异常，OSS已撤销重命名"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "SQL update exception. OSS rename has been undone.");
                }
                return resp;
            }
            if (updated != 1) {
                try {
                    ossClient.deleteObject(bucket, objectKeyNew);
                } catch (Exception cleanupException) {
                    switch (language) {
                        case LanguagesSelector.zh_CN: resp.put("error_message", "SQL未成功更新文件记录，且新OSS副本清理失败"); break;
                        case LanguagesSelector.en_US:
                        default: resp.put("error_message", "File record was not updated, and the new OSS copy could not be cleaned up.");
                    }
                    return resp;
                }
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "SQL未成功更新文件记录，OSS已撤销重命名"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "File record was not updated. OSS rename has been undone.");
                }
                return resp;
            }

            try {
                ossClient.deleteObject(bucket, objectKeyOld);
            } catch (Exception e) {
                resp.put("error_message", "success_oss_error");

                switch (language) {
                    case LanguagesSelector.zh_CN:
                        resp.put(
                                "warning_message",
                                "文件已重命名，但旧OSS对象暂未删除"
                        );
                        break;
                    case LanguagesSelector.en_US:
                    default:
                        resp.put(
                                "warning_message",
                                "The file was renamed, but the old OSS object was not deleted."
                        );
                }

                return resp;
            }
            resp.put("error_message", "success");
            return resp;
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "OSS操作异常"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "OSS operation exception.");
            }
            return resp;
        } finally {
            ossClient.shutdown();
        }
    }
}
