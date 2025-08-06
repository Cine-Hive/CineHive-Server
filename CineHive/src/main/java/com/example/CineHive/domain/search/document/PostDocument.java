package com.example.CineHive.domain.search.document;

import com.example.CineHive.domain.post.entity.Post;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;

@Getter
@Builder
@Document(indexName = "posts")
@Setting(settingPath = "elasticsearch/nori-settings.json")
public class PostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String content;

    @Field(type = FieldType.Keyword)
    private String userNickname;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    public static PostDocument from(Post post) {
        return PostDocument.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }
}