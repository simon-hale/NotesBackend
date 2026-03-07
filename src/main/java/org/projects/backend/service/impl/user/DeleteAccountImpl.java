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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Override
    public JSONObject deleteAccount(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> user_list = userMapper.selectList(queryWrapper);
        JSONObject resp = new JSONObject();
        if(user_list.isEmpty()){
            resp.put("error_message", "该用户已被删除");
            return resp;
        }

        if(user_list.size()>1){
            resp.put("error_message", "Same username more than once");
            return resp;
        }

        User user = user_list.getFirst();
        List<File> fileList = fileMapper.selectList(new QueryWrapper<File>().eq("user_id", user.getId()));

        OSS ossClient = new OSSClientBuilder()
                .build("https://" + ossRegion + domain, accessKeyId, accessKeySecret);

        String objectKey;
        try {
            for(File file:fileList){
                objectKey = "user/" + file.getUserId() + "/" + file.getStringOfPath() + file.getName();
                ossClient.deleteObject(bucket, objectKey);
                fileMapper.deleteById(file.getId());
            }
            directoryMapper.delete(new QueryWrapper<Directory>().eq("user_id", user.getId()));
        } finally {
            ossClient.shutdown();
        }

        int result = userMapper.deleteById(user.getId());
        if(result > 0){
            resp.put("error_message", "success");
        }else {
            resp.put("error_message", "数据库错误，请联系管理员");
        }
        return resp;
    }
}
