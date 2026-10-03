package com.organizer.adapter.security;

import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;

public record AuthenticatedUser(UserId userId, Email email) {
}
