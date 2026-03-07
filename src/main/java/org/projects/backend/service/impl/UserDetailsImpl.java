package org.projects.backend.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.backend.pojo.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// 这个类的功能是从上层得到一个User对象，供登录模块调用其方法来判断用户是否合法来确认登录

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

//    是否过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // （正常用户的默认值为false）
    }

//    用户是否被启用
    @Override
    public boolean isEnabled() {
        return true;  // return修改为true（正常用户的默认值）
    }
}
