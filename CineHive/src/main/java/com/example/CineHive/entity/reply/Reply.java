package com.example.CineHive.entity.reply;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reply")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "memEmail", nullable = false)
    private String memEmail;


    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name="mem_nickname", nullable = false)
    private String memNickname;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime replyRegDate;

    public Reply(String memNickname, String memEmail, Long movieId, String content) {
        this.memNickname = memNickname;
        this.memEmail = memEmail;
        this.movieId = movieId;
        this.reviewContent = content;
        this.replyRegDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.replyRegDate = LocalDateTime.now();
    }
}
