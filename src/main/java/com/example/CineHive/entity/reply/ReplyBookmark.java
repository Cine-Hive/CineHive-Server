package com.example.CineHive.entity.reply;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reply_bookmark", uniqueConstraints = { // 중복 방지
        @UniqueConstraint(columnNames = {"memEmail", "movie_id"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "memEmail", nullable = false)
    private String memEmail;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

}
