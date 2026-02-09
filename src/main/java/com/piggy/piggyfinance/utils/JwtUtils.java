package com.piggy.piggyfinance.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.UUID;

public class JwtUtils {

    private final String secret;

    public JwtUtils(String secret) {
        this.secret = secret;
    }

    public UUID getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.get("userId", String.class));
    }
}
