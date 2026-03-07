package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface GetDirectoryInfoService {
    JSONObject getAllDirectoryInfoByParentId(Integer parentId, String username);
    JSONObject getAllDirectoryInfoByInit(String username);
}
