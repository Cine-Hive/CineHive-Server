package com.example.CineHive.domain.account.service;

import com.example.CineHive.domain.account.dto.AccountInfoResponse;
import com.example.CineHive.domain.account.dto.UpdatePasswordRequest;
import com.example.CineHive.domain.account.dto.UpdatePreferencesRequest;
import com.example.CineHive.domain.account.dto.UpdateProfileRequest;

public interface AccountService {
    AccountInfoResponse getAccountInfo(String userEmail);
    void changePassword(String userEmail, UpdatePasswordRequest request);
    AccountInfoResponse updateProfile(String userEmail, UpdateProfileRequest request);
    AccountInfoResponse updatePreferences(String userEmail, UpdatePreferencesRequest request);
    void deleteAccount(String userEmail);
}