package com.organizer.application.user;

import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

  private final TokenIssuerPort tokenIssuer;
  private final UserRepository userRepository;

  public RefreshTokenUseCaseImpl(TokenIssuerPort tokenIssuer, UserRepository userRepository) {
    this.tokenIssuer = tokenIssuer;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(noRollbackFor = InvalidCredentialsException.class)
  public AuthResult execute(String presentedRefreshToken) {
    TokenPair tokens = tokenIssuer.rotateRefreshToken(presentedRefreshToken);

    UserId userId =
        tokenIssuer
            .parseAccessToken(tokens.accessToken())
            .orElseThrow(
                () -> new IllegalStateException("Freshly issued accessToken did not parse"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new IllegalStateException("User " + userId + " vanished mid rotation"));

    return new AuthResult(user, tokens);
  }
}
