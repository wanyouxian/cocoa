package com.rocky.cocoa.server.jwt;

import com.rocky.cocoa.entity.system.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtManager {
    private final static String JWT_SECRET_KEY = "cocoa@rocky.com";

    //生成jwt token
    public static String createJwt(long ttlMillis, User user) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getName());
        claims.put("password", user.getPwd());

        String subject = user.getName();

        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setSubject(subject)
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY);

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            jwtBuilder.setExpiration(exp);
        }
        return jwtBuilder.compact();
    }

    //解析jwt token

    public static Claims parseJwt(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY)
                .parseClaimsJws(token).getBody();
        return claims;
    }

    //验证jwt token

    public static Boolean isVerify(String token, String pwd) {

        Claims claims = parseJwt(token);
        return claims.get("password").equals(pwd);
    }
}
