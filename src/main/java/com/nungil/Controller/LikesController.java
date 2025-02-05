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

    @PostMapping("/{videoId}/like")
    public ResponseEntity<?> likeVideo(@PathVariable String videoId, @RequestParam Long userId) {
        likesService.likeVideo(videoId, userId);
        return ResponseEntity.ok("Liked");
    }
}
