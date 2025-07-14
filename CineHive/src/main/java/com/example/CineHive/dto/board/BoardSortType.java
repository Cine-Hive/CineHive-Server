package com.example.CineHive.dto.board;

import com.example.CineHive.config.converter.StringValueConvertible;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardSortType implements StringValueConvertible {
    LATEST("latest", "createdAt"),
    VIEWS("views", "views"),
    LIKES("likes", "likeCount");

    private final String clientValue;
    private final String dbField;
}