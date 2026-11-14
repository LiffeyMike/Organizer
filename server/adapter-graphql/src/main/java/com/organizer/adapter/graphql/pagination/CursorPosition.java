package com.organizer.adapter.graphql.pagination;

import java.time.Instant;
import java.util.UUID;

public record CursorPosition(Instant sortKey, UUID id) {}
