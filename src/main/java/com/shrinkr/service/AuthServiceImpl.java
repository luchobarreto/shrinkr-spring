package com.shrinkr.service;

import com.shrinkr.entity.User;
import com.shrinkr.repository.UserRepository;
import com.shrinkr.security.CustomAuthenticationProvider;
import com.shrinkr.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomAuthenticationProvider authenticationManager;
    private final HttpServletRequest servletRequest;
    private final UserRepository userRepository;

    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtUtils jwtUtils, CustomAuthenticationProvider authenticationManager, HttpServletRequest servletRequest, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.servletRequest = servletRequest;
        this.userRepository = userRepository;
    }

    public ResponseCookie getJwtCookie(String email, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = getUserDetails(authentication);

        return jwtUtils.generateJwtCookie(userDetails);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public ResponseCookie cleanCookies() {
        return jwtUtils.getCleanJwtCookie();
    }

    public Long getUserIdBySession() {
        try {
            return jwtUtils.getIdFromJwtToken(jwtUtils.getJwtFromCookies(servletRequest));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
    }

    @PreAuthorize("#id == principal.id")
    public void updatePassword(Long id, String password, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id: " + id + " does not exist."));

        boolean isValid = passwordEncoder.matches(password, user.getPassword());

        if(isValid) {
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passwords does not match.");
        }
    }

    private UserDetailsImpl getUserDetails(Authentication authentication) {
        return (UserDetailsImpl) authentication.getPrincipal();
    }
}
