package com.nungil.Service;

import com.nungil.Dto.BannerDTO;
import com.nungil.Dto.SettingDTO;
import com.nungil.Repository.Interfaces.BannerRepository;
import com.nungil.Repository.Interfaces.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SettingService {
    private final SettingRepository settingRepository;


    public void insertSetting(int userId){
        settingRepository.insertSetting(userId);
    }

    public SettingDTO getSetting(int userId){
        return settingRepository.selectSettingByUID(userId);
    }

    public void updateSetting(int userId, SettingDTO settingDTO){
        settingRepository.updateSetting(userId, settingDTO);
    }
}
