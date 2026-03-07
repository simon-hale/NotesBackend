package org.projects.backend.service.directory;

import com.alibaba.fastjson2.JSONObject;

public interface InsertDirectoryInfoService {
    JSONObject createDirectory(String name, Integer parentId, String username);
}
