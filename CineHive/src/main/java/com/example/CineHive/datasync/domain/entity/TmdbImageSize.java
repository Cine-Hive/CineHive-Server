package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ref_tmdb_image_size")
@IdClass(TmdbImageSizeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmdbImageSize {

    @Id
    @Column(length = 16)
    private String kind;

    @Id
    @Column(length = 16)
    private String sizeCode;

    @Column(nullable = false)
    private int orderNo;

    @Builder
    public TmdbImageSize(String kind, String sizeCode, int orderNo) {
        this.kind = kind;
        this.sizeCode = sizeCode;
        this.orderNo = orderNo;
    }
}