package org.projects.backend.service.user;

import com.alibaba.fastjson2.JSONObject;

public interface DeleteAccount {
    JSONObject deleteAccount(String username);
}
