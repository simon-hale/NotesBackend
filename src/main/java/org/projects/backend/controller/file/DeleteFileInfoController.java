package org.projects.backend.controller.file;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.file.DeleteFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeleteFileInfoController {
    @Autowired
    private DeleteFileInfoService deleteFileInfoService;

    @PostMapping("/api/file/delete/")
    JSONObject deleteFileById(@RequestParam Map<String, String> data) {
        return deleteFileInfoService.deleteFileById(Integer.valueOf(data.get("id")), data.get("username"), data.get("language"));
    }
}
