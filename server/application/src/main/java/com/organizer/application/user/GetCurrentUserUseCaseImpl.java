package com.organizer.application.user;

import com.organizer.application.port.out.UserRepository;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {

  private final UserRepository userRepository;

  public GetCurrentUserUseCaseImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User execute(UserId userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}
