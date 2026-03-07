package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;

public interface DeleteFileInfoService {
    JSONObject deleteFileById(Integer id, String username);
}
