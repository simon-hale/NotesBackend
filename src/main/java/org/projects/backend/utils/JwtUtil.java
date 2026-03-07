package org.projects.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

//    本类的作用是生成jwt令牌并实现解析userid
//    本类不实现令牌验证
//    原理是将一个字符串加上一个密钥再加上有效期，变成一个加密之后的字符串

@Component
public class JwtUtil {
//    准备前置数据，用于创建令牌
//    生存时间，默认为14天
    public static final long JWT_TTL = 60 * 60 * 1000L * 24 * 14;  // 有效期14天
//    自定义的一个随机密钥，用来生成jwt令随牌，可以自行定义，但一定要随机
    public static final String JWT_KEY = "SDFGjhdsfalshdfHFdsjkdsfds121232131afasdfac";
//    获得去掉'-'字符的UUID，唯一标志jwt，作为token的一个组成部分
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

//    创建一个jwt令牌，作为供其他对象调用的最上层函数，其再调用getJwtBuilder函数执行具体的创建过程
    public static String createJWT(String subject) {
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID());
        return builder.compact();
    }
//    用于构建令牌的具体实现过程
    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
//        设置签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();  // 对密钥处理，得到一个可以用于签名的密钥
//        处理并获取时间信息，用于生成令牌
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }

//        构建jwt令牌
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)
                .setSubject(subject)
                .setIssuer("sg")
                .setIssuedAt(now)
                .signWith(signatureAlgorithm, secretKey)
                .setExpiration(expDate);
    }

//    对密钥JWT_KEY进行处理
    public static SecretKey generalKey() {
        byte[] encodeKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        return new SecretKeySpec(encodeKey, 0, encodeKey.length, "HmacSHA256");
    }

//    根据令牌解析userid
    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}

//    原理顺序：
//    1. 前三行：准备数据
//    2. 第一个函数：共外界调用，其调用第二个函数生成令牌
//    3. 第二个函数：先处理数据，再构建令牌，返回给第一个函数，再返回给外界对象

//    构建jwt使用方法：
//    1. 引入此类
//    2. 修改前两行
//    3. 其他对象调用createJWT函数返回得到jwt令牌

//    解析jwt使用方法：
//    1. 引入此类
//    2. 其他对象调用parseJWT函数返回得到jwt令牌