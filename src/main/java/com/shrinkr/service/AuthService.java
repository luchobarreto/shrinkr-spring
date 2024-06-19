package com.shrinkr.service;

import org.springframework.http.ResponseCookie;

public interface AuthService {
    ResponseCookie getJwtCookie(String email, String password);
    public String encodePassword(String password);
    ResponseCookie cleanCookies();
    Long getUserIdBySession();
    void updatePassword(Long id, String password, String newPassword);
}
