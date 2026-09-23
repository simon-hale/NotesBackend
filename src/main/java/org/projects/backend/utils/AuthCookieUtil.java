package org.projects.backend.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Component
public class AuthCookieUtil {

    public static final String AUTH_COOKIE_NAME = "notes-auth";
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthCookieUtil(
            @Value("${app.auth.cookie-secure:true}") boolean cookieSecure,
            @Value("${app.auth.cookie-same-site:Strict}") String cookieSameSite) {
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = StringUtils.hasText(cookieSameSite)
                ? cookieSameSite
                : "Strict";
    }

    public String getAuthToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public void addAuthCookie(
            HttpServletResponse response,
            String token,
            boolean rememberMe) {

        ResponseCookie.ResponseCookieBuilder builder =
                ResponseCookie.from(AUTH_COOKIE_NAME, token)
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite(cookieSameSite)
                        .path("/");

        if (rememberMe) {
            builder.maxAge(Duration.ofMillis(JwtUtil.JWT_TTL));
        }

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                builder.build().toString()
        );
    }

    public void clearSessionCookies(HttpServletResponse response) {
        expireCookie(response, AUTH_COOKIE_NAME, true);
        expireCookie(response, CSRF_COOKIE_NAME, false);
    }

    private void expireCookie(
            HttpServletResponse response,
            String name,
            boolean httpOnly) {

        ResponseCookie cookie =
                ResponseCookie.from(name, "")
                        .httpOnly(httpOnly)
                        .secure(cookieSecure)
                        .sameSite(cookieSameSite)
                        .path("/")
                        .maxAge(Duration.ZERO)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}