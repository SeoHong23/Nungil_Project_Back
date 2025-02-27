package com.nungil.Repository.Interfaces;

import com.nungil.Dto.SettingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SettingRepository {
    void insertSetting(@Param("userId") Integer userId);
    SettingDTO selectSettingByUID(@Param("userId") Integer userId);
    void updateSetting(@Param("userId") Integer userId, @Param("setting") SettingDTO setting);
}
