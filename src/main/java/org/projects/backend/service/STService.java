package org.projects.backend.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class STService {

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.role-arn}")
    private String roleArn;

    @Value("${aliyun.region-id}")
    private String regionId;

    public AssumeRoleResponse.Credentials assumeRole(String policyJson) throws Exception {
        DefaultProfile profile = DefaultProfile.getProfile(
                regionId,
                accessKeyId,
                accessKeySecret
        );

        IAcsClient client = new DefaultAcsClient(profile);

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(roleArn);
        request.setRoleSessionName("oss-upload-session");
        request.setPolicy(policyJson);
        request.setDurationSeconds(900L); // 15 分钟

        AssumeRoleResponse response = client.getAcsResponse(request);
        return response.getCredentials();
    }
}
