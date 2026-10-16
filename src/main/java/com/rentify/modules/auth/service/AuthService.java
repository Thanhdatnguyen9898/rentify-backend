package com.rentify.modules.auth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.rentify.common.exception.AppException;
import com.rentify.common.exception.ErrorCode;
import com.rentify.common.util.SecurityUtil;
import com.rentify.modules.auth.dto.request.GoogleLoginRequest;
import com.rentify.modules.auth.dto.request.LoginRequest;
import com.rentify.modules.auth.dto.request.RegisterRequest;
import com.rentify.modules.auth.dto.response.AuthResponse;
import com.rentify.modules.user.entity.Role;
import com.rentify.modules.user.entity.User;
import com.rentify.modules.user.enums.AuthProvider;
import com.rentify.modules.user.repository.RoleRepository;
import com.rentify.modules.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Service 
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final JwtDecoder jwtDecoder;

    @Value("${rentify.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${rentify.oauth2.google.client-id}")
    private String googleClientId;
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityUtil securityUtil,
                       JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
        this.jwtDecoder = jwtDecoder;
    }

    @Transactional 
    public AuthResponse.UserSummary register(RegisterRequest request) {
        // 1. Kiểm tra trùng lặp 3 định danh
        if (this.userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (this.userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        // 2. Lấy role mặc định ROLE_USER
        Role userRole = this.roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // 3. Tạo User và mã hóa mật khẩu
        User user = new User();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(this.passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(userRole);
        user.setIsActive(true);
        user.setIsPhoneVerified(false);

        User savedUser = this.userRepository.save(user);
        return toUserSummary(savedUser);
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // 1. Tìm user theo identifier (SĐT / Email / Username)
        User user = this.userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        // 2. Kiểm tra mật khẩu
        if (!this.passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. Kiểm tra trạng thái tài khoản
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 4. Cấp Access Token & Refresh Token
        String accessToken = this.securityUtil.createAccessToken(user);
        String refreshToken = this.securityUtil.createRefreshToken(user);

        // 5. Gán HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(this.refreshTokenExpiration)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(toUserSummary(user))
                .build();
    }

    public AuthResponse.UserSummary getAccount() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User user = this.userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return toUserSummary(user);
    }

    public AuthResponse refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Jwt decodedJwt;
        try {
            decodedJwt = this.jwtDecoder.decode(refreshToken);
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = Long.parseLong(decodedJwt.getSubject());
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Cấp phát cặp Token mới (Token Rotation)
        String newAccessToken = this.securityUtil.createAccessToken(user);
        String newRefreshToken = this.securityUtil.createRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(this.refreshTokenExpiration)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .user(toUserSummary(user))
                .build();
    }

    public void logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private AuthResponse.UserSummary toUserSummary(User user) {
        return AuthResponse.UserSummary.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request, HttpServletResponse response) {
    // 1. Xác thực ID Token với Google
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(),
            GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(this.googleClientId))
            .build();
    GoogleIdToken googleIdToken;
    try {
        googleIdToken = verifier.verify(request.getIdToken());
    } catch (Exception e) {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    if (googleIdToken == null) {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    // 2. Lấy thông tin user từ Google Payload
    Payload payload = googleIdToken.getPayload();
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    String pictureUrl = (String) payload.get("picture");
    String googleId = payload.getSubject();
    // 3. Tìm xem user đã tồn tại chưa (Account Linking)
    User user = this.userRepository.findByEmail(email).map(existingUser -> {
        // Nếu user đã có từ trước (ví dụ từng đăng ký thủ công), liên kết thêm googleId
        if (existingUser.getGoogleId() == null) {
            existingUser.setGoogleId(googleId);
            return this.userRepository.save(existingUser);
        }
        return existingUser;
    }).orElseGet(() -> {
        // Nếu là user mới  -> Tự động đăng ký
        Role userRole = this.roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setAvatarUrl(pictureUrl);
        newUser.setGoogleId(googleId);
        newUser.setAuthProvider(AuthProvider.GOOGLE);
        newUser.setRole(userRole);
        newUser.setIsActive(true);
        newUser.setIsPhoneVerified(false);
        return this.userRepository.save(newUser);
    });
    if (!Boolean.TRUE.equals(user.getIsActive())) {
        throw new AppException(ErrorCode.ACCOUNT_LOCKED);
    }
    // 4. Cấp phát Access Token & Refresh Token Cookie 
    String accessToken = this.securityUtil.createAccessToken(user);
    String refreshToken = this.securityUtil.createRefreshToken(user);
    ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(this.refreshTokenExpiration)
            .sameSite("Lax")
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return AuthResponse.builder()
            .accessToken(accessToken)
            .user(toUserSummary(user))
            .build();
}
}