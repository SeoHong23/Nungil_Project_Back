package com.nungil.Repository.Interfaces;

import com.nungil.Dto.BannerDTO;
import com.nungil.entity.BannerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerRepository {
    void insertBanner(@Param("title") String title, @Param("fileName") String fileName, @Param("type") String type);
    BannerDTO selectBannerById(@Param("id") Integer id);
    List<BannerEntity> findAllBanner();
    void deleteBanner(@Param("id") Integer id);
    BannerDTO randomBanner(@Param("type") String type);
}
