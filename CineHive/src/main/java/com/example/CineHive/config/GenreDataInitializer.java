package com.example.CineHive.config;

import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.repository.media.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDataInitializer implements CommandLineRunner {

    private final GenreRepository genreRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (genreRepository.count() == 0) {
            log.info("장르 테이블이 비어있어 기본 장르 정보를 주입합니다.");
            initializeAllGenres();
            log.info("장르 데이터 초기화 완료: {} 개의 장르가 저장되었습니다.", genreRepository.count());
        }
    }

    /**
     * 모든 장르를 COMMON 타입으로 초기화
     */
    private void initializeAllGenres() {
        List<Genre> allGenres = Arrays.asList(
            // 영화 장르
            createGenre(28, "액션"),
            createGenre(12, "모험"),
            createGenre(16, "애니메이션"),
            createGenre(35, "코미디"),
            createGenre(80, "범죄"),
            createGenre(99, "다큐멘터리"),
            createGenre(18, "드라마"),
            createGenre(10751, "가족"),
            createGenre(14, "판타지"),
            createGenre(36, "역사"),
            createGenre(27, "공포"),
            createGenre(10402, "음악"),
            createGenre(9648, "미스터리"),
            createGenre(10749, "로맨스"),
            createGenre(878, "SF"),
            createGenre(10770, "TV 영화"),
            createGenre(53, "스릴러"),
            createGenre(10752, "전쟁"),
            createGenre(37, "서부"),
            
            // TV 장르 (영화 장르와 중복되지 않는 것만)
            createGenre(10759, "액션 & 어드벤처"),
            createGenre(10762, "키즈"),
            createGenre(10763, "뉴스"),
            createGenre(10764, "리얼리티"),
            createGenre(10765, "SF & 판타지"),
            createGenre(10766, "소프"),
            createGenre(10767, "토크"),
            createGenre(10768, "전쟁 & 정치")
        );
        
        genreRepository.saveAll(allGenres);
        log.info("모든 장르 {} 개 초기화 완료", allGenres.size());
    }

    /**
     * 장르 생성 (COMMON 타입으로 통일)
     */
    private Genre createGenre(Integer id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genre.setMediaType(Genre.MediaType.COMMON);
        return genre;
    }
} 