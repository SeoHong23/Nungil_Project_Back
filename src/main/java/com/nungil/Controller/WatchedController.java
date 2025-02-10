package com.nungil.Controller;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Service.WatchedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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


}
