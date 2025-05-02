package com.example.CineHive.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemPasswordRequest {
    private String oldPassword;
    private String newPassword;
}
