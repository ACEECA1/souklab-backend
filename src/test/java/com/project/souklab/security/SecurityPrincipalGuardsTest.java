package com.project.souklab.security;

import com.project.souklab.config.AppProperties;
import java.time.Clock;
import com.project.souklab.service.auth.AuthService;
import com.project.souklab.util.ServletResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests verifying principal type-safety guards in JwtUtils and OAuth2AuthenticationSuccessHandler.
 * Ensures unexpected principal types (e.g. String or AnonymousAuthenticationToken) result in clear
 * IllegalArgumentException rather than unhandled ClassCastException.
 */
class SecurityPrincipalGuardsTest {

    private JwtUtils jwtUtils;
    private OAuth2AuthenticationSuccessHandler oAuth2Handler;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setSecret("secretKeyForTestingGuardsOnlyMin32BytesLength123");
        appProperties.getJwt().setAccessTokenExpirationMs(3600000L);
        appProperties.getJwt().setRefreshTokenExpirationMs(86400000L);

        jwtUtils = new JwtUtils(appProperties, Clock.systemUTC());

        AuthService authService = Mockito.mock(AuthService.class);
        ServletResponseUtil servletResponseUtil = Mockito.mock(ServletResponseUtil.class);
        oAuth2Handler = new OAuth2AuthenticationSuccessHandler(authService, servletResponseUtil);
    }

    /**
     * Verifies JwtUtils.generateAccessToken throws IllegalArgumentException when principal is not a UserDetails.
     */
    @Test
    @DisplayName("JwtUtils: generateAccessToken rejects non-UserDetails principal with IllegalArgumentException")
    void testJwtUtils_generateAccessToken_rejectsNonUserDetailsPrincipal() {
        System.out.println("=== JWT ACCESS TOKEN GUARD EVIDENCE ===");
        Authentication anonymousAuth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        assertThatThrownBy(() -> jwtUtils.generateAccessToken(anonymousAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(ClassCastException.class)
                .hasMessageStartingWith("Expected principal of type UserDetails, but found: java.lang.String")
                .satisfies(ex -> {
                    System.out.println("Exception: " + ex.getClass().getName());
                    System.out.println("Message: " + ex.getMessage());
                });

        Authentication tokenWithCustomPrincipal = new TestingAuthenticationToken(12345L, "creds");
        assertThatThrownBy(() -> jwtUtils.generateAccessToken(tokenWithCustomPrincipal))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(ClassCastException.class)
                .hasMessageStartingWith("Expected principal of type UserDetails, but found: java.lang.Long");

        System.out.println("Proof: JwtUtils.generateAccessToken guarded against non-UserDetails principal");
    }

    /**
     * Verifies JwtUtils.generateRefreshToken throws IllegalArgumentException when principal is not a UserDetails.
     */
    @Test
    @DisplayName("JwtUtils: generateRefreshToken rejects non-UserDetails principal with IllegalArgumentException")
    void testJwtUtils_generateRefreshToken_rejectsNonUserDetailsPrincipal() {
        System.out.println("=== JWT REFRESH TOKEN GUARD EVIDENCE ===");
        Authentication anonymousAuth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        assertThatThrownBy(() -> jwtUtils.generateRefreshToken(anonymousAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(ClassCastException.class)
                .hasMessageStartingWith("Expected principal of type UserDetails, but found: java.lang.String")
                .satisfies(ex -> {
                    System.out.println("Exception: " + ex.getClass().getName());
                    System.out.println("Message: " + ex.getMessage());
                });

        System.out.println("Proof: JwtUtils.generateRefreshToken guarded against non-UserDetails principal");
    }

    /**
     * Verifies OAuth2AuthenticationSuccessHandler throws IllegalArgumentException when principal is not an OAuth2User.
     */
    @Test
    @DisplayName("OAuth2AuthenticationSuccessHandler: rejects non-OAuth2User principal with IllegalArgumentException")
    void testOAuth2Handler_rejectsNonOAuth2UserPrincipal() {
        System.out.println("=== OAUTH2 SUCCESS HANDLER GUARD EVIDENCE ===");
        Authentication stringPrincipalAuth = new TestingAuthenticationToken("standardUsername", "password");
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> oAuth2Handler.onAuthenticationSuccess(request, response, stringPrincipalAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(ClassCastException.class)
                .hasMessageStartingWith("Expected principal of type OAuth2User, but found: java.lang.String")
                .satisfies(ex -> {
                    System.out.println("Exception: " + ex.getClass().getName());
                    System.out.println("Message: " + ex.getMessage());
                });

        System.out.println("Proof: OAuth2AuthenticationSuccessHandler guarded against non-OAuth2User principal");
    }
}
