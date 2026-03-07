package org.projects.backend.service.file;

import com.alibaba.fastjson2.JSONObject;
import java.time.LocalDateTime;

public interface InsertFileInfoService {
    JSONObject insertFileInfo(String username, String stringOfPath, String fileName, Integer parentId);
}
