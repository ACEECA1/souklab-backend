package com.project.souklab.service.security;

import lombok.RequiredArgsConstructor;
import com.project.souklab.config.AppProperties;
import com.project.souklab.dao.RefreshTokenRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.RefreshToken;
import com.project.souklab.model.User;
import com.project.souklab.util.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    /**
     * Generates and stores a new refresh token for a specific user.
     * If the user already has a token, it will be overridden or updated.
     *
     * @param userId the unique identifier of the user who needs the token
     * @return the newly created and saved RefreshToken entity
     * @throws AppException if the user does not exist
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user).orElse(new RefreshToken());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenExpirationMs()));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies whether a given refresh token is still valid (not expired).
     * If expired, it deletes the token from the database and throws an exception.
     *
     * @param token the RefreshToken entity to check
     * @return the same token if it is valid
     * @throws AppException if the token has expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
        }
        return token;
    }

    /**
     * Removes the refresh token associated with a particular user.
     * Usually called upon user logout or session invalidation.
     *
     * @param userId the unique identifier of the user whose token is being deleted
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }
    
    /**
     * Finds a RefreshToken entity in the database using the raw token string.
     *
     * @param token the raw UUID string of the refresh token
     * @return an Optional wrapping the RefreshToken if found, or empty otherwise
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
