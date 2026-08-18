package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface DeleteDirectoryInfoService {
    JSONObject deleteDirectoryById(String idString, String language);
}
