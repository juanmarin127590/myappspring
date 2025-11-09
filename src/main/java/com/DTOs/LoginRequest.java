package com.DTOs;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class LoginRequest {
    
    @Nonnull
    private String email;
    
    @Nonnull
    private String password;
}
