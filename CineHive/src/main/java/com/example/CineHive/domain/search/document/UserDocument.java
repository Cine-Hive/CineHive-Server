package com.example.CineHive.domain.search.document;

import com.example.CineHive.domain.user.entity.User;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Builder
@Document(indexName = "users")
@Setting(settingPath = "elasticsearch/nori-settings.json")
public record UserDocument(
        @Id
        Long id,

        @Field(type = FieldType.Text, analyzer = "nori_analyzer")
        String nickname,

        @Field(type = FieldType.Keyword, index = false)
        String profileImageUrl
) {
    /**
     * User 엔티티를 UserDocument로 변환합니다.
     * @param user 원본 User 엔티티
     * @return Elasticsearch에 저장될 UserDocument
     */
    public static UserDocument from(User user) {
        return UserDocument.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
