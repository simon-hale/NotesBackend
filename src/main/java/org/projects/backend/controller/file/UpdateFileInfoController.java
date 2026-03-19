package org.projects.backend.controller.file;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.file.UpdateFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateFileInfoController {
    @Autowired
    private UpdateFileInfoService updateFileInfoService;

    @PostMapping("/api/file/modify/name/")
    JSONObject modifyFileNameById(@RequestParam Map<String, String> data) {
        return updateFileInfoService.modifyFileNameById(data.get("username"), Integer.parseInt(data.get("parentId")), Integer.parseInt(data.get("fileId")), data.get("filenameNew"), data.get("language"));
    }
}
