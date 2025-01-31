package com.nungil.Controller;

import com.nungil.Document.UserDocument;
import com.nungil.Dto.UserDTO;
import com.nungil.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

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
                    userDTO.getBirthYear()
            );
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // 이메일 중복 시 에러 메시지 반환
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO) {
        System.out.println("Received email: " + userDTO.getEmail());
        System.out.println("Received password: " + userDTO.getPassword());
        boolean isAuthenticated = userService.authenticateUser(userDTO.getEmail(), userDTO.getPassword());

        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.isEmailAlreadyRegistered(email);
        return ResponseEntity.ok(exists);
    }

}
