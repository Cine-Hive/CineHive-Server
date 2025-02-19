package com.example.CineHive.entity.reply;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_email", nullable = false)
    private String userEmail;


    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Lob
    @Column(nullable = false)
    private String reviewContent;

}
