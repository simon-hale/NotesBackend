package org.projects.backend.controller.user;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.user.UpdatePassword;
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
        String username = data.get("username");
        String password = data.get("password");
        String confirmedPassword = data.get("confirmedPassword");
        return updatePassword.updatePassword(username, password, confirmedPassword);
    }
}
