package com.example.CineHive.domain.search.document;

import com.example.CineHive.domain.collection.entity.Collection;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Builder
@Document(indexName = "collections")
@Setting(settingPath = "elasticsearch/nori-settings.json")
public record CollectionDocument(
        @Id
        Long id,

        @Field(type = FieldType.Text, analyzer = "nori")
        String name,

        @Field(type = FieldType.Keyword, index = false)
        String posterPath
) {
    public static CollectionDocument from(Collection collection) {
        return CollectionDocument.builder()
                .id(collection.getId())
                .name(collection.getName())
                .posterPath(collection.getPosterPath())
                .build();
    }
}