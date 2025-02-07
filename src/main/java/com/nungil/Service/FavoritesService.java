package com.nungil.Service;

import com.nungil.Repository.Interfaces.FavoritesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FavoritesService {

    private final FavoritesRepository favoritesRepository;

    public void addFavorite(Long userId, String videoId) {
        favoritesRepository.insertFavorite(userId, videoId);
    }
}
