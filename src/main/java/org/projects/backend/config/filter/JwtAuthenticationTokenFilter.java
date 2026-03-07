package org.projects.backend.config.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.User;
import org.projects.backend.service.impl.UserDetailsImpl;
import org.projects.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//    拦截http请求,获取其中jwt令牌,并验证jwt令牌，判断用户的合法性
//    如果用户非法,抛出异常,如果合法,解析用户信息

//    说明:令牌中不是有userid或者username吗?为什么还要解析
//    解答:令牌和身份信息是相互绑定的,是用来登陆的,仅包含必要的身份信息,但是程序并不只是使用身份信息,还要得知其他的各项信息
//    所以说,解析信息就是根据令牌中的身份信息(如果可以),就根据这个身份信息把其他信息从数据库中读出来,供外界使用

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
//    用于在数据库中查找数据的具体信息,需要根据具体项目细节修改
    @Autowired
    private UserMapper userMapper;

//    验证jwt令牌,并当jwt令牌合法时,解析用户信息
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

//        从前端的数据中的headers部分提取令牌
//        因为此处定义Authorization,所以前端headers中的字段名也是这个,并且需要"Bearer "+令牌
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

//        去掉多余部分"Bearer "仅保留纯令牌
        token = token.substring(7);

//        尝试解析令牌
//        如果解析成功,说明令牌有效,并直接提取用户信息
//        如果解析不成功则说明令牌非法,则直接排除异常,其他对象可根据异常得知jwt令牌非法
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        jwt解析成功,获取用户详细信息
        User user = userMapper.selectById(Integer.parseInt(userid));

//        如果找不到信息,说明虽然令牌和身份信息合法且相互绑定,但是信息没有注册到数据库中
        if (user == null) {
            throw new RuntimeException("用户名未注册");
        }

//        如果全部成功,返回用户信息,则放行当前令牌对应的用户,使其可以访问受限制的域名
        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}

//    注意:本类中的方法不会被项目中的新加类直接调用,被AuthenticationManager类对象间接调用
//    本类的作用是对访问受限域名的用户进行合法性验证,通过调用AuthenticationManager类方法简介调用本类实现验证
//    因此,如果想实现验证jwt的操作步骤:
//    1. 引入此类
//    2. 修正Mapper类为当前项目,一边数据库操作
//    3. 在受限域名的函数中使用AuthenticationManager来验证jwt令牌的合法性并获取用户信息,进而执行下一步操作