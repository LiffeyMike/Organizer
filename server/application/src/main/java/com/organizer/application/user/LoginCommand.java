package com.organizer.application.user;

public record LoginCommand(String email, String rawPassword) {}
