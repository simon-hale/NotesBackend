package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface GetDirectoryInfoService {
    JSONObject getAllDirectoryInfoByInit();
    JSONObject getAllDirectoryInfoByParentId(Integer parentId);
}
