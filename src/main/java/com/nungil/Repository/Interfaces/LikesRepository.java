package com.nungil.Repository.Interfaces;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikesRepository {

    int insertLike(@Param("videoId") String videoId, @Param("userId") Long userId);

    int existsLike(@Param("videoId") String videoId, @Param("userId") Long userId);

    void deleteLike(@Param("videoId") String videoId, @Param("userId") Long userId);
}
