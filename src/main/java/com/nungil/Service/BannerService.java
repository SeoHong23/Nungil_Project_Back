package com.nungil.Service;

import com.nungil.Repository.Interfaces.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BannerService {
    private final BannerRepository bannerRepository;

    public void insertBanner(String title, String path){
        bannerRepository.insertBanner(title, path);
    }
}
