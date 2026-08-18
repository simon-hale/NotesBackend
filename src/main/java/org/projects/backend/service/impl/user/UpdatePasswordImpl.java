package org.projects.backend.service.impl.user;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.User;
import org.projects.backend.service.user.UpdatePassword;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UpdatePasswordImpl implements UpdatePassword {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject updatePassword(String curPassword, String password, String confirmedPassword, String language) {
        JSONObject resp = new JSONObject();
        User user = accessTokenExtractor.getCurrentUser();
        Integer userId = user.getId();

        if (curPassword == null || curPassword.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "原密码不能为空"); break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "Original password cannot be empty.");
            }
            return resp;
        }

        if(password == null || password.isEmpty() || (confirmedPassword == null || confirmedPassword.isEmpty())){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "新密码不能为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "New password cannot be empty.");
            }
            return resp;
        }

        if (password.length() > 100 || confirmedPassword.length() > 100) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "密码长度不能超过100个字符。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put("error_message", "Password cannot exceed 100 characters.");
            }
            return resp;
        }

        if(!Objects.equals(password, confirmedPassword)){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "两次密码不一致"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "The two passwords do not match.");
            }
            return resp;
        }

        if(!passwordEncoder.matches(curPassword, user.getPassword())){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "原密码不正确"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Incorrect original password.");
            }
            return resp;
        }

        if(passwordEncoder.matches(password, user.getPassword())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "新旧密码不能相同"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "New password cannot be the same as old password.");
            }
            return resp;
        }

        String encodedPassword = passwordEncoder.encode(password);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("password", encodedPassword);
        int result;
        try {
            result = userMapper.update(updateWrapper);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库更新出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database update error.");
            }
            return resp;
        }
        if(result == 1){
            resp.put("error_message", "success");
        }else {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库错误，请联系管理员"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database error, please contact administrator.");
            }
        }
        return resp;
    }
}
