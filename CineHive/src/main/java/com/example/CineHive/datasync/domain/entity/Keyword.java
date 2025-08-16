package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Keyword {
    
    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;
    
    @Column(name = "name", nullable = false, length = 128)
    private String name;
}