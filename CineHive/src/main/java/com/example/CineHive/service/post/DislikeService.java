package com.example.CineHive.service.post;

public interface DislikeService {
    void addDislike(Long boardId, String memberEmail);
    void removeDislike(Long boardId, String memberEmail);
    int getDislikeCount(Long boardId);
}