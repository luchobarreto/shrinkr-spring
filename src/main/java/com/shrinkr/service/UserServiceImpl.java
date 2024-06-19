package com.shrinkr.service;

import com.shrinkr.entity.User;
import com.shrinkr.dto.request.EditUserRequest;
import com.shrinkr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Service
public class UserServiceImpl implements UserService {

    private static final String URL = "/users";
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FileService fileService;

    @Autowired
    UserServiceImpl(UserRepository userRepository, AuthService authService, FileService fileService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.fileService = fileService;
    }

    public URI createUser(User user) {
        try {
            user.setPassword(authService.encodePassword(user.getPassword()));
            User newUser = userRepository.save(user);

            return ServletUriComponentsBuilder
                    .fromCurrentContextPath().path(URL + "/{id}")
                    .buildAndExpand(newUser.getId()).toUri();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email: " + user.getEmail() + " already exists.");
        }
    }

    @PreAuthorize("#id == principal.id")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id: " + id + " does not exist."));
    }

    @PreAuthorize("#id == principal.id")
    public void updateUser(Long id, EditUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id: " + id + " does not exist."));
        user.setName(request.getName());

        userRepository.save(user);
    }

    @PreAuthorize("#id == principal.id")
    public String updateUserImage(Long id, MultipartFile file) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id: " + id + " does not exist."));

        if(user.getPhotoUrl() != null) {
            fileService.deleteFile(user.getPhotoUrl());
        }

        if(file != null) {
            user.setPhotoUrl(fileService.uploadFile(file));
            userRepository.save(user);
            return user.getPhotoUrl();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image is null.");
        }
    }

    @PreAuthorize("#id == principal.id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id: " + id + " does not exist."));
        userRepository.deleteById(user.getId());
    }
}
