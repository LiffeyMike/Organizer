package com.organizer.adapter.graphql.pagination;

import java.util.List;

public record Connection<N>(List<Edge<N>> edges, PageInfo pageInfo) {}
