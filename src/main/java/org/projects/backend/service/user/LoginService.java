package org.projects.backend.service.user;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

//    登录就是得到一个临时通行证以便访问特殊功能
//    所以此类就是一个获取Token的方法，调用此方法来获得一个Token

public interface LoginService {
    Map<String, String> getToken(String username, String password);
    Map<String, String> autoLogin();
}
