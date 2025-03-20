package com.nungil.Service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import com.nungil.Dto.UserDTO;
import com.nungil.Enum.Gender;
import com.nungil.Repository.Interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isEmailAlreadyRegistered(String email) {
        return userRepository.checkEmailExists(email);  // 이메일이 이미 등록되어 있는지 확인
    }

    // 회원가입
    public void registerUser(UserDTO dto) {
        // 이메일 중복 체크
        if (isEmailAlreadyRegistered(dto.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");  // 중복된 이메일 처리
        }

        String encodedPwd = passwordEncoder.encode(dto.getPassword());
log.info("Not Encoded password: " + dto.getPassword());
log.info("Encoded password: " + encodedPwd);
        dto.setPassword(encodedPwd);

        userRepository.insertUser(dto);  // 사용자 등록
    }

    public boolean authenticateUser(String email, String password) {
        System.out.println("Attempting login for email: " + email); // 디버깅
        UserDTO userDTO = userMapper.findByEmail(email);

        if (userDTO != null) {
            System.out.println("User found: " + userDTO.getEmail()); // 디버깅
            return passwordEncoder.matches(password, userDTO.getPassword());
        }
        return false;
    }


    public void encryptExistingPasswords() {
        List<UserDTO> users = userRepository.findAllUsers();

        for (UserDTO user : users) {
            String oldPassword = user.getPassword();

            if (!oldPassword.startsWith("$2a$")) {
                String newEncodedPassword = passwordEncoder.encode(oldPassword);
                user.setPassword(newEncodedPassword);
                userRepository.updateUserPassword(user.getEmail(), newEncodedPassword);
                System.out.println("비밀번호 암호화 업데이트" + user.getEmail());
            } else {
                System.out.println("이미 암호화되었습니다" + user.getEmail());
            }
        }
    }


    public UserDTO findUserByEmail(String email) {
        UserDTO user = userRepository.findByEmail(email);
        if(user != null && user.getNickname() != null) {
            user.setNickname(processNickname(user.getNickname()));
        }
            return user;
    }

    public UserDTO findOrCreateKakaoUser(String accessToken) {
        try {
            String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    kakaoUserInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONObject kakaoAccount = jsonObject.getJSONObject("kakao_account");

            // 🚀 카카오 유저 정보 가져오기
            Long kakaoId = jsonObject.getLong("id");
            String email = kakaoAccount.has("email") ? kakaoAccount.getString("email") : kakaoId + "@kakao.user";
            JSONObject profile = kakaoAccount.getJSONObject("profile");
            String nickname = profile.getString("nickname");

            int birthDate = 0;

            if (kakaoAccount.has("birthDate")) {
                birthDate = kakaoAccount.getInt("birthDate");
            } else if (kakaoAccount.has("birthyear") && kakaoAccount.has("birthday")) {
                String birthyear = kakaoAccount.getString("birthyear");
                String birthday = kakaoAccount.getString("birthday");
                birthDate = Integer.parseInt(birthyear + birthday);
            } else if (kakaoAccount.has("birthday")) {
                birthDate = Integer.parseInt(kakaoAccount.getString("birthday"));
            }

            Gender gender = Gender.MALE;
            if (kakaoAccount.has("gender")) {
                String genderStr = kakaoAccount.getString("gender").toUpperCase();
                if (genderStr.equals("MALE") || genderStr.equals("FEMALE")) {
                    gender = Gender.valueOf(genderStr);
                }
            }
            UserDTO userDTO = userRepository.findByKakaoId(kakaoId); // 여기서 호출

            if (userDTO != null) {
                return userDTO;
            } else {
                // 사용자가 없으면 새 사용자 등록
                userDTO = new UserDTO();
                userDTO.setEmail(email);
                userDTO.setNickname(nickname);
                userDTO.setGender(gender);
                userDTO.setKakaoId(kakaoId);
                userDTO.setBirthDate(birthDate);
                // 사용자 등록 (예: 비밀번호는 카카오에서 제공하지 않으므로 임의로 설정)
                userDTO.setPassword("defaultPassword");  // 비밀번호는 임의로 설정
                int result = userRepository.save(userDTO);
                if (result > 0) {
                    // 저장 성공 시 필요한 로직 수행
                    System.out.println("사용자 저장 성공!");
                } else {
                    // 저장 실패 처리
                    System.out.println("사용자 저장 실패!");
                }
                return userDTO;
            }
        } catch (Exception e) {
            throw new RuntimeException("카카오 로그인 처리 중 오류: " + e.getMessage(), e);
        }
    }

    public UserDTO getUserByKakaoId(Long kakaoId) {
        UserDTO user = userRepository.findByKakaoId(kakaoId);
        return user;
    }


    private String processNickname(String nickname) {
        if (nickname == null) {
            return null;
        }

        try {
            byte[] bytes = nickname.getBytes("UTF-8");
            String decodedNickname = new String(bytes, "UTF-8");

            System.out.println("원래 닉네임: " + nickname);
            System.out.println("처리된 닉네임: " + decodedNickname);

            return decodedNickname;
        } catch (Exception e) {
            System.out.println("닉네임 인코딩 처리 중 오류 발생: " + e.getMessage());
            return nickname;
        }
    }

}
