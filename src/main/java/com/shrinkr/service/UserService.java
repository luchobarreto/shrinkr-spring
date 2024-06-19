package com.shrinkr.service;

import com.shrinkr.entity.User;
import com.shrinkr.dto.request.EditUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

public interface UserService {
    URI createUser(User user);
    User getUser(Long id);
    void updateUser(Long id, EditUserRequest request);

    String updateUserImage(Long id, MultipartFile file);
    void deleteUser(Long id);
}
