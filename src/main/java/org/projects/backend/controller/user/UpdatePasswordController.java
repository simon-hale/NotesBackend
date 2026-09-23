package org.projects.backend.controller.user;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletResponse;
import org.projects.backend.service.user.UpdatePassword;
import org.projects.backend.utils.AuthCookieUtil;
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

    @Autowired
    private AuthCookieUtil authCookieUtil;

    @PostMapping("/api/user/update/password/")
    public JSONObject updatePassword(
            @RequestParam Map<String, String> data,
            HttpServletResponse response) {

        JSONObject result =
                updatePassword.updatePassword(
                        data.get("cur_password"),
                        data.get("password"),
                        data.get("confirmedPassword"),
                        data.get("language") == null
                                ? LanguagesSelector.en_US
                                : data.get("language")
                );

        if ("success".equals(
                result.getString("error_message"))) {

            authCookieUtil.clearSessionCookies(
                    response
            );
        }

        return result;
    }
}