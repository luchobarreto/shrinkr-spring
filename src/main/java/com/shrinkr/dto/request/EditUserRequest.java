package com.shrinkr.dto.request;

import jakarta.validation.constraints.Size;

public class EditUserRequest {
    @Size(min = 3, max = 30, message = "Name should be between 3 and 30 characters long.")
    private String name;

    public String getName() {
        return name;
    }
}
