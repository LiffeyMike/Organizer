package com.organizer.application.user;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.User;
import java.util.Optional;

public interface GetUserUseCase {

  Optional<User> execute(UserId callerId, UserId targetId);
}
