package com.organizer.adapter.graphql.pagination;

public record PageInfo(
    boolean hasNextPage, boolean hasPreviousPage, String startCursor, String endCursor) {}
