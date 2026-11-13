package com.organizer.application.user;

import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GetUserUseCaseImpl implements GetUserUseCase {

  private final UserRepository userRepository;

  public GetUserUseCaseImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<User> execute(UserId callerId, UserId targetId) {
    if (!callerId.equals(targetId)) {
      return Optional.empty();
    }

    return userRepository.findById(targetId);
  }
}
