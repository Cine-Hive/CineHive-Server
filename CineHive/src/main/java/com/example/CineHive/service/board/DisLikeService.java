package com.example.CineHive.service.board;

public interface DislikeService {
    void addDislike(Long boardId, String memberEmail);
    void removeDislike(Long boardId, String memberEmail);
    int getDislikeCount(Long boardId);
}