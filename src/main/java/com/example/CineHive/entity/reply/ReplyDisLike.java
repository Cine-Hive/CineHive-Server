package com.example.CineHive.entity.reply;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "reply_dislikes",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"memEmail", "reply_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDisLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "memEmail", nullable = false)
    private String memEmail;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "reply_id", nullable = false)
    private Long replyId;
}
