package com.example.CineHive.mapper;

import com.example.CineHive.dto.ott.OttDto;
import com.example.CineHive.dto.ott.ProviderDto;
import com.example.CineHive.entity.ott.Ott;

import java.util.List;
import java.util.stream.Collectors;

public class OttMapper {

    // 엔티티 -> DTO 변환
    public static OttDto toDto(Ott ott) {
        return new OttDto(
                ott.getId(),
                ott.getTitle(),
                ott.getOverview(),
                ott.getPosterPath(),
                ott.getPopularity(),
                ott.getReleaseDate(),
                new ProviderDto(ott.getProvider().getId(), ott.getProvider().getName())
        );
    }

    // 엔티티 리스트 -> DTO 리스트 변환
    public static List<OttDto> toDtoList(List<Ott> movies) {
        return movies.stream()
                .map(OttMapper::toDto)
                .collect(Collectors.toList());
    }
}
