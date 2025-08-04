package com.example.CineHive.domain.person.service;

import com.example.CineHive.global.service.LikeService;

/**
 * 인물 '좋아요' 기능에 대한 서비스 인터페이스입니다.
 * 공통 LikeService 인터페이스를 상속받아 like, unlike 메서드를 제공합니다.
 */
public interface PersonLikeService extends LikeService {
    // PersonLike에만 특화된 메서드가 있다면 여기에 추가할 수 있습니다.
    // (예: findLikingUsersByPerson(Long personId))
    // 현재는 공통 기능만 사용하므로 비워둡니다.
}