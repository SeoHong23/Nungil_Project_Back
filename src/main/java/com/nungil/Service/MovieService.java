package com.nungil.Service;

import org.springframework.data.mongodb.core.index.Index;
import com.mongodb.client.result.UpdateResult;
import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import com.nungil.Repository.Interfaces.MovieRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static final int PAGE_SIZE = 10;

    private final MongoTemplate mongoTemplate;
    private final MovieRepository movieRepository;
    private final KinoService kinoService;

    @Autowired
    public MovieService(MongoTemplate mongoTemplate, MovieRepository movieRepository, KinoService kinoService) {
        this.mongoTemplate = mongoTemplate;
        this.movieRepository = movieRepository;
        this.kinoService = kinoService;
    }

//    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시 실행
    @Scheduled(fixedRate = 60000)

    public void scheduledMovieUpdate() {
        System.out.println("🚀 [스케줄러] MongoDB 기반 주기적 크롤링 시작...");

        int page = 1; // 첫 페이지부터 시작
        boolean hasMorePages = true; // 더 이상 가져올 데이터가 없을 때 중단

        while (hasMorePages) {
            System.out.println("🔍 페이지 " + page + " 크롤링 중...");

            // 특정 페이지의 영화 목록 크롤링
            List<MovieDocument> movieList = getMoviesFromPage(page);

            if (movieList.isEmpty()) {
                hasMorePages = false; // 더 이상 크롤링할 데이터가 없음
                System.out.println("⛔ 모든 페이지 크롤링 완료!");
                break;
            }

            for (MovieDocument movie : movieList) {
                if (Boolean.TRUE.equals(movie.isCrawled())) {
                    continue; // 이미 크롤링된 데이터는 건너뛰기
                }

                System.out.println("🔄 업데이트 중: " + movie.getTitle());

                Map<String, Object> updateResult = updateOttInfo(movie.getTitle(), page, PAGE_SIZE);
                boolean success = "OTT 정보가 성공적으로 업데이트되었습니다.".equals(updateResult.get("message"));

                // ✅ 크롤링 성공 시, `isCrawled`와 `lastCrawled` 업데이트
                if (success) {
                    Query updateQuery = new Query(Criteria.where("id").is(movie.getId()));
                    Update update = new Update().set("isCrawled", true).set("lastCrawled", new Date());
                    mongoTemplate.updateFirst(updateQuery, update, MovieDocument.class);
                }
            }

            page++; // 다음 페이지로 이동
        }

        System.out.println("✅ [스케줄러] MongoDB 기반 주기적 크롤링 완료!");
    }
    private List<MovieDocument> getMoviesFromPage(int page) {
        Query query = new Query(
                new Criteria().orOperator(
                        Criteria.where("isCrawled").is(false),
                        Criteria.where("isCrawled").exists(false)
                )
        ).limit(PAGE_SIZE).skip((page - 1) * PAGE_SIZE);
        return mongoTemplate.find(query, MovieDocument.class);
    }

    private boolean isReleaseDateMatch(String dbReleaseDate, String kinoReleaseDate) {
        String dbYear = extractYear(dbReleaseDate);
        String kinoYear = extractYear(kinoReleaseDate);
        return dbYear.equals(kinoYear);
    }



    /**
     * 개봉일에서 연도(YYYY)만 추출하는 메서드
     */
    private String extractYear(Object dateInput) {
        if (dateInput == null) return "";

        String dateStr = String.valueOf(dateInput).trim(); // 공백 제거

        // 4자리 연도만 있는 경우 (ex: "2025")
        if (dateStr.matches("^\\d{4}$")) {
            return dateStr;
        }

        // 8자리 숫자 (YYYYMMDD 형식) → 연도만 추출
        if (dateStr.matches("^\\d{8}$")) {
            return dateStr.substring(0, 4);
        }

        // 정규식을 사용하여 4자리 연도 추출 (문자열 중간 포함 가능)
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b\\d{4}\\b").matcher(dateStr);
        return matcher.find() ? matcher.group() : "";
    }


    @PostConstruct
    public void ensureTextIndex() {
        mongoTemplate.indexOps("video")
                .ensureIndex(new Index().on("title", org.springframework.data.domain.Sort.Direction.ASC));
        System.out.println("✅ title 필드에 텍스트 인덱스가 생성되었습니다.");
    }

    public MovieDocument findMovieByTitle(String title) {
        Query query = new Query(Criteria.where("title").regex(title, "i"));
        return mongoTemplate.findOne(query, MovieDocument.class);
    }

    private String normalize(String input) {
        if (input == null) return "";
        // 모든 공백, 특수문자 제거 및 소문자 변환
        return input.replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase().trim();
    }

    public Map<String, Object> updateOttInfo(String title, int page, int limit) {
        Map<String, Object> result = new HashMap<>();

        // 1️⃣ MongoDB에서 영화 조회
        Query query = new Query(Criteria.where("title")
                .regex("^\\s*" + title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "") + "\\s*$", "i"));
        MovieDocument existingMovie = mongoTemplate.findOne(query, MovieDocument.class);

        if (existingMovie == null) {
            result.put("message", "해당 영화가 MongoDB에 존재하지 않습니다.");
            return result;
        }

        // 2️⃣ 키노라이츠 크롤링
        List<MovieDTO> kinoMovies = kinoService.fetchMoviesByTitle(title);
        if (kinoMovies.isEmpty()) {
            result.put("message", "키노라이츠에서 영화 정보를 찾을 수 없습니다.");
            return result;
        }

        for (MovieDTO kinoMovie : kinoMovies) {
            // **🎯 여기서 제목과 개봉 연도를 비교한 후, 일치하는 경우만 업데이트 진행**
            if (normalize(kinoMovie.getTitle()).equals(normalize(existingMovie.getTitle()))
                    && isReleaseDateMatch(existingMovie.getReleaseDate(), kinoMovie.getReleaseDate())) {

                // OTT 정보 업데이트
                Update update = new Update();

                // ✅ OTT 정보 업데이트
                if (!kinoMovie.getOttInfo().isEmpty()) {
                    List<MovieDocument.OTTInfo> ottInfos = convertToMovieDocumentOttInfo(kinoMovie.getOttInfo());
                    update.set("ottInfo", ottInfos);
                }

                // ✅ 극장 정보 업데이트
                if (kinoMovie.getTheaterLinks() != null && !kinoMovie.getTheaterLinks().isEmpty()) {
                    update.set("inTheater", true)
                            .set("theaterLinks", kinoMovie.getTheaterLinks());
                }

                // ✅ 크롤링 상태 업데이트
                update.set("isCrawled", true)
                        .set("lastCrawled", new Date());

                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, MovieDocument.class);

                boolean success = updateResult.getModifiedCount() > 0;
                result.put("ottInfo", kinoMovie.getOttInfo());
                result.put("theaterLinks", kinoMovie.getTheaterLinks());
                result.put("message", success ? "OTT 정보가 성공적으로 업데이트되었습니다." : "OTT 정보 업데이트에 실패했습니다.");

                if (success) {
                    System.out.println("✅ DB 업데이트 성공 - " + title);
                    System.out.println("  - OTT 정보: " + kinoMovie.getOttInfo());
                    System.out.println("  - 극장 정보: " + kinoMovie.getTheaterLinks());
                }

                return result; // 하나만 매칭되면 바로 리턴
            }
        }

        result.put("message", "키노라이츠와 일치하는 영화 정보를 찾을 수 없습니다.");
        return result;
    }

    private List<MovieDocument.OTTInfo> convertToMovieDocumentOttInfo(List<MovieDTO.OTTInfo> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        return dtoList.stream()
                .map(dto -> new MovieDocument.OTTInfo(
                        dto.getOttPlatform(),
                        dto.getAvailable(),
                        dto.getLink()))
                .collect(Collectors.toList());
    }
}