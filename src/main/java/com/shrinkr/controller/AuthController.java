package com.shrinkr.controller;

import com.shrinkr.entity.User;
import com.shrinkr.dto.request.SignInRequest;
import com.shrinkr.dto.request.SignUpRequest;
import com.shrinkr.dto.request.UpdatePasswordRequest;
import com.shrinkr.dto.response.MessageResponse;
import com.shrinkr.repository.UserRepository;
import com.shrinkr.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;

    @Autowired
    public AuthController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        ResponseCookie jwtCookie = authService.getJwtCookie(signInRequest.getEmail(), signInRequest.getPassword());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use"));
        }

        User newUser = new User(
                signUpRequest.getEmail(),
                authService.encodePassword(signUpRequest.getPassword()),
                signUpRequest.getName()
        );

        userRepository.save(newUser);

        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        ResponseCookie cookie = authService.cleanCookies();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("User signed out successfully"));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(authService.getUserIdBySession(), request.getPassword(), request.getNewPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
