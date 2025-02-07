package com.nungil.Repository.Interfaces;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoritesRepository {

    void insertFavorite(@Param("userId") Long userId, @Param("videoId") String videoId);

    void deleteFavorite(@Param("userId") Long userId, @Param("videoId") String videoId);
}
