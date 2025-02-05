package com.nungil.Service;


import com.nungil.Repository.Interfaces.DisLikesRepository;
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
    private final DisLikesRepository disLikesMapper;
    private final VideoRepository videoRepository;

    public boolean likeVideo(String videoId, Long userId) {
        if (disLikesMapper.existsDislike(videoId, userId) > 0) {
            return false; // 별로예요가 눌려 있으면 좋아요 불가능
        }
        if (likesMapper.existsLike(videoId, userId) == 0) {
            likesMapper.insertLike(videoId, userId);
        }
        return true;
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

    public boolean dislikeVideo(String videoId, Long userId) {
        if (likesMapper.existsLike(videoId, userId) > 0) {
            return false; // 좋아요가 눌려 있으면 별로예요 불가능
        }
        if (disLikesMapper.existsDislike(videoId, userId) == 0) {
            disLikesMapper.insertDislike(videoId, userId);
        }
        return true;
    }

    public void deletedislikeVideo(String videoId, Long userId) {
        log.info("Trying to unlike video ID: {} by user ID: {}", videoId, userId);

        if (disLikesMapper.existsDislike(videoId, userId) > 0) {
            disLikesMapper.deleteDislike(videoId, userId);
            log.info("Like removed for video ID {} by user {}", videoId, userId);
        } else {
            log.warn("User {} has not liked video {}, so nothing to remove", userId, videoId);
        }
    }


}
