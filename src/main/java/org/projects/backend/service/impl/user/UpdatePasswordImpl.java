package org.projects.backend.service.impl.user;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.User;
import org.projects.backend.service.user.UpdatePassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpdatePasswordImpl implements UpdatePassword {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public JSONObject updatePassword(String username, String password, String confirmedPassword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        List<User> user_list = userMapper.selectList(queryWrapper);
        JSONObject resp = new JSONObject();
        if(user_list.isEmpty()){
            resp.put("error_message", "该用户不存在");
            return resp;
        }

        if((password == null || password.isEmpty()) || (confirmedPassword == null || confirmedPassword.isEmpty())){
            resp.put("error_message", "密码不能为空");
            return resp;
        }

        if(!password.equals(confirmedPassword)){
            resp.put("error_message", "两次密码不一致");
            return resp;
        }

        String encodedPassword = passwordEncoder.encode(password);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", username).set("password", encodedPassword);
        int result = userMapper.update(updateWrapper);
        if(result > 0){
            resp.put("error_message", "success");
            return resp;
        }else {
            resp.put("error_message", "数据库错误，请联系管理员");
            return resp;
        }
    }
}
