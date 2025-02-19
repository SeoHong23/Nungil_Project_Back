package com.nungil.Repository.Interfaces;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BannerRepository {
    void insertBanner(@Param("title") String title, @Param("path") String path);
}
