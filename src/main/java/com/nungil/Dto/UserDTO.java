package com.nungil.Dto;

import com.nungil.Enum.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@ToString
@NoArgsConstructor
@Setter
@Getter
public class UserDTO {

    private int userid;
    private String password;     // 비밀번호
    private String nickname;     // 닉네임
    private String email;        // 이메일
    private int birthDate;    // 생년월일
    private Gender gender;       // 성별
    private boolean admin;
    private String kakaoId; // 카카오로그인시 id

    public UserDTO(int userid, String password, String nickname, String email, int birthDate, Gender gender,boolean admin, String kakaoId) {
        this.userid = userid;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
        this.admin = admin;
        this.kakaoId = kakaoId;
    }
}
