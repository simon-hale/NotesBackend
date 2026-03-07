package org.projects.backend.controller.user;

import org.projects.backend.service.user.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {
    @Autowired
    private InfoService infoService;

//    get请求获取token，前端使用
//    headers: {
//        Authorization:"Bearer "+"token",
//      },
//    来实现,即把token存入Authorization中,不过这个量不需要主动定义,其定义在JwtAuthenticationTokenFilter.java中
//    因此全程没有参数传递,因为参数一定写好并且已经定义了传递方法,不需要手动写参数
//    只要前端写好headers就可以了
    @GetMapping("/api/user/info/")
    public Map<String, String> getInfo(){
        return infoService.getInfo();
    }
}
