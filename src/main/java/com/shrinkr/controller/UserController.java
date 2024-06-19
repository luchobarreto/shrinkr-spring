package com.shrinkr.controller;

import com.shrinkr.entity.User;
import com.shrinkr.dto.request.CreateUserRequest;
import com.shrinkr.dto.request.EditUserRequest;
import com.shrinkr.security.JwtUtils;
import com.shrinkr.service.AuthService;
import com.shrinkr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    UserController(UserService userService, AuthService authService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<User> getUserBySession() {
        User user = userService.getUser(authService.getUserIdBySession());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        if(user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request){
        User user = new User(request.getEmail(), request.getPassword(), request.getName());

        URI location = userService.createUser(user);

        return ResponseEntity.created(location).build();
    }

    @PatchMapping()
    public ResponseEntity<Void> editUser(@Valid @RequestBody EditUserRequest request) {
        userService.updateUser(authService.getUserIdBySession(), request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, String>> editUserImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = userService.updateUserImage(authService.getUserIdBySession(), file);
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        ResponseCookie cookie = authService.cleanCookies();
        userService.deleteUser(authService.getUserIdBySession());
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
