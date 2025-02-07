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

    public void removeFavorite(Long userId, String videoId) {
        favoritesRepository.deleteFavorite(userId, videoId);
    }

    public boolean isFavorite(Long userId, String videoId) {
        return favoritesRepository.existsByUserIdAndVideoId(userId, videoId);
    }

}
