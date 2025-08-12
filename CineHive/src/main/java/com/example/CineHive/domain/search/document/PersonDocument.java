package com.example.CineHive.domain.search.document;

import com.example.CineHive.client.tmdb.dto.TmdbPersonInListResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Builder
@Document(indexName = "persons")
@Setting(settingPath = "elasticsearch/nori-settings.json")
public class PersonDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String name;

    @Field(type = FieldType.Keyword, index = false)
    private String profilePath;

    /**
     * TMDB API 응답 DTO를 PersonDocument로 변환합니다.
     */
    public static PersonDocument from(TmdbPersonInListResponse tmdbPerson) {
        return PersonDocument.builder()
                .id(tmdbPerson.id())
                .name(tmdbPerson.name())
                .profilePath(tmdbPerson.profilePath())
                .build();
    }
}