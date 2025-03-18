package com.example.CineHive.entity.credit.drama;

import com.example.CineHive.entity.videotype.Drama;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DramaGenre") // 엔티티 이름을 명시적으로 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="drama_genres")
public class Genre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "drama_id")
    @JsonIgnore
    private Drama drama;

}
