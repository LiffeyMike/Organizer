package com.organizer.application.port.out;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import java.util.Optional;

public interface TokenIssuerPort {
  TokenPair issueInitial(UserId userId, Email email);

  Optional<UserId> parseAccessToken(String token);

  TokenPair rotateRefreshToken(String presentedRefreshToken);
}
