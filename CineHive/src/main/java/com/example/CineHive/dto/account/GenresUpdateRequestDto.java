package com.example.CineHive.dto.account;

import java.util.List;

public record GenresUpdateRequestDto(
        List<String> genres
) {}