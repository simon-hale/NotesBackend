package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;

public interface UpdateFileInfoService {
    JSONObject modifyFileNameById(String parentDirectoryId, String fileIdString, String filenameNew, String language);
}
