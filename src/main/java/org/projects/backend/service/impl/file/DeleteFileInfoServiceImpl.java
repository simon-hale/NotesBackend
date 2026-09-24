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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

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

        String objectKey =
                "user/"
                        + file.getUserId()
                        + "/"
                        + file.getStringOfPath()
                        + file.getName();

        OSS ossClient = null;

        try {
            ossClient = new OSSClientBuilder()
                    .build(
                            "https://" + ossRegion + domain,
                            accessKeyId,
                            accessKeySecret
                    );

            ossClient.deleteObject(bucket, objectKey);

        } catch (Exception e) {

            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put(
                            "error_message",
                            "OSS文件删除失败，数据库记录未删除"
                    );
                    break;

                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Failed to delete the OSS object. "
                                    + "The database record was not deleted."
                    );
            }

            return resp;

        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        int deleted;
        try {
            QueryWrapper<File> deleteWrapper = new QueryWrapper<File>()
                    .eq("id", id)
                    .eq("user_id", userId)
                    .eq("parent_id", file.getParentId())
                    .eq("name", file.getName());
            if (file.getStringOfPath() == null) {
                deleteWrapper.isNull("string_of_path");
            } else {
                deleteWrapper.eq("string_of_path", file.getStringOfPath());
            }
            deleted = fileMapper.delete(deleteWrapper);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put(
                            "error_message",
                            "OSS文件已删除，但数据库记录删除失败，请重试"
                    );
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The OSS object was deleted, but the database "
                                    + "record deletion failed. Please retry."
                    );
            }
            return resp;
        }

        if (deleted == 1) {
            resp.put("error_message", "success");
            return resp;
        }

        if (deleted == 0) {
            try {
                File currentFile = fileMapper.selectById(id);
                if (currentFile == null) {
                    resp.put("error_message", "success");
                    return resp;
                }
                switch (language) {
                    case LanguagesSelector.zh_CN:
                        resp.put(
                                "error_message",
                                "文件状态已发生变化，数据库记录未删除，请刷新后重试"
                        );
                        break;
                    case LanguagesSelector.en_US:
                    default:
                        resp.put(
                                "error_message",
                                "The file state has changed. "
                                        + "The database record was not deleted. "
                                        + "Refresh and try again."
                        );
                }
                return resp;
            } catch (Exception e) {
                switch (language) {
                    case LanguagesSelector.zh_CN:
                        resp.put(
                                "error_message",
                                "数据库删除未生效，且文件状态复查失败，请刷新后重试"
                        );
                        break;
                    case LanguagesSelector.en_US:
                    default:
                        resp.put(
                                "error_message",
                                "The database deletion did not take effect, "
                                        + "and the file state could not be verified. "
                                        + "Refresh and try again."
                        );
                }
                return resp;
            }
        }
        switch (language) {
            case LanguagesSelector.zh_CN:
                resp.put("error_message", "数据库删除返回值异常");
                break;
            case LanguagesSelector.en_US:
            default: resp.put("error_message", "Database deletion returned an unexpected value.");
        }

        return resp;
    }
}
