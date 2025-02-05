package com.nungil.Service;


import com.nungil.Repository.Interfaces.LikesRepository;
import com.nungil.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikesService {

    private final LikesRepository likesMapper;
    private final VideoRepository videoRepository;

    public void likeVideo(String videoId, Long userId) {
        // videoId가 존재하는지 확인
        if (!videoRepository.existsById(videoId)) {
            throw new IllegalArgumentException("해당 videoId가 존재하지 않습니다.");
        }

        // 좋아요가 이미 존재하는지 확인 후 추가
        if (likesMapper.existsLike(videoId, userId) == 0) {
            likesMapper.insertLike(videoId, userId);
        }
    }
}
