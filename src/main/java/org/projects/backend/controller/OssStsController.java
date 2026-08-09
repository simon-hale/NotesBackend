package org.projects.backend.controller;

import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.service.STService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.projects.backend.utils.StsUsageSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/oss")
public class OssStsController {

    @Autowired
    private STService stService;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private DirectoryMapper directoryMapper;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.region}")
    private String ossRegion;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @GetMapping("/sts/")
    public Map<String, Object> getStsToken(@RequestParam Map<String, String> data) throws Exception {
        String language = data.get("language") == null ? LanguagesSelector.en_US : data.get("language");
        // ① 校验用户身份
        Integer userId = accessTokenExtractor.getCurrentUserId();
        String usage = data.get("usage") == null ? "" : data.get("usage").trim();
        String stringOfPath = data.get("string_of_path");
        String fileName = data.get("filename");
        if (stringOfPath == null || fileName == null || stringOfPath.isBlank() || fileName.isBlank()) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "文件名和路径均不能为空");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put("error_message", "The file name and path cannot be empty.");
            }
            return rejectResult;
        }
        if (stringOfPath.length() > 1000) {
            Map<String, Object> rejectResultPath = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResultPath.put("error_message", "路径长度不能超过1000个字符。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResultPath.put("error_message", "Path cannot exceed 1000 characters.");
            }
            return rejectResultPath;
        }
        if (fileName.length() > 100) {
            Map<String, Object> rejectResultPath = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResultPath.put("error_message", "文件名长度不能超过100个字符。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResultPath.put("error_message", "File name cannot exceed 100 characters.");
            }
            return rejectResultPath;
        }
        if (stringOfPath.contains("*")
                || stringOfPath.contains("?")
                || fileName.contains("*")
                || fileName.contains("?")) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put(
                            "error_message",
                            "文件名或路径不能包含“*”或“?”。"
                    );
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The file name or path cannot contain \"*\" or \"?\"."
                    );
            }
            return rejectResult;
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            Map<String, Object> rejectResult = new HashMap<>();

            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "文件名不能包含路径分隔符。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The file name cannot contain path separators."
                    );
            }

            return rejectResult;
        }
        String parentId = data.get("parent_id");
        if (parentId == null || parentId.isBlank()) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "父目录 ID 不能为空。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The parent directory ID cannot be empty."
                    );
            }
            return rejectResult;
        }
        Integer parentDirectoryId;
        try {
            parentDirectoryId = Integer.valueOf(parentId.trim());
        } catch (NumberFormatException e) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "父目录 ID 格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The parent directory ID is invalid."
                    );
            }
            return rejectResult;
        }
        Directory directory = directoryMapper.selectById(parentDirectoryId);
        if (directory == null) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "父目录查询失败");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "Error querying parent directory."
                    );
            }
            return rejectResult;
        }
        if (!Objects.equals(directory.getUserId(), userId)) {
            Map<String, Object> rejectResult = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "未授权的操作");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "Unauthorized operation."
                    );
            }
            return rejectResult;
        }
        // 路径必须由目录ID和“/”组成，例如：1/5/12/
        if (!stringOfPath.matches("\\d+(?:/\\d+)*/")) {
            Map<String, Object> rejectResult = new HashMap<>();

            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "文件路径格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The file path format is invalid."
                    );
            }

            return rejectResult;
        }

        String[] pathParts = stringOfPath.split("/");
        String lastDirectoryId = pathParts[pathParts.length - 1];

        if (!lastDirectoryId.equals(parentDirectoryId.toString())) {
            Map<String, Object> rejectResult = new HashMap<>();

            switch (language) {
                case LanguagesSelector.zh_CN:
                    rejectResult.put("error_message", "文件路径与父目录不匹配。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    rejectResult.put(
                            "error_message",
                            "The file path does not match the parent directory."
                    );
            }

            return rejectResult;
        }

        // ② 校验STS用途、分配权限
        List<String> actions = switch (usage) {
            case StsUsageSelector.SINGLE_FILE_UPLOAD -> List.of(
                    "oss:PutObject",
                    "oss:AbortMultipartUpload"
            );
            default -> List.of();
        };

        if (actions.isEmpty()) {
            Map<String, Object> errorResultActions = new HashMap<>();
            switch (language) {
                case LanguagesSelector.zh_CN:
                    errorResultActions.put("error_message", "不支持的请求");
                    break;
                case LanguagesSelector.en_US:
                default:
                    errorResultActions.put("error_message", "unsupported usage");
            }
            return errorResultActions;
        }

        // ③ 生成 objectKey（你自己的目录逻辑）
        String objectKey = "user/" + userId + "/" + stringOfPath + fileName;

        Map<String, Object> statement = new HashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Action", actions);
        statement.put("Resource", List.of(
                "acs:oss:*:*:" + bucket + "/" + objectKey
        ));

        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("Version", "1");
        policyMap.put("Statement", List.of(statement));

        String policy = new ObjectMapper().writeValueAsString(policyMap);

        AssumeRoleResponse.Credentials cred = stService.assumeRole(policy);

        Map<String, Object> result = new HashMap<>();
        result.put("accessKeyId", cred.getAccessKeyId());
        result.put("accessKeySecret", cred.getAccessKeySecret());
        result.put("securityToken", cred.getSecurityToken());
//        result.put("expireAt", cred.getExpiration());
        result.put("bucket", bucket);
        result.put("region", ossRegion);
        result.put("objectKey", objectKey);

        if (!fileMapper.selectList(new QueryWrapper<File>()
                        .eq("user_id", userId)
                        .eq("parent_id", parentDirectoryId)
                        .eq("name", fileName))
                .isEmpty()) result.put("error_message", "same_file_name");
        else result.put("error_message", "success");

        return result;
    }
}
