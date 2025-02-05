package com.nungil.Repository.Interfaces;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DisLikesRepository {

    void insertDislike(@Param("videoId") String videoId, @Param("userId") Long userId);

    int existsDislike(@Param("videoId") String videoId, @Param("userId") Long userId);

    void deleteDislike(@Param("videoId") String videoId, @Param("userId") Long userId);
}
