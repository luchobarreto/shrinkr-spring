package com.shrinkr.controller;

import com.shrinkr.entity.Url;
import com.shrinkr.entity.User;
import com.shrinkr.service.AuthService;
import com.shrinkr.service.UrlService;
import com.shrinkr.service.UserDetailsImpl;
import com.shrinkr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    UrlController(UrlService urlService, UserService userService, AuthService authService) {
        this.urlService = urlService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping()
    public ResponseEntity<Map<String, List<Url>>> getUrlsByUser(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Url> urls = urlService.getUrls(authService.getUserIdBySession(), page, size);
        Map<String, List<Url>> response = new HashMap<>();
        response.put("urls", urls.getContent());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{shortId}")
    public ResponseEntity<Url> getUrlByShortId(@PathVariable("shortId") String shortId) {
        Url url = urlService.getUrlByShortId(shortId);
        return ResponseEntity.status(HttpStatus.OK).body(url);
    }

    @PostMapping()
    public ResponseEntity<Void> createUrl(@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody Url url) {
        User user = userService.getUser(userDetails.getId());
        URI location = urlService.createUrl(user, url);
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editUrl(@PathVariable Long id, @Valid @RequestBody Url url) {
        Url newUrl = urlService.getUrlById(id);
        newUrl.setUrl(url.getUrl());
        urlService.updateUrl(id, newUrl, authService.getUserIdBySession());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        Url url = urlService.getUrlById(id);
        urlService.deleteUrl(url, authService.getUserIdBySession());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
