package com.organizer.application.user;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.User;

public interface GetCurrentUserUseCase {

  User execute(UserId userId);
}
