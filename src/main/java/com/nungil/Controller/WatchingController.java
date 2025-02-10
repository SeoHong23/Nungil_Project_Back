package com.nungil.Controller;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Dto.WatchingDTO;
import com.nungil.Service.WatchingService;
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
public class WatchingController {

    private final WatchingService watchingService;

    @PostMapping("/watching")
    public ResponseEntity<Map<String, String>> addwatching(@RequestBody WatchingDTO watchingDTO) {
        watchingService.addWatching(watchingDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Watching Successfully");

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/watching/remove")
    public ResponseEntity<Map<String, String>> removewatching(@RequestBody WatchingDTO watchingDTO) {
        watchingService.removeWatching(watchingDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Remove Watched Successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/watching/{videoId}/status")
    public ResponseEntity<Map<String, Boolean>> getWatchingStatus(
            @PathVariable String videoId,
            @RequestParam Long userId) {

        WatchingDTO watchingDTO = new WatchingDTO();
        watchingDTO.setUserId(userId);
        watchingDTO.setVideoId(videoId);

        boolean isWatched = watchingService.isWatching(watchingDTO);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isWatched", isWatched);

        return ResponseEntity.ok(response);

    }
}


