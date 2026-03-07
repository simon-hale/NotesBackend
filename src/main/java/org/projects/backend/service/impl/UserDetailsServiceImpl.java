package org.projects.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//  这个类的作用是重写spring-boot-starter-security的默认登录函数loadUserByUsername
//  security类登录账户的原理是将输入的username在数据库中查找到对应的账户数据存进当前类的User对象中
//  然后调用当前类的loadUserByUsername方法，传入User对象并得到一个UserDetails对象
//  此时，UserDetails对象中存着当前前端请求登录的User信息
//  然后调用UserDetails的类方法，判断其得到的User对象是否合法
//  如果合法则成功登录

//  spring-boot-starter-security默认的账户是user和一个随机密码
//  重写之后，要支持存在数据库中的用户账户的登录
//  也就是要能够把接收到的username从数据库中找到并封装进User对象
//  因此，此类需要UserMapper访问数据库
//  此外，由于loadUserByUsername函数返回的不是简单数据，而是UserDetails接口
//  但UserDetails仅是一个接口，因此，还要实现UserDetailsImpl类
//  此类中获得的账户名传进UserDetailsImpl类获得用户的详情，就可以判断用户合法性

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
//            如果没找到user，抛出异常
            throw new RuntimeException("用户不存在");
        } else {
//            这个函数调用是lombok生成的有参构造函数
//            UserDetailsImpl(final User user) {this.user = user;}
//            因为SpringSecurity不能直接处理User，所以需要再次封装为指定类
//            通过这个函数将user传进去，封装得到符合SpringSecurity规范的用户信息类UserDetails
//            然后SpringSecurity再根据用户信息处理是否允许登录，并处理其他后序操作
            return new UserDetailsImpl(user);
        }
    }
}
