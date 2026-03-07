package org.projects.backend.service.impl.file;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.file.GetFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class GetFileInfoServiceImpl implements GetFileInfoService {
    @Autowired
    private FileMapper fileMapper;
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

    @Override
    public JSONObject getAllFileInfoByParentId(Integer parentId, Integer userId) {
        return null;
    }

    @Override
    public JSONObject getFileInfoById(Integer parentId, Integer userId) {
        return null;
    }

    @Override
    public JSONObject getFileURL(Integer id, String username) {
        JSONObject resp = new JSONObject();
        File file = fileMapper.selectById(id);
        if (file == null) {
            resp.put("error_message", "File does not exist.");
            return resp;
        }
        if (!userMapper.selectOne(new QueryWrapper<User>().eq("username", username)).getId().equals(file.getUserId())) {
            resp.put("error_message", "Permission denied.");
            return resp;
        }
        String objectKey = "user/" + file.getUserId() + "/" + file.getStringOfPath() + file.getName();

        OSS ossClient = new OSSClientBuilder()
                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);

        if (!ossClient.doesObjectExist(bucket, objectKey)) {
            resp.put("error_message", "OSS object not found.");
            return resp;
        }

        try {
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
            URL url = ossClient.generatePresignedUrl(
                    bucket,
                    objectKey,
                    expiration
            );

            resp.put("url", url.toString());
            resp.put("type", file.getType());
            resp.put("error_message", "success");
        } finally {
            ossClient.shutdown();
        }

        return resp;
    }
}
