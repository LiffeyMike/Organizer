package com.organizer.adapter.graphql.pagination;

public record Edge<N>(String cursor, N node) {}
