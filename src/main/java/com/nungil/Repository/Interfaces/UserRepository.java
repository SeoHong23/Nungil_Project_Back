package com.nungil.Repository.Interfaces;

import com.nungil.Dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRepository {

    // 이메일 중복 체크
    boolean checkEmailExists(@Param("email") String email);

    // 사용자 등록
    void insertUser(UserDTO user);

    UserDTO findByEmail(@Param("email") String email);

    UserDTO findByKakaoId(Long kakaoId);

    int save(UserDTO user);

    List<UserDTO> findAllUsers();

    void updateUserPassword(@Param("email") String email, @Param("password") String password);


}
