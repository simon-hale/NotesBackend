package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;

public interface UpdateFileInfoService {
    JSONObject modifyFileNameById(String username, Integer parentId, Integer fileId, String filenameNew);
}
