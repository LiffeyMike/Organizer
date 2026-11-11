package com.organizer.application.user;

import com.organizer.domain.auth.TokenPair;
import com.organizer.domain.user.User;

public record AuthResult(User user, TokenPair tokens) {}
