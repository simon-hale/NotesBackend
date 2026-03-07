package org.projects.backend.config;

//    此类用于①设置密码加密方法②在解决了跨域的情况下,控制允许直接访问的域名
//    注意:本类不直接实现密码加密,具体的加密函数已给出,直接调用
//    spring-boot-starter-security类会将前端输入的密码与后端进行判定
//    但是默认判定加密之后的密码,会调用SecurityConfig的passwordEncoder并得到一个加密方法PasswordEncoder
//    因此,本类的作用就是提供一个SecurityConfig类的passwordEncoder方法并返回一个加密方法

//    此类不被任何类调用
//    使用方法:
//    1. 引入此类,通过passwordEncoder函数设置加密方法(不用变动)
//    2. 其他类中直接注入PasswordEncoder类,定义其对象passwordEncoder,调用passwordEncoder.encode(password)函数即可对密码加密

import org.projects.backend.config.filter.JwtAuthenticationTokenFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

//    给出加密方法
//    指定BCrypt加密算法，封装为函数，使用这个类的方法对字符串加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    在解决了跨域的情况下,配置允许直接访问的域名
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        下行表示放行给未通过验证的用户的域名
                        .requestMatchers(
                                "/api/user/token/",
                                "/api/user/register/").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }
}