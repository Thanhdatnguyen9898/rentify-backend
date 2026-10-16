package com.rentify.modules.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rentify.common.response.annotation.ApiMessage;
import com.rentify.modules.auth.dto.request.LoginRequest;
import com.rentify.modules.auth.dto.request.RegisterRequest;
import com.rentify.modules.auth.dto.response.AuthResponse;
import com.rentify.modules.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    @ApiMessage("Đăng ký tài khoản thành công")
    public ResponseEntity<AuthResponse.UserSummary> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse.UserSummary response = this.authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest request,HttpServletResponse response) {
        AuthResponse authResponse = this.authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản thành công")
    public ResponseEntity<AuthResponse.UserSummary> getAccount() {
        AuthResponse.UserSummary response = this.authService.getAccount();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/refresh")
    @ApiMessage("Gia hạn token thành công")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        AuthResponse authResponse = this.authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        this.authService.logout(response);
        return ResponseEntity.ok().build();
    }
}

