package org.projects.backend.controller.directory;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.directory.InsertDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InsertDirectoryInfoController {
    @Autowired
    private InsertDirectoryInfoService insertDirectoryInfoService;

    @PostMapping("/api/directory/create/")
    JSONObject createDirectory(@RequestParam Map<String, String> data) {
        return insertDirectoryInfoService.createDirectory(data.get("name"), Integer.valueOf(data.get("parent_id")), data.get("username"), data.get("language"));
    }
}
