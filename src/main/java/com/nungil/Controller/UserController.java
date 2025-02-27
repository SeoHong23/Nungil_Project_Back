package com.nungil.Controller;

import com.nungil.Dto.UserDTO;
import com.nungil.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        try {
            userService.registerUser(
                    userDTO.getEmail(),
                    userDTO.getPassword(),
                    userDTO.getNickname(),
                    userDTO.getGender(),
                    userDTO.getBirthDate()
            );
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // 이메일 중복 시 에러 메시지 반환
        }
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody UserDTO userDTO) {
        boolean isAuthenticated = userService.authenticateUser(userDTO.getEmail(), userDTO.getPassword());

        Map<String, Object> response = new HashMap<>();

        if (isAuthenticated) {
            UserDTO user = userService.findUserByEmail(userDTO.getEmail());

            // 디버깅 로그 추가
            System.out.println("User details: " + user);  // user 객체를 출력하여 값이 제대로 들어오는지 확인
            System.out.println("Response before adding data: " + response);
            log.info("User details: " + user);

            response.put("message", "Login successful");
            response.put("userId", user.getUserid());
            response.put("email", user.getEmail());
            response.put("nickname", user.getNickname());
            response.put("birthYear", user.getBirthDate());
            response.put("gender", user.getGender().toString());
            response.put("admin", user.isAdmin());

            // JSON 형식으로 반환
            return ResponseEntity.ok(response); // JSON 형식으로 반환
        } else {
            response.put("message", "Invalid email or password");

            // 디버깅 로그 추가
            System.out.println("Invalid login attempt: " + userDTO.getEmail());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // JSON 형식으로 반환
        }
    }
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.isEmailAlreadyRegistered(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestBody Map<String, String> request) {
        log.info("✅ 카카오 로그인 요청 도착! 데이터: {}", request);

        Map<String, Object> response = new HashMap<>();
        try {
            String accessToken = request.get("accessToken");
            log.info("카카오 액세스 토큰: {}", accessToken);

            UserDTO user = userService.findOrCreateKakaoUser(accessToken);

            response.put("message", "Login successful");
            response.put("userId", user.getUserid());
            response.put("email", user.getEmail());
            response.put("nickname", user.getNickname());
            response.put("birthYear", user.getBirthDate());
            response.put("gender", user.getGender().toString());
            response.put("admin", user.isAdmin());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            System.out.println("Error in kakao login: " + e.getMessage());
            e.printStackTrace();
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/kakao/{kakaoId}")
    public ResponseEntity<UserDTO> findByKakaoIdTest(@PathVariable Long kakaoId) {
        log.debug("🔍 컨트롤러 - findByKakaoId 호출: kakaoId = {}", kakaoId);
        UserDTO user = userService.getUserByKakaoId(kakaoId);
        log.debug("✅ 컨트롤러 - findByKakaoId 결과: {}", user);
        return ResponseEntity.ok(user);
    }

}
