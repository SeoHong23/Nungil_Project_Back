package com.nungil.Service;

import com.nungil.Dto.BannerDTO;
import com.nungil.Repository.Interfaces.BannerRepository;
import com.nungil.entity.BannerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BannerService {
    private final BannerRepository bannerRepository;


    public void insertBanner(String title, String fileName, String type){
        bannerRepository.insertBanner(title, fileName, type);
    }

    public List<BannerDTO> getAllBanner(){
        return bannerRepository.findAllBanner().stream().map(
                banner -> new BannerDTO(banner.getId()+"", banner.getTitle(), banner.getFileName(), banner.getType())).collect(Collectors.toList());
    }

    public BannerDTO getBannerById(int id){
        return bannerRepository.selectBannerById(id);
    }

    public void deleteBanner(int id){
        bannerRepository.deleteBanner(id);
    }

    public BannerDTO getRandomBanner(String type){
        return bannerRepository.randomBanner(type);
    }
}
