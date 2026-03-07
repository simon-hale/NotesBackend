package org.projects.backend.controller.directory;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.directory.UpdateDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdateDirectoryInfoController {
    @Autowired
    private UpdateDirectoryInfoService updateDirectoryInfoService;

    @PostMapping("/api/directory/modify/name/")
    JSONObject modifyDirectoryNameById(@RequestParam Map<String, String> data) {
        return updateDirectoryInfoService.modifyDirectoryNameById(Integer.valueOf(data.get("id")), data.get("name"));
    }
}
