package com.organizer.application.user;

public record RegisterCommand(String email, String rawPassword, String displayName) {}
