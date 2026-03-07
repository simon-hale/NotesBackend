package org.projects.backend.service.impl.user;

import org.projects.backend.pojo.User;
import org.projects.backend.service.impl.UserDetailsImpl;
import org.projects.backend.service.user.InfoService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

//    解析Token的具体过程，过程细节是已经定义好的api，背过熟练应用即可

@Service
public class InfoServiceImpl implements InfoService {
    @Override
    public Map<String, String> getInfo() {
//        前三行用于在jwt之下的对受限域名访问,直接记
//        受限域名的访问使用令牌访问,因此这几行是对令牌的验证,如果验证成功则返回数据并继续执行

        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = loginUser.getUser();

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("id", user.getId().toString());
        map.put("username", user.getUsername());
        return map;
    }
}
