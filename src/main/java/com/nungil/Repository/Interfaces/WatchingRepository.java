package com.nungil.Repository.Interfaces;

import com.nungil.Dto.WatchedDTO;
import com.nungil.Dto.WatchingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WatchingRepository {

    void insertWatching(WatchingDTO watchingDTO);

    void deleteWatching(WatchingDTO watchingDTO);

    boolean existsByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") String videoId);
}
