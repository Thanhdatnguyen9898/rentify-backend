package com.rentify.modules.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class AuthResponse {
    private String accessToken;
    private UserSummary user;
    @Getter
    @Setter
    @Builder
    public static class UserSummary {
        private Long id;
        private String phoneNumber;
        private String email;
        private String username;
        private String fullName;
        private String role;
        private String avatarUrl;
    }
}
