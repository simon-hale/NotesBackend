package org.projects.backend.controller;

import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.STService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oss")
public class OssStsController {

    @Autowired
    private STService stService;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.region}")
    private String ossRegion;

    @GetMapping("/sts/")
    public Map<String, Object> getStsToken(@RequestParam Map<String, String> data) throws Exception {
        String username = data.get("username");
        String stringOfPath = data.get("string_of_path");
        String fileName = data.get("filename");
        String parentId = data.get("parent_id");

        // ① TODO：校验用户身份（JWT / Session）
        Integer userId = userMapper.selectOne(new QueryWrapper<User>().eq("username", username)).getId();

        // ② 生成 objectKey（你自己的目录逻辑）
        String objectKey = "user/" + userId + "/" + stringOfPath + fileName;

        Map<String, Object> statement = new HashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Action", List.of(
                "oss:PutObject",
                "oss:InitiateMultipartUpload",
                "oss:UploadPart",
                "oss:CompleteMultipartUpload",
                "oss:GetObject",
                "oss:HeadObject",
                "oss:DeleteObject"
        ));
        statement.put("Resource", List.of(
                "acs:oss:*:*:" + bucket + "/user/" + userId + "/*"
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
        result.put("expireAt", cred.getExpiration());
        result.put("bucket", bucket);
        result.put("region", ossRegion);
        result.put("objectKey", objectKey);

        if (!fileMapper.selectList(new QueryWrapper<File>()
                        .eq("user_id", userId)
                        .eq("parent_id", parentId)
                        .eq("name", fileName))
                .isEmpty()) result.put("error_message", "same_file_name");
        else result.put("error_message", "success");

        return result;
    }
}
