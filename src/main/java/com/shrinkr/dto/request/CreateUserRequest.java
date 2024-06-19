package com.shrinkr.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {
    @NotBlank
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank
    @Size(min = 6, max = 30, message = "Password should be between 6 and 30 characters long.")
    private String password;

    @NotBlank
    @Size(min = 3, max = 30, message = "Name should be between 3 and 30 characters long.")
    private String name;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
