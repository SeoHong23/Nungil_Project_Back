package com.nungil.Repository.Interfaces;

import com.nungil.Dto.WatchedDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WatchedRepository {

    void insertWatched(WatchedDTO watchedDTO);

    void deleteWatched(WatchedDTO watchedDTO);

    boolean existsByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") String videoId);

    Long countWatchedByUser(@Param("userId") Long userId);
}
