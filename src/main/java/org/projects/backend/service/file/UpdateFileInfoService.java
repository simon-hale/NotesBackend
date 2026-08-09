package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;

public interface UpdateFileInfoService {
    JSONObject modifyFileNameById(Integer parentId, Integer fileId, String filenameNew, String language);
}
