package com.example.CineHive.entity.board;

import com.example.CineHive.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    private String reason;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @PrePersist
    protected void onCreate() {
        this.reportedAt = LocalDateTime.now();
    }

}
