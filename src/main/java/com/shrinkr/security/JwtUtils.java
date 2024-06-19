package com.shrinkr.security;

import com.shrinkr.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${shrinkr.app.jwtSecret}")
    private String jwtSecret;

    @Value("${shrinkr.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${shrinkr.app.jwtCookieName}")
    private String jwtCookieName;

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if(cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl principal) {
        String jwt = generateCookieFromEmail(principal.getUsername(), principal.getId());
        return ResponseCookie.from(jwtCookieName, jwt)
                .path("/").maxAge(24 * 60 * 60).httpOnly(true).build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookieName, null).maxAge(0).path("/").build();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public Long getIdFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    public String generateCookieFromEmail(String email, Long id) {
        Map<String, Long> claims = new HashMap<>();
        claims.put("id", id);
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .claims(claims)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token Expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token Unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
