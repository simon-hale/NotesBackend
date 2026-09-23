package org.projects.backend.controller.user;

import jakarta.servlet.http.HttpServletResponse;
import org.projects.backend.service.user.LoginService;
import org.projects.backend.utils.AuthCookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private AuthCookieUtil authCookieUtil;

    /*
     * Android / Native API。
     *
     * 保持原接口、原响应结构不变。
     */
    @PostMapping("/api/user/token/")
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
        return loginService.getToken(map.get("username"), map.get("password"));
    }

    @GetMapping("/api/user/csrf/")
    public Map<String, String> csrf() {
        Map<String, String> result = new HashMap<>();
        result.put("error_message", "success");
        return result;
    }

    /*
     * Web 专用登录。
     *
     * JWT 只写入 HttpOnly Cookie，
     * JSON 中不再将 token 返回给 JavaScript。
     */
    @PostMapping("/api/user/login/")
    public Map<String, String> loginForWeb(
            @RequestParam Map<String, String> map,
            HttpServletResponse response) {

        Map<String, String> result =
                loginService.getToken(
                        map.get("username"),
                        map.get("password")
                );

        if (!"success".equals(
                result.get("error_message"))) {

            result.remove("token");
            return result;
        }

        String token = result.remove("token");

        boolean rememberMe =
                Boolean.parseBoolean(
                        map.getOrDefault(
                                "remember_me",
                                "false"
                        )
                );

        authCookieUtil.addAuthCookie(
                response,
                token,
                rememberMe
        );

        result.put(
                "username",
                map.get("username")
        );

        return result;
    }

    @PostMapping("/api/user/auto-login/")
    public Map<String, String> tryLogin() {
        return loginService.autoLogin();
    }

    /*
     * 只退出当前 Web 浏览器。
     *
     * 不修改 tokenVersion，
     * 因此其他 Web / Android 设备不受影响。
     */
    @PostMapping("/api/user/logout/")
    public Map<String, String> logout(HttpServletResponse response) {
        authCookieUtil.clearSessionCookies(response);
        Map<String, String> result = new HashMap<>();
        result.put("error_message", "success");
        return result;
    }

    /*
     * 一键退出所有设备：
     * tokenVersion++，同时清除当前浏览器 Cookie。
     */
    @PostMapping("/api/user/logout-all/")
    public Map<String, String> logoutAll(HttpServletResponse response) {
        Map<String, String> result = loginService.logoutAll();
        if ("success".equals(result.get("error_message"))) authCookieUtil.clearSessionCookies(response);
        return result;
    }
}