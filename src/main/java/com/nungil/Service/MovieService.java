package com.nungil.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import com.nungil.Repository.Interfaces.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {
    @Autowired
    private KobisService kobisService;

    @Autowired
    private KinoService kinoService;

    @Autowired
    MovieRepository movieRepository;

    /**
     * 영화 제목을 기반으로 KOBIS API와 키노라이츠를 호출하여 정보를 반환합니다.
     *
     * @param title     사용자 입력 영화 제목
     * @param kobisYear 사용자 입력 연도 (개봉 연도)
     * @return 영화 정보와 OTT 링크를 포함한 Map
     */
    public Map<String, Object> getMovieDetails(String title, String kobisYear) {
        Map<String, Object> result = new HashMap<>();

        // KOBIS API 호출
        JsonNode movieList = kobisService.getMovieByTitle(title);
        if (movieList == null || !movieList.isArray() || movieList.isEmpty()) {
            result.put("message", "KOBIS API에서 검색 결과가 없습니다.");
            return result;
        }

        // KOBIS 응답 데이터 중 입력 값과 가장 유사한 영화 찾기
        JsonNode matchingMovie = findMatchingKobisMovie(movieList, title, kobisYear);
        if (matchingMovie == null) {
            result.put("message", "KOBIS에서 입력 데이터와 일치하는 영화가 없습니다.");
            return result;
        }

        // 매칭된 KOBIS 영화 정보
        String kobisTitle = matchingMovie.path("movieNm").asText();
        String kobisOpenDate = matchingMovie.path("openDt").asText();
        String kobisDirector = matchingMovie.path("directors").isArray() && matchingMovie.path("directors").size() > 0
                ? matchingMovie.path("directors").get(0).path("peopleNm").asText()
                : "정보 없음";
        String kobisGenre = matchingMovie.path("genreAlt").asText();

        // 키노라이츠 크롤링
        List<MovieDTO> kinoMovies = kinoService.fetchMoviesByTitle(title, kobisYear);
        if (kinoMovies.isEmpty()) {
            result.put("message", "키노라이츠에서 영화 정보를 찾을 수 없습니다.");
            return result;
        }

        // KOBIS와 키노라이츠 데이터 비교
        for (MovieDTO kinoMovie : kinoMovies) {
            String kinoTitle = kinoMovie.getTitle();
            String kinoOpenDate = kinoMovie.getReleaseDate(); // 예: "2019년 01월 23일"
            String kinoYear = kinoOpenDate.replaceAll("[^0-9]", "").substring(0, 4); // "2019년 01월 23일" → "2019"

            // 제목과 개봉 연도 비교
            if (kobisTitle.equals(kinoTitle) && isYearMatching(kinoYear, kobisOpenDate, matchingMovie.path("prdtYear").asText())) {
                // 매칭된 경우 결과 저장
                result.put("movieCd", matchingMovie.path("movieCd").asText());
                result.put("movieTitle", kobisTitle);
                result.put("releaseDate", kobisOpenDate);
                result.put("director", kobisDirector);
                result.put("genre", kobisGenre);
                result.put("ottLinks", kinoMovie.getOttInfo());
                System.out.println("Result OTT Links: " + result.get("ottLinks"));

                System.out.println("kinoMovie: " + kinoMovie);
                System.out.println("kinoMovie OTT Info: " + kinoMovie.getOttInfo());

                MovieDocument.OTTInfo ottInfo = new MovieDocument.OTTInfo();
                if (kinoMovie.getOttInfo() != null) {
                    System.out.println("OTT Info is not null");
                    ottInfo.setPlatform(kinoMovie.getOttInfo().get(0).getOttPlatform());
                    ottInfo.setAvailable(kinoMovie.getOttInfo().get(0).getAvailable());
                } else {
                    System.out.println("OTT Info is null, setting default values.");
                    ottInfo.setPlatform("N/A");
                    ottInfo.setAvailable(false);
                }

                MovieDocument movie = new MovieDocument();
                movie.setTitle(kobisTitle);
                movie.setReleaseDate(kobisOpenDate);
                movie.setNation(kinoMovie.getNation());
                movie.setGenre(kinoMovie.getGenre());
                movie.setType("");
                movie.setRuntime(kinoMovie.getRuntime());
                movie.setOttInfo(ottInfo);

                movieRepository.save(movie);
                return result;
            }
        }

        // KOBIS와 키노라이츠 모두에서 매칭 실패
        result.put("message", "키노라이츠와 일치하는 영화 정보를 찾을 수 없습니다.");
        return result;
    }

    /**
     * KOBIS 응답 데이터에서 입력 제목과 개봉 연도에 가장 유사한 영화 선택.
     *
     * @param movieList  KOBIS 응답 영화 목록
     * @param inputTitle 사용자 입력 영화 제목
     * @param inputYear  사용자 입력 연도 (개봉 연도)
     * @return 매칭된 KOBIS 영화 데이터
     */
    private JsonNode findMatchingKobisMovie(JsonNode movieList, String inputTitle, String inputYear) {
        for (JsonNode movie : movieList) {
            String kobisTitle = movie.path("movieNm").asText();
            String kobisOpenDate = movie.path("openDt").asText();
            String kobisPrdtYear = movie.path("prdtYear").asText();

            // 제목과 연도 비교
            if (isTitleMatching(inputTitle, kobisTitle) && isYearMatching(inputYear, kobisOpenDate, kobisPrdtYear)) {
                return movie; // 매칭된 영화 반환
            }
        }
        return null; // 매칭 실패
    }

    /**
     * 영화 제목이 일치하는지 확인 (공백, 특수문자, 대소문자 무시).
     *
     * @param inputTitle 사용자 입력 제목
     * @param kobisTitle KOBIS 영화 제목
     * @return 제목이 일치하면 true
     */
    private boolean isTitleMatching(String inputTitle, String kobisTitle) {
        String normalizedInput = inputTitle.replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();
        String normalizedKobis = kobisTitle.replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();
        return normalizedInput.equals(normalizedKobis);
    }

    /**
     * 입력 연도와 KOBIS 연도가 일치하는지 확인.
     *
     * @param inputYear     사용자 입력 연도
     * @param kobisOpenDate KOBIS 개봉일
     * @param kobisPrdtYear KOBIS 제작 연도
     * @return 연도가 일치하면 true
     */
    private boolean isYearMatching(String inputYear, String kobisOpenDate, String kobisPrdtYear) {
        if (kobisOpenDate != null && !kobisOpenDate.isEmpty()) {
            return kobisOpenDate.startsWith(inputYear); // 개봉일의 연도 확인
        }
        return kobisPrdtYear.equals(inputYear); // 제작 연도로 비교
    }

    /**
     * KOBIS 개봉일과 키노라이츠 개봉일이 일치하는지 확인.
     *
     * @param kobisOpenDate KOBIS 개봉일 (yyyyMMdd)
     * @param kinoOpenDate  키노라이츠 개봉일 (예: "2019년 01월 23일")
     * @return 개봉일이 일치하면 true
     */
    private boolean isDateMatching(String kobisOpenDate, String kinoOpenDate) {
        if (kobisOpenDate == null || kobisOpenDate.isEmpty() || kinoOpenDate == null || kinoOpenDate.isEmpty()) {
            return false;
        }
        String normalizedKinoDate = kinoOpenDate.replaceAll("[^0-9]", ""); // "2019년 01월 23일" -> "20190123"
        return kobisOpenDate.equals(normalizedKinoDate);
    }
}
