package com.nungil.Controller;

import com.nungil.Dto.NotInterestedDTO;
import com.nungil.Service.NotInterestedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@Log4j2
@RequiredArgsConstructor
@RestController
public class NotInterestedController {

    private final NotInterestedService notInterestedService;

    @PostMapping("/notinterested")
    public ResponseEntity<Map<String,String>> addnotinterested(@RequestBody NotInterestedDTO notinterestedDTO) {
        notInterestedService.addNotInterested(notinterestedDTO);
        Map<String,String> response = new HashMap<>();
        response.put("message", "NotInterested successfully added");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/notinterested/count")
    public ResponseEntity<Map<String, Long>> getnotinterestedCount(@RequestParam Long userId) {
        Long count = notInterestedService.countNotInterestedByUser(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notinterested/remove")
    public ResponseEntity<Map<String,String>> removenotinterested(@RequestBody NotInterestedDTO notinterestedDTO) {
        notInterestedService.removeNotInterested(notinterestedDTO);
        Map<String,String> response = new HashMap<>();
        response.put("message", "NotInterested successfully removed");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notinterested/{videoId}/status")
    public ResponseEntity<Map<String, Boolean>> getnotinterestedstatus
            (@PathVariable String videoId,
             @RequestParam Long userId) {

        NotInterestedDTO notInterestedDTO = new NotInterestedDTO();
        notInterestedDTO.setVideoId(videoId);
        notInterestedDTO.setUserId(userId);

        boolean checkNotInterested = notInterestedService.checkNotInterested(notInterestedDTO);

        Map<String, Boolean> response = new HashMap<>();
        response.put("checkNotInterested", checkNotInterested);

        return ResponseEntity.ok(response);
    }

}
