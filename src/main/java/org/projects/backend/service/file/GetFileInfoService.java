package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;

public interface GetFileInfoService {
    JSONObject getAllFileInfoByParentId(Integer parentId, Integer userId);
    JSONObject getFileInfoById(Integer parentId, Integer userId);
    JSONObject getFileURL(Integer id, String username);
}
