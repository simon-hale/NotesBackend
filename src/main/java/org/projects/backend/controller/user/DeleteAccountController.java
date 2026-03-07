package org.projects.backend.controller.user;

import com.alibaba.fastjson2.JSONObject;
import org.projects.backend.service.user.DeleteAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeleteAccountController {
    @Autowired
    private DeleteAccount deleteAccount;

    @PostMapping("/api/user/delete/")
    public JSONObject deleteAccount(@RequestParam Map<String, String> data) {
        String username = data.get("username");
        return deleteAccount.deleteAccount(username);
    }
}
