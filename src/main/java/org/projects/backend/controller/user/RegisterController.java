package org.projects.backend.controller.user;

import org.projects.backend.service.user.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RegisterController {
    @Autowired
    private RegisterService registerService;

//    因为传过来的是封装为data的数据，这是一个map数据结构，所以从前端获取及返回前端的数据均应为map
//    不过如果单独想收到一个jason，应该使用@RequestBody
    @PostMapping("/api/user/register/")
    public Map<String, String> register(@RequestParam Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        String confirmedPassword  = map.get("confirmedPassword");
        return registerService.registerAccount(username, password, confirmedPassword);
    }

}
