package com.nungil.Service;

import com.nungil.Dto.UserDTO;
import com.nungil.Enum.Gender;
import com.nungil.Repository.Interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userMapper;
    private final UserRepository userRepository;

    public boolean isEmailAlreadyRegistered(String email) {
        return userRepository.checkEmailExists(email);  // 이메일이 이미 등록되어 있는지 확인
    }

    // 회원가입
    public void registerUser(String email, String password, String nickname, Gender gender, int birthDate) {
        // 이메일 중복 체크
        if (isEmailAlreadyRegistered(email)) {
            throw new IllegalArgumentException("Email is already in use");  // 중복된 이메일 처리
        }

        // 이메일이 중복되지 않으면 사용자 등록
        UserDTO user = new UserDTO();
        user.setEmail(email);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setGender(gender);  // gender는 enum으로 설정
        user.setBirthDate(birthDate);

        userRepository.insertUser(user);  // 사용자 등록
    }


    // 비밀번호 암호화 (여기서는 예시로 BCrypt를 사용)
    private String encryptPassword(String password) {
        // BCrypt 또는 다른 암호화 로직 사용
        return password; // 암호화된 비밀번호 리턴 (예시)
    }

    public boolean authenticateUser(String email, String password) {
        System.out.println("Attempting login for email: " + email); // 디버깅
        UserDTO userDTO = userMapper.findByEmail(email);

        if (userDTO != null) {
            System.out.println("User found: " + userDTO.getEmail()); // 디버깅
            if (userDTO.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public UserDTO findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserDTO findOrCreateKakaoUser(UserDTO userDTO) {

        UserDTO existingUser = userRepository.findByKakaoId(userDTO.getKakaoId());

        if (existingUser != null) {
            return existingUser;
        }

        UserDTO newUser = new UserDTO();
        newUser.setKakaoId(userDTO.getKakaoId());
        newUser.setEmail(userDTO.getEmail());
        newUser.setNickname(userDTO.getNickname());
        newUser.setPassword("KAKAO_USER");  // 임시 비밀번호
        newUser.setGender(userDTO.getGender());
        newUser.setAdmin(false);

        // 3. DB에 저장 (userid는 자동 생성됨)
        userRepository.insertUser(newUser);
        return newUser;

    }

}
