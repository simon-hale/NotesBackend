package org.projects.backend.controller.user;

import org.projects.backend.service.user.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/api/user/token/")
//    @RequestParam Map<String, String> map指将POST传过来的数据装进Map<String, String>类型的map变量中
    public Map<String, String> getToken(@RequestParam Map<String, String> map) {
//        map.get("username")值从前端传过来的数据中提取出username字段对应的值
        String username = map.get("username");
        String password = map.get("password");
        return loginService.getToken(username, password);
    }

}
