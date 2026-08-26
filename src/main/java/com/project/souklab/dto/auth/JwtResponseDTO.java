package com.project.souklab.dto.auth;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String username;
    private List<String> roles;
}
