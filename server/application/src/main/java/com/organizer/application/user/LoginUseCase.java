package com.organizer.application.user;

public interface LoginUseCase {
  AuthResult execute(LoginCommand command);
}
