package com.nungil.Document;

import com.nungil.Enum.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserDocument {

    @Id
    private String userid;           // MongoDB에서 사용되는 고유 ID
    private String password;     // 비밀번호
    private String nickname;     // 닉네임
    private String email;        // 이메일
    private int birthYear;    // 생년월일
    private Gender gender;       // 성별

    public UserDocument(String password, String nickname, String email, int birthDate, Gender gender) {
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.birthYear = birthYear;
        this.gender = gender;
    }

}
