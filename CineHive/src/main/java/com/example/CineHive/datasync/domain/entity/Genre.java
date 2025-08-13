package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genres")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Genre extends BaseEntity {
    
    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;
    
    @Column(name = "name", nullable = false)
    private String name;
}