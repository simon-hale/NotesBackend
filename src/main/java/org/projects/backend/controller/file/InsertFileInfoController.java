package org.projects.backend.controller.file;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.file.InsertFileInfoService;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InsertFileInfoController {
    @Autowired
    private InsertFileInfoService insertFileInfoService;

    @PostMapping("/api/file/insert/")
    JSONObject insertFileInfo(@RequestParam Map<String, String> data) {
        return insertFileInfoService.insertFileInfo(data.get("string_of_path"), data.get("filename"), data.get("parent_id"), data.get("language") == null ? LanguagesSelector.en_US : data.get("language"));
    }

}
