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
import org.projects.backend.utils.AuthCookieUtil;
import org.projects.backend.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter
        extends OncePerRequestFilter {

    private final UserMapper userMapper;
    private final AuthCookieUtil authCookieUtil;

    public JwtAuthenticationTokenFilter(
            UserMapper userMapper,
            AuthCookieUtil authCookieUtil) {

        this.userMapper = userMapper;
        this.authCookieUtil = authCookieUtil;
    }

    @Override
    protected boolean shouldNotFilter(
            @NotNull HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        /*
         * 登录接口必须忽略已有 Cookie。
         *
         * 否则浏览器如果保存了一个已经过期的 Cookie，
         * 用户连重新登录接口都无法调用。
         */
        return "/api/user/token/".equals(path)
                || "/api/user/login/".equals(path)
                || "/api/user/register/".equals(path)
                || "/api/user/csrf/".equals(path);
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        String token;
        boolean cookieAuthenticated = false;

        /*
         * Bearer 优先。
         *
         * 如果客户端明确提供 Bearer，但 Bearer 无效，
         * 不允许自动 fallback 到 Cookie。
         */
        if (StringUtils.hasText(authorization)
                && authorization.startsWith("Bearer ")) {

            token = authorization.substring(7);

        } else {

            token = authCookieUtil.getAuthToken(request);
            cookieAuthenticated = StringUtils.hasText(token);
        }

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        int userId;
        Integer tokenVersion;

        try {

            Claims claims = JwtUtil.parseJWT(token);

            userId = Integer.parseInt(
                    claims.getSubject()
            );

            tokenVersion = claims.get(
                    JwtUtil.TOKEN_VERSION_CLAIM,
                    Integer.class
            );

        } catch (Exception e) {

            rejectUnauthorized(
                    response,
                    cookieAuthenticated
            );

            return;
        }

        User user = userMapper.selectById(userId);

        if (user == null
                || tokenVersion == null
                || !Objects.equals(
                tokenVersion,
                user.getTokenVersion())) {

            rejectUnauthorized(
                    response,
                    cookieAuthenticated
            );

            return;
        }

        UserDetailsImpl loginUser =
                new UserDetailsImpl(user);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginUser,
                        null,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private void rejectUnauthorized(
            HttpServletResponse response,
            boolean cookieAuthenticated)
            throws IOException {

        SecurityContextHolder.clearContext();

        /*
         * 如果坏掉的是 Web Cookie，
         * 顺便让浏览器删除失效 Cookie。
         *
         * Android Bearer 失败则不会操作 Cookie。
         */
        if (cookieAuthenticated) {
            authCookieUtil.clearSessionCookies(response);
        }

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                "{\"error_message\":\"Unauthorized\"}"
        );
    }
}