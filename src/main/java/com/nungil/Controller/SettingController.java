package com.nungil.Controller;

import com.nungil.Dto.BannerDTO;
import com.nungil.Dto.SettingDTO;
import com.nungil.Service.BannerService;
import com.nungil.Service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/setting")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping
    public ResponseEntity<SettingDTO> getSetting(@RequestParam int userId) {
        SettingDTO setting = settingService.getSetting(userId);
        if (setting == null) {
            settingService.insertSetting(userId);
            SettingDTO newSetting = new SettingDTO();
            newSetting.setUserId(Long.valueOf(userId));
            newSetting.setIsAlert('0');
            log.info(newSetting.toString());
            return ResponseEntity.ok(newSetting);
        }
        return ResponseEntity.ok(setting);
    }

    @PutMapping
    public ResponseEntity updateSetting(@RequestParam int userId, @RequestBody SettingDTO setting) {
        try{

            settingService.updateSetting(userId, setting);

            return ResponseEntity.ok().build();
        }catch (Exception e){
            e.printStackTrace();

            return ResponseEntity.badRequest().build();
        }
    }
}
