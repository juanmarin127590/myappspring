package com.DTOs;

import jakarta.annotation.Nonnull;
import lombok.Data;


@Data
public class LoginRequest {
    
    @Nonnull
    private String email;
    
    @Nonnull
    private String password;
}
