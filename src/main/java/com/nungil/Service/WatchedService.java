package com.nungil.Service;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Repository.Interfaces.WatchedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WatchedService {

    private final WatchedRepository watchedRepository;


    public void addWatched(WatchedDTO watchedDTO) {
        watchedRepository.insertWatched(watchedDTO);
    }
}
