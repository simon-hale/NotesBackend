package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface UpdateDirectoryInfoService {
    JSONObject modifyDirectoryNameById(String idString, String name, String language);
}
