package com.nungil.Controller;

import com.nungil.Service.LikesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
public class LikesController {

    private final LikesService likesService;

    // 좋아요
    @PostMapping("/{videoId}/like")
    public ResponseEntity<?> likeVideo(@PathVariable String videoId, @RequestParam Long userId) {
        likesService.likeVideo(videoId, userId);
        return ResponseEntity.ok("Liked");
    }

    // 좋아요 삭제
    @DeleteMapping("/{videoId}/unlike")
    public ResponseEntity<?> unlikeVideo(@PathVariable String videoId, @RequestParam Long userId) {
        likesService.unlikeVideo(videoId, userId);
        return ResponseEntity.ok("Unliked");
    }

    // 별로에요
    @PostMapping("/{videoId}/dislike")
    public ResponseEntity<?> dislikeVideo(@PathVariable String videoId, @RequestParam Long userId) {
        likesService.dislikeVideo(videoId, userId);
        return ResponseEntity.ok("Disliked");
    }

    // 별로에요 삭제
    @DeleteMapping("/{videoId}/undislike")
    public ResponseEntity<?> undislikeVideo(@PathVariable String videoId, @RequestParam Long userId) {
        likesService.deletedislikeVideo(videoId, userId);
        return ResponseEntity.ok("Undisliked");
    }

    @GetMapping("/{videoId}/like-status")
    public ResponseEntity<?> getLikeStatus(@PathVariable String videoId, @RequestParam Long userId) {
        boolean isLiked = likesService.isLiked(videoId, userId);
        boolean isDisliked = likesService.isDisliked(videoId, userId);

        // 좋아요/별로예요 상태를 반환
        Map<String, Boolean> status = new HashMap<>();
        status.put("isLiked", isLiked);
        status.put("isDisliked", isDisliked);

        return ResponseEntity.ok(status);
    }

}
