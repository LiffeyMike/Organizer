package com.organizer.application.user;

public interface RefreshTokenUseCase {
  AuthResult execute(String presentedRefreshToken);
}
