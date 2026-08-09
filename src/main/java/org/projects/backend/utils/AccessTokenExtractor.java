package org.projects.backend.utils;

import org.projects.backend.pojo.User;
import org.projects.backend.service.impl.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 从Spring Security上下文中获取当前JWT对应的用户。
 *
 * JWT已经由JwtAuthenticationTokenFilter完成解析和验证，
 * 本类不会重复读取请求头、解析JWT或查询数据库。
 */
@Component
public class AccessTokenExtractor {

    // 获取当前登录用户
    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserDetailsImpl loginUser)
                || loginUser.getUser() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "无法获取当前登录用户"
            );
        }

        return loginUser.getUser();
    }

    // 获取当前登录用户ID
    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // 获取当前登录用户名
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}