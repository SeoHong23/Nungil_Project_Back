package com.nungil.Controller;

import com.nungil.Document.VideoList;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Dto.VideoRankResponseDTO;
import com.nungil.Service.VideoListService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class VideoRankingController {
    private final VideoListService videoService;

    public VideoRankingController(VideoListService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/daily")
    public List<VideoRankResponseDTO> getDailyRanking(@RequestParam(required = false) String date) {
        if (date == null || date.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            int currentHour = now.getHour();

            // 자정(0시)부터 오전 4시(4시) 사이인 경우 2일 전 날짜로 설정
            if (currentHour >= 0 && currentHour < 9) {
                date = now.minusDays(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else {
                // 그 외 시간대는 기존처럼 1일 전 날짜로 설정
                date = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        }
        return videoService.getBoxOffice(date, "daily");
    }

    @GetMapping("/weekly")
    public List<VideoRankResponseDTO> getWeeklyRanking(@RequestParam(required = false) String date) {
        String formattedDate;

        if (date != null && !date.isEmpty()) {
            // 파라미터가 있는 경우 해당 날짜를 LocalDate로 변환
            LocalDate inputDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 해당 날짜가 포함된 주의 일요일 계산
            LocalDate sundayOfWeek = inputDate.with(DayOfWeek.SUNDAY);
            formattedDate = sundayOfWeek.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            // 파라미터가 없을 경우, 오늘 날짜 기준으로 지난 주 일요일 계산
            LocalDate today = LocalDate.now();
            LocalDate lastSunday = today.with(DayOfWeek.SUNDAY).minusWeeks(1);
            formattedDate = lastSunday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        return videoService.getBoxOffice(formattedDate, "weekly");
    }



}
