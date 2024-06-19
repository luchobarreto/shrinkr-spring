package com.shrinkr.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePasswordRequest {
    @NotBlank
    @Size(min = 6, message = "Password should be at least 6 characters long")
    private String password;

    @NotBlank
    @Size(min = 6, message = "Password should be at least 6 characters long")
    private String newPassword;

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
