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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MongoTemplate mongoTemplate;
    private final MovieRepository movieRepository;
    private final KinoService kinoService;

    @Autowired
    public MovieService(MongoTemplate mongoTemplate, MovieRepository movieRepository,KinoService kinoService) {
        this.mongoTemplate = mongoTemplate;
        this.movieRepository = movieRepository;
        this.kinoService = kinoService;
    }

    /**
     * 🕒 저장된 영화 목록을 기반으로 주기적으로 크롤링하여 업데이트
     * - 매일 새벽 3시에 실행
     */
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

                Map<String, Object> updateResult = updateOttInfo(movie.getTitle());
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

    /**
     * 특정 페이지에서 영화 목록을 가져오는 메서드
     */
    private List<MovieDocument> getMoviesFromPage(int page) {
        // 페이지네이션을 적용하여 가져오기
        Query query = new Query(Criteria.where("isCrawled").is(false))
                .limit(100) // 한 페이지당 10개씩 가져오기 (조절 가능)
                .skip((page - 1) * 10); // 페이지 번호에 맞게 offset 설정

        return mongoTemplate.find(query, MovieDocument.class);
    }



    @PostConstruct
    public void ensureTextIndex() {
        mongoTemplate.indexOps("video")
                .ensureIndex(new Index().on("title", org.springframework.data.domain.Sort.Direction.ASC));
        System.out.println("✅ title 필드에 텍스트 인덱스가 생성되었습니다.");
    }


    public MovieDocument findMovieByTitle(String title) {
        Query query = new Query(Criteria.where("title")
                .regex(title, "i"));
        return mongoTemplate.findOne(query, MovieDocument.class);
    }
    private String normalize(String input) {
        if (input == null) return ""; // Null 처리 추가
        return input.replaceAll("[^a-zA-Z0-9가-힣]", "")
        .toLowerCase(); // 공백 및 특수문자 제거

    }


    /**
     * MongoDB에 저장된 영화의 OTT 정보를 업데이트하는 메서드
     */
    public Map<String, Object> updateOttInfo(String title) {
        Map<String, Object> result = new HashMap<>();

        // 1️⃣ MongoDB에서 영화 조회
        Query query = new Query(Criteria.where("title")
                        .regex("^\\s*" + title.trim()
                        .replaceAll("[^a-zA-Z0-9가-힣]", "") + "\\s*$", "i"));
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

        // 3️⃣ OTT 정보 업데이트
        MovieDTO matchedMovie = kinoMovies.stream()
                .filter(kinoMovie -> normalize(kinoMovie.getTitle()).equals(normalize(existingMovie.getTitle())) &&
                        kinoMovie.getReleaseDate().equals(existingMovie.getReleaseDate())) // 개봉일 비교
                .findFirst()
                .orElse(null);

        if (matchedMovie != null) {
            boolean updateSuccess = updateOTTLinksByTitle(existingMovie.getTitle(), matchedMovie.getOttInfo(),  matchedMovie.getTheaterLinks());


            result.put("ottInfo", matchedMovie.getOttInfo());
            result.put("theaterLinks", matchedMovie.getTheaterLinks()); // 영화관 정보 포함
            result.put("message", updateSuccess ? "OTT 정보가 성공적으로 업데이트되었습니다." : "OTT 정보 업데이트에 실패했습니다.");
        } else {
            result.put("message", "키노라이츠와 일치하는 영화 정보를 찾을 수 없습니다.");
            result.put("kinoTitles", kinoMovies.stream().map(MovieDTO::getTitle).collect(Collectors.toList()));
        }

        return result;
    }


    /**
     * MongoDB에서 영화 제목이 존재할 경우 OTT 정보를 업데이트
     */
    public boolean updateOTTLinksByTitle(String title, List<MovieDTO.OTTInfo> ottInfoList, List<String> theaterLinks) {
        // 제목 정규화
        String normalizedTitle = title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();

        // MongoDB에서 대소문자 구분 없이 공백 제거 후 제목 검색
//        Query query = new Query(Criteria.where("title")
//                .regex("^\\s*" + normalizedTitle + "\\s*$", "i")); // 대소문자 무시 (i)
//                .regex(normalizedTitle, "i")); // 🔥 부분 일치 검색으로 변경
        Query query = new Query(Criteria.where("title")
                .regex(".*" + normalizedTitle + ".*", "i")); // 🔥 부분 일치 검색

//        // 🔥 theaterLinks를 직접 사용하도록 수정
//        Update update = new Update()
//                .set("ottInfo", convertToMovieDocumentOttInfo(ottInfoList))
//                .set("inTheater", theaterLinks != null && !theaterLinks.isEmpty()) // 영화관 상영 여부 설정
//                .set("theaterLinks", theaterLinks); // 🔥 예매 링크 직접 추가


        Update update = new Update();

        // 🎟️ 예매 정보가 있으면 MongoDB에 저장
        if (!theaterLinks.isEmpty()) {
            update.set("inTheater", true)
                    .set("theaterLinks", theaterLinks);
        }

        // ✅ OTT 정보가 있으면 추가 (예매 정보가 없어도 OTT만 저장 가능)
        if (!ottInfoList.isEmpty()) {
            update.set("ottInfo", convertToMovieDocumentOttInfo(ottInfoList));
        }



        UpdateResult result = mongoTemplate.updateFirst(query, update, MovieDocument.class);

        System.out.println("🔍 MongoDB 업데이트 결과 - 매칭된 문서 수: " + result.getMatchedCount());
        System.out.println("🔍 MongoDB 업데이트 결과 - 수정된 문서 수: " + result.getModifiedCount());

        if (result.getMatchedCount() > 0) {
            System.out.println("✅ OTT 및 영화관 정보가 업데이트됨: " + title);
            return true;
        } else {
            System.out.println("🚨 업데이트 실패 (제목 불일치 가능성 있음): " + title);
            return false;
        }
    }


    /**
     * DTO → MovieDocument의 OTTInfo 변환 메서드
     */
    private List<MovieDocument.OTTInfo> convertToMovieDocumentOttInfo(List<MovieDTO.OTTInfo> dtoList) {
        return dtoList.stream()
                .map(dto -> new MovieDocument.OTTInfo(dto.getOttPlatform(), dto.getAvailable(), dto.getLink()))
                .collect(Collectors.toList());
    }

//    /**
//     * OTT 정보를 크롤링하여 업데이트 후 반환
//     */
//    private MovieDocument updateOttInfoForMovie(MovieDocument movie) {
//        // 키노라이츠 크롤링
//        List<MovieDTO> kinoMovies = kinoService.fetchMoviesByTitle(movie.getTitle());
//
//        if (!kinoMovies.isEmpty()) {
//            MovieDTO kinoMovie = kinoMovies.get(0); // 첫 번째 크롤링 결과 사용
//
//            // OTT 정보 업데이트
//            updateOTTLinksByTitle(movie.getTitle(), kinoMovie.getOttInfo(), kinoMovie.getTheaterLinks());
//
//            // 업데이트된 MongoDB 데이터 반환
//            return movieRepository.findByTitle(movie.getTitle()).orElse(movie);
//        }
//
//        return movie; // OTT 정보 없이 반환
//    }
}
