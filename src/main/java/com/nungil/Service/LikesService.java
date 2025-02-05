package com.nungil.Service;


import com.nungil.Repository.Interfaces.LikesRepository;
import com.nungil.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class LikesService {

    private final LikesRepository likesMapper;
    private final VideoRepository videoRepository;

    public void likeVideo(String videoId, Long userId) {
        log.info("Trying to like video with ID: {}", videoId);

        if (!videoRepository.existsById(videoId)) {
            log.error("Video with ID {} does not exist!", videoId);
            throw new IllegalArgumentException("해당 videoId가 존재하지 않습니다.");
        }

        if (likesMapper.existsLike(videoId, userId) == 0) {
            likesMapper.insertLike(videoId, userId);
            log.info("Like inserted for video ID {} by user {}", videoId, userId);
        } else {
            log.warn("User {} already liked video {}", userId, videoId);
        }
    }

    public void unlikeVideo(String videoId, Long userId) {
        log.info("Trying to unlike video ID: {} by user ID: {}", videoId, userId);

        if (likesMapper.existsLike(videoId, userId) > 0) {
            likesMapper.deleteLike(videoId, userId);
            log.info("Like removed for video ID {} by user {}", videoId, userId);
        } else {
            log.warn("User {} has not liked video {}, so nothing to remove", userId, videoId);
        }
    }
}
