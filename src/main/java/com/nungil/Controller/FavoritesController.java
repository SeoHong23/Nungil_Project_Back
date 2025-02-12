package com.nungil.Controller;

import com.nungil.Dto.FavoritesDTO;
import com.nungil.Service.FavoritesService;
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
public class FavoritesController {

    private final FavoritesService favoritesService;

    @PostMapping("/favorite")
    public ResponseEntity<Map<String, String>> addFavorite(@RequestBody FavoritesDTO dto) {
        favoritesService.addFavorite(dto.getUserId(), dto.getVideoId());

        // 성공 메시지를 JSON 형식으로 반환
        Map<String, String> response = new HashMap<>();
        response.put("message", "찜 목록에 추가되었습니다.");

        return ResponseEntity.ok(response);  // 200 OK와 함께 JSON 응답 반환
    }

    @GetMapping("/favorite/count")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(@RequestParam Long userId) {
        Long count = favoritesService.countFavoritesByUser(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/favorite/remove")
    public ResponseEntity<Map<String, String>> removeFavorite(@RequestBody FavoritesDTO favoritesDTO) {
        favoritesService.removeFavorite(favoritesDTO.getUserId(), favoritesDTO.getVideoId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "찜 목록에서 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorite/{videoId}/like-status")
    public ResponseEntity<Map<String, Boolean>> getFavoriteStatus(
            @PathVariable String videoId,
            @RequestParam Long userId) {

        boolean isFavorited = favoritesService.isFavorite(userId, videoId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);

        return ResponseEntity.ok(response);
    }
}
