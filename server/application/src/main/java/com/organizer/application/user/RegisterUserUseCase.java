package com.organizer.application.user;

public interface RegisterUserUseCase {
  AuthResult execute(RegisterCommand command);
}
