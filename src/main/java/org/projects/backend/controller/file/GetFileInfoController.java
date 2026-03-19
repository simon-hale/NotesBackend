package org.projects.backend.controller.file;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.file.GetFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetFileInfoController {
    @Autowired
    private GetFileInfoService getFileInfoService;

    @PostMapping("/api/file/url/")
    JSONObject getFileURL(@RequestParam Map<String, String> data) {
        return getFileInfoService.getFileURL(Integer.valueOf(data.get("id")), data.get("username"), data.get("language"));
    }
}
