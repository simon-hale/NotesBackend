package org.projects.backend.controller.directory;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.directory.DeleteDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeleteDirectoryInfoController {
    @Autowired
    private DeleteDirectoryInfoService deleteDirectoryInfoService;

    @PostMapping("/api/directory/delete/")
    JSONObject deleteDirectoryById(@RequestParam Map<String, String> data) {
        return deleteDirectoryInfoService.deleteDirectoryById(Integer.valueOf(data.get("id")));
    }
}
