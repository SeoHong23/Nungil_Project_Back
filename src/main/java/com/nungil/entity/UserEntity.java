package com.nungil.entity;


import com.nungil.Enum.Gender;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    private String userid;           // MongoDB에서 사용되는 고유 ID
    private String password;     // 비밀번호
    private String nickname;     // 닉네임
    private String email;        // 이메일
    private int birthDate;    // 생년월일
    private Gender gender;       // 성별

    public UserEntity(String password, String nickname, String email, int birthDate, Gender gender) {
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

}
