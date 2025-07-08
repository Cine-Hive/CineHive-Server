package com.example.CineHive.repository.banner;

import com.example.CineHive.entity.banner.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    // 활성화된 배너만, 표시 순서에 따라 오름차순으로 조회
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();
}