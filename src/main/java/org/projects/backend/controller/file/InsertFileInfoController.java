package org.projects.backend.controller.file;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.file.InsertFileInfoService;
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
        String username = data.get("username");
        String stringOfPath = data.get("string_of_path");
        String fileName = data.get("filename");
        Integer parentId = Integer.valueOf(data.get("parent_id"));
        return insertFileInfoService.insertFileInfo(username, stringOfPath, fileName, parentId);
    }

}
