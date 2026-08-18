package org.projects.backend.controller.directory;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.directory.GetDirectoryInfoService;
import org.projects.backend.service.file.GetFileInfoService;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GetDirectoryInfoController {
    @Autowired
    private GetDirectoryInfoService getDirectoryInfoService;

    @PostMapping("/api/directory/init/")
    JSONObject getDirectoryInfoInit(@RequestParam Map<String, String> data) {
        return getDirectoryInfoService.getAllDirectoryInfoByInit(data.get("language") == null ? LanguagesSelector.en_US : data.get("language"));
    }

    @PostMapping("/api/directory/id/")
    JSONObject getDirectoryInfoByParentId(@RequestParam Map<String, String> data) {
        return getDirectoryInfoService.getAllDirectoryInfoByParentId(data.get("parent_id"), data.get("language") == null ? LanguagesSelector.en_US : data.get("language"));
    }

}
