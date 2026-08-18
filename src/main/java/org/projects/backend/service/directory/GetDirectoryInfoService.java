package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface GetDirectoryInfoService {
    JSONObject getAllDirectoryInfoByInit(String language);
    JSONObject getAllDirectoryInfoByParentId(String idString, String language);
}
