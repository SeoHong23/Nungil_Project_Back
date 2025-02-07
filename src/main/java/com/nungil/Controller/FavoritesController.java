package com.nungil.Controller;

import com.nungil.Dto.FavoritesDTO;
import com.nungil.Service.FavoritesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

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
}
