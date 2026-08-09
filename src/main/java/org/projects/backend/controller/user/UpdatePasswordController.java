package org.projects.backend.controller.user;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.user.UpdatePassword;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UpdatePasswordController {
    @Autowired
    private UpdatePassword updatePassword;

    @PostMapping("/api/user/update/password/")
    public JSONObject updatePassword(@RequestParam Map<String, String> data) {
        return updatePassword.updatePassword(data.get("cur_password"), data.get("password"), data.get("confirmedPassword"), data.get("language") == null ? LanguagesSelector.en_US : data.get("language"));
    }
}
