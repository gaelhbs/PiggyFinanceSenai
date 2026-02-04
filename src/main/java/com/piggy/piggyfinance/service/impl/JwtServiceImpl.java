package com.piggy.piggyfinance.service.impl;

import com.piggy.piggyfinance.model.User;
import com.piggy.piggyfinance.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private final String SECRET = "SADWADWA#@@ASFASFAFASFASG#%$@TWAEGASGFAsfafasfasfawdfawdfawdf";
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
