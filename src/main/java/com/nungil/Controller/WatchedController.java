package com.nungil.Controller;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Service.WatchedService;
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
public class WatchedController {

    private final WatchedService watchedService;

    @PostMapping("/watched")
    public ResponseEntity<Map<String, String>> addwatched(@RequestBody WatchedDTO watchedDTO) {
        watchedService.addWatched(watchedDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Watched Successfully");

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/watched/remove")
    public ResponseEntity<Map<String, String>> removewatched(@RequestBody WatchedDTO watchedDTO) {
        watchedService.removeWatched(watchedDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Remove Watched Successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/watched/{videoId}/status")
    public ResponseEntity<Map<String, Boolean>> getWatchedStatus(
            @PathVariable String videoId,
            @RequestParam Long userId) {

        WatchedDTO watchedDTO = new WatchedDTO();
        watchedDTO.setUserId(userId);
        watchedDTO.setVideoId(videoId);

        boolean isWatched = watchedService.isWatched(watchedDTO);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isWatched", isWatched);

        return ResponseEntity.ok(response);

    }
}


