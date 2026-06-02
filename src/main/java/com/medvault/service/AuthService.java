package com.medvault.service;

import com.medvault.dto.request.LoginRequest;
import com.medvault.dto.request.RegisterRequest;
import com.medvault.dto.request.ResetPasswordRequest;
import com.medvault.dto.response.ApiResponse;
import com.medvault.dto.response.LoginResponse;
import com.medvault.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    ApiResponse<String> register(RegisterRequest req,
                                  MultipartFile profilePhoto,
                                  MultipartFile idCard,
                                  MultipartFile degreeCert,
                                  MultipartFile licenceCard);

    LoginResponse login(LoginRequest req);

    ApiResponse<String> resetPassword(Long userId, ResetPasswordRequest req);

    String generateTempPassword(User user);   // ← added
}