package org.projects.backend.service.user;

import com.alibaba.fastjson2.JSONObject;

public interface UpdatePassword {
    JSONObject updatePassword(String curPassword, String password, String confirmedPassword, String language);
}
