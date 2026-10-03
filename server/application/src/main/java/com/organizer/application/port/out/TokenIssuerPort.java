package com.organizer.application.port.out;

import java.util.Optional;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;
import com.organizer.domain.auth.TokenPair;

public interface TokenIssuerPort {
  TokenPair issueInitial(UserId userId, Email email);

  Optional<UserId> parseAccessToken(String token);

  TokenPair rotateRefreshToken(String presentedRefreshToken);
}
