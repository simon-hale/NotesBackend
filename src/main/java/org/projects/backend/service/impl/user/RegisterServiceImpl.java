package org.projects.backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.User;
import org.projects.backend.service.user.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DirectoryMapper directoryMapper;

//    注入密码加密类,实现密码的加密,此类不再任何新加类中,直接注入使用即可
//    只是加密算法由SecurityConfig类给出,但不直接调用此类
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> registerAccount(String username, String password, String confirmedPassword) {
        Map<String, String> map = new HashMap<>();
//        if(username == null || password == null || confirmedPassword == null) {
//            map.put("error_message", "请输入用户名或密码");
//            return map;
//        }
//
//        username = username.trim();  // 删除首位空白符
//        if(username.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {
//            map.put("error_message", "用户名和密码不能为空");
//            return map;
//        }
//
//        if(username.length() > 100) {
//            map.put("error_message", "用户名长度不能大于100");
//            return map;
//        }
//
//        if(password.length() > 100 || confirmedPassword.length() > 100) {
//            map.put("error_message", "密码长度不能大于100");
//            return map;
//        }
//
//        if(!password.equals(confirmedPassword)) {
//            map.put("error_message", "两次密码不一致");
//            return map;
//        }
//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("username", username);
//        List<User> users = userMapper.selectList(queryWrapper);
//        if(!users.isEmpty()) {
//            map.put("error_message", "用户名已存在");
//            return map;
//        }
//
////        对密码加密
//        String encodedPassword = passwordEncoder.encode(password);
//        User user = new User(null, username, encodedPassword);  // 主键自增可仅传入空值
//        userMapper.insert(user);
//
//        Directory directory = new Directory();
//        directory.setName("root");
//        directory.setUserId(userMapper.selectOne(new QueryWrapper<User>().eq("username", username)).getId());
//        directory.setParentId(directoryMapper.selectOne(new QueryWrapper<Directory>().eq("name", "root_parent")).getId());
//        directoryMapper.insert(directory);
//
//        map.put("error_message", "success");

        map.put("error_message", "本站暂不开放注册！");

        return map;
    }
}
