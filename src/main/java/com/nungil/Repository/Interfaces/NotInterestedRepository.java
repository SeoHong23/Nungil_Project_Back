package com.nungil.Repository.Interfaces;


import com.nungil.Dto.NotInterestedDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotInterestedRepository {

    void insertNotInterested(NotInterestedDTO notInterestedDTO);

    void deleteNotInterested(NotInterestedDTO notInterestedDTO);

    boolean existsByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") String videoId);

    Long countNotInterestedByUser(@Param("userId") Long userId);
}
