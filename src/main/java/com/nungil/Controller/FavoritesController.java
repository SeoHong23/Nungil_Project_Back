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
    public void addFavorite(@RequestBody FavoritesDTO dto) {
        favoritesService.addFavorite(dto.getUserId(), dto.getVideoId());
    }

    @DeleteMapping("/favorite/remove")
    public String removeFavorite(@RequestBody FavoritesDTO favoritesDTO) {
        favoritesService.removeFavorite(favoritesDTO.getUserId(), favoritesDTO.getVideoId());
        return "찜 목록에서 삭제되었습니다.";
    }

    @GetMapping("/favorite/{videoId}/like-status")
    public ResponseEntity<Map<String, Boolean>> getFavoriteStatus(
            @PathVariable String videoId,
            @PathVariable Long userId) {

        boolean isFavorited = favoritesService.isFavorite(userId, videoId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);

        return ResponseEntity.ok(response);
    }
}
