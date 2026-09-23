package org.projects.backend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.projects.backend.config.filter.JwtAuthenticationTokenFilter;
import org.projects.backend.utils.AuthCookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(
            @Value("${app.auth.cookie-secure:true}")
            boolean cookieSecure,
            @Value("${app.auth.cookie-same-site:Strict}")
            String cookieSameSite) {

        CookieCsrfTokenRepository repository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieCustomizer(builder -> builder
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/"));

        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository)
            throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(
                                new SpaCsrfTokenRequestHandler())
                        .requireCsrfProtectionMatcher(
                                SecurityConfig::requiresCsrfProtection))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/token/",
                                "/api/user/login/",
                                "/api/user/register/",
                                "/api/user/csrf/")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS)
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        /*
         * JWT 必须先于 CsrfFilter 验证。
         *
         * Cookie JWT 无效：
         *      先返回 401
         *
         * Cookie JWT 有效：
         *      再执行 CSRF 检查
         *
         * Bearer：
         *      JWT 正常验证，但 CSRF matcher 会跳过。
         */
        http.addFilterBefore(
                jwtAuthenticationTokenFilter,
                CsrfFilter.class
        );

        return http.build();
    }

    private static boolean requiresCsrfProtection(
            HttpServletRequest request) {

        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "TRACE".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        String path = request.getServletPath();

        /* Native token login、registration 和只读 auto-login
         * 不使用浏览器 Cookie CSRF。
         */
        if ("/api/user/token/".equals(path)
                || "/api/user/register/".equals(path)
                || "/api/user/auto-login/".equals(path)) {
            return false;
        }

        /*
         * Web Cookie 登录本身需要防止 Login CSRF。
         */
        if ("/api/user/login/".equals(path)) {
            return true;
        }

        /*
         * Android / 原生客户端显式发送 Bearer。
         * 浏览器不会自动附加 Authorization Bearer，
         * 因此不属于 Cookie CSRF 场景。
         */
        String authorization =
                request.getHeader("Authorization");

        if (StringUtils.hasText(authorization)
                && authorization.startsWith("Bearer ")) {
            return false;
        }

        return hasCookie(
                request,
                AuthCookieUtil.AUTH_COOKIE_NAME
        );
    }

    private static boolean hasCookie(
            HttpServletRequest request,
            String cookieName) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) return false;

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return true;
            }
        }

        return false;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }
}