package org.projects.backend.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.User;
import org.projects.backend.service.user.RegisterService;
import org.projects.backend.utils.LanguagesSelector;
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
    public Map<String, String> registerAccount(String username, String password, String confirmedPassword, String language) {
        Map<String, String> map = new HashMap<>();
//        if (username == null || password == null || confirmedPassword == null) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "请输入用户名和密码。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Please enter a username and password.");
//            }
//            return map;
//        }
//
//        username = username.trim();  // 删除首尾空白符
//        if (username.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "用户名和密码不能为空。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Username and password cannot be empty.");
//            }
//            return map;
//        }
//
//        if (username.length() > 100) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "用户名长度不能超过100个字符。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Username cannot exceed 100 characters.");
//            }
//            return map;
//        }
//
//        if (password.length() > 100 || confirmedPassword.length() > 100) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "密码长度不能超过100个字符。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Password cannot exceed 100 characters.");
//            }
//            return map;
//        }
//
//        if (!password.equals(confirmedPassword)) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "两次输入的密码不一致。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "The passwords do not match.");
//            }
//            return map;
//        }
//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("username", username);
//        List<User> users;
//        try {
//            users = userMapper.selectList(queryWrapper);
//        } catch (Exception e) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "数据库查询出错");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Database query error.");
//            }
//            return map;
//        }
//        if (!users.isEmpty()) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "该用户名已存在。");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "This username already exists.");
//            }
//            return map;
//        }
//
////        对密码加密
//        String encodedPassword = passwordEncoder.encode(password);
//        User user = new User(null, username, encodedPassword);  // 主键自增可仅传入空值
//        int result_insert_user;
//        try {
//            result_insert_user = userMapper.insert(user);
//        } catch (Exception e) {
//            switch (language) {
//                case LanguagesSelector.zh_CN:
//                    map.put("error_message", "数据库用户插入出错");
//                    break;
//                case LanguagesSelector.en_US:
//                default:
//                    map.put("error_message", "Database user insert error.");
//            }
//            return map;
//        }
//        if (result_insert_user != 1) {
//            switch (language) {
//                case LanguagesSelector.zh_CN: map.put("error_message", "数据库用户插入返回值非1"); break;
//                case LanguagesSelector.en_US:
//                default: map.put("error_message", "Database user insert returned a non-1 value.");
//            }
//            return map;
//        }
//
//        Directory directory = new Directory();
//        directory.setName("root");
//        directory.setUserId(user.getId());
//        Directory rootParent;
//        try {
//            rootParent = directoryMapper.selectOne(new QueryWrapper<Directory>().eq("name", "root_parent"));
//        } catch (Exception e) {
//            boolean clean = cleanUserAfterError(user.getId());
//            String resp_zh_CN, resp_en_US;
//            if (clean) {
//                resp_zh_CN = "数据库根父目录查询出错，已有用户表项已清理";
//                resp_en_US = "Database root_parent query error, with existing user table entries cleared.";
//            } else {
//                resp_zh_CN = "数据库根父目录查询出错，但已有用户表项清理失败";
//                resp_en_US = "Database root_parent query error, but clearing the existing user table entries failed.";
//            }
//            switch (language) {
//                case LanguagesSelector.zh_CN: map.put("error_message", resp_zh_CN); break;
//                case LanguagesSelector.en_US:
//                default: map.put("error_message", resp_en_US);
//            }
//            return map;
//        }
//        if (rootParent == null) {
//            boolean clean = cleanUserAfterError(user.getId());
//            String resp_zh_CN, resp_en_US;
//            if (clean) {
//                resp_zh_CN = "数据库根父目录查询结果为空，已有用户表项已清理";
//                resp_en_US = "Database root_parent query result is null, with existing user table entries cleared.";
//            } else {
//                resp_zh_CN = "数据库根父目录查询结果为空，但已有用户表项清理失败";
//                resp_en_US = "Database root_parent query result is null, but clearing the existing user table entries failed.";
//            }
//            switch (language) {
//                case LanguagesSelector.zh_CN: map.put("error_message", resp_zh_CN); break;
//                case LanguagesSelector.en_US:
//                default: map.put("error_message", resp_en_US);
//            }
//            return map;
//        }
//        directory.setParentId(rootParent.getId());
//        int result_insert_directory;
//        try {
//            result_insert_directory = directoryMapper.insert(directory);
//        } catch (Exception e) {
//            boolean clean = cleanUserAfterError(user.getId());
//            String resp_zh_CN, resp_en_US;
//            if (clean) {
//                resp_zh_CN = "数据库根目录插入出错，已有用户表项已清理";
//                resp_en_US = "Database root directory insert error, with existing user table entries cleared.";
//            } else {
//                resp_zh_CN = "数据库根目录插入出错，但已有用户表项清理失败";
//                resp_en_US = "Database root directory insert error, but clearing the existing user table entries failed.";
//            }
//            switch (language) {
//                case LanguagesSelector.zh_CN: map.put("error_message", resp_zh_CN); break;
//                case LanguagesSelector.en_US:
//                default: map.put("error_message", resp_en_US);
//            }
//            return map;
//        }
//        if (result_insert_directory != 1) {
//            boolean clean = cleanUserAfterError(user.getId());
//            String resp_zh_CN, resp_en_US;
//            if (clean) {
//                resp_zh_CN = "数据库根目录插入返回值非1，已有用户表项已清理";
//                resp_en_US = "Database root directory insert returned a non-1 value, with existing user table entries cleared.";
//            } else {
//                resp_zh_CN = "数据库根目录插入返回值非1，但已有用户表项清理失败";
//                resp_en_US = "Database root directory insert returned a non-1 value, but clearing the existing user table entries failed.";
//            }
//            switch (language) {
//                case LanguagesSelector.zh_CN: map.put("error_message", resp_zh_CN); break;
//                case LanguagesSelector.en_US:
//                default: map.put("error_message", resp_en_US);
//            }
//            return map;
//        }
//
//        map.put("error_message", "success");

        switch (language) {
            case LanguagesSelector.zh_CN: map.put("error_message", "本站暂不开放注册！"); break;
            case LanguagesSelector.en_US:
            default: map.put("error_message", "Registration is temporarily closed.");
        }
        return map;
    }

    private boolean cleanUserAfterError(Integer userId) {
        int delete;
        try {
            delete = userMapper.deleteById(userId);
        } catch (Exception e) {
            return false;
        }
        return delete == 1;
    }
}
