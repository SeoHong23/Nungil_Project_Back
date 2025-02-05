package com.nungil.Controller;

import com.nungil.Service.LikesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
