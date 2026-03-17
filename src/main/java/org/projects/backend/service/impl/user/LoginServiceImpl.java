package org.projects.backend.service.impl.user;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.User;
import org.projects.backend.service.impl.UserDetailsImpl;
import org.projects.backend.service.user.LoginService;
import org.projects.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//    实现获取Token的具体过程，过程细节是已经定义好的api，背过熟练应用即可

@Service
public class LoginServiceImpl implements LoginService {

//    用于将userid,username封装
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, String> getToken(String username, String password) {
//        前四行直接记,用于实现jwt之下的密码登录过程,  可以理解为对数据处理

//        先对userid,username进行封装
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
//        对userid,username认证
        Authentication authenticate = authenticationManager.authenticate(authentication);
//        认证成功,提取对象
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();
//        提取信息
        User user = loginUser.getUser();

        String jwt = JwtUtil.createJWT(user.getId().toString());
        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("token", jwt);
        return map;
    }

    @Override
    public Map<String, String> autoLogin() {
        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        return map;
    }

}
