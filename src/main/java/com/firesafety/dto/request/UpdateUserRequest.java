package com.firesafety.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private Boolean enabled;

    private Set<String> roles;
}
