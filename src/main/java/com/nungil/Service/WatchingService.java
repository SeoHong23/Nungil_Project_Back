package com.nungil.Service;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Dto.WatchingDTO;
import com.nungil.Repository.Interfaces.WatchedRepository;
import com.nungil.Repository.Interfaces.WatchingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WatchingService {

    private final WatchingRepository watchingRepository;


    public void addWatching(WatchingDTO watchingDTO) {
        watchingRepository.insertWatching(watchingDTO);
    }

    public void removeWatching(WatchingDTO watchingDTO) {
        watchingRepository.deleteWatching(watchingDTO);
    }

    public boolean isWatching(WatchingDTO watchingDTO) {
        return watchingRepository.existsByUserIdAndVideoId(watchingDTO.getUserId(), watchingDTO.getVideoId());
    }

}
