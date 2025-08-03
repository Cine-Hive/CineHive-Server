package com.example.CineHive.domain.post.controller.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSortType {
    LATEST("latest", "createdAt"),
    VIEWS("views", "views"),
    LIKES("likes", "likeCount");

    private final String clientValue;
    private final String dbField;
}