package com.organizer.application.user;

import com.organizer.application.port.out.PasswordHasherPort;
import com.organizer.application.port.out.TokenIssuerPort;
import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.InvalidCredentialsException;
import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.Email;
import com.organizer.domain.user.HashedPassword;
import com.organizer.domain.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

  static final String INVALID_CREDENTIALS = "Invalid email or password";

  private final UserRepository userRepository;
  private final PasswordHasherPort passwordHasher;
  private final TokenIssuerPort tokenIssuer;
  private final HashedPassword timingDecoy;

  public LoginUseCaseImpl(
      UserRepository userRepository,
      PasswordHasherPort passwordHasher,
      TokenIssuerPort tokenIssuer) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
    this.timingDecoy = new HashedPassword(UUID.randomUUID().toString());
  }

  @Override
  public AuthResult execute(LoginCommand command) {
    Email email = new Email(command.email());
    String rawPassword = command.rawPassword();

    Optional<User> found = userRepository.findByEmail(email);
    HashedPassword hashToCheck = found.map(User::password).orElse(timingDecoy);
    boolean passwordMatches = passwordHasher.matches(rawPassword, hashToCheck);

    User user =
        found
            .filter(canidate -> passwordMatches)
            .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS));

    TokenPair tokens = tokenIssuer.issueInitial(user.id(), user.email());
    return new AuthResult(user, tokens);
  }
}
