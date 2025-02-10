package com.nungil.Repository.Interfaces;

import com.nungil.Dto.WatchedDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WatchedRepository {

    void insertWatched(WatchedDTO watchedDTO);
}
