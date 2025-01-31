package com.nungil.Service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    private final KobisService kobisService;
    private final KinoService kinoService;

    public MovieService(KinoService kinoService, KobisService kobisService) {
        this.kinoService = kinoService;
        this.kobisService = kobisService;
    }

    public Map<String, Object> getMovieWithOtt(String movieCd) {
        Map<String, Object> movieDetails = kobisService.getMovieDetails(movieCd);
        Map<String, Object> movieInfo = (Map<String, Object>) ((Map<String, Object>) movieDetails.get("movieInfoResult")).get("movieInfo");

        String movieTitle = (String) movieInfo.get("movieNm");

        List<String> ottPlatforms = kinoService.fetchOttPlatforms(movieTitle);


        Map<String, Object> result = new HashMap<>();
        result.put("movieInfo", movieInfo);
        result.put("ottPlatforms", ottPlatforms);

        return result;
        

    }
}
