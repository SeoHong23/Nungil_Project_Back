package com.nungil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()  // CSRF 보호 비활성화 (필요시 활성화)
                .authorizeHttpRequests()
                .requestMatchers("/login", "/register", "/check-email", "/public/**").permitAll()  // 공개 경로
                .requestMatchers("/api/**").permitAll()  // 공개 경로

                .anyRequest().authenticated();  // 인증된 사용자만 접근 가능

        return http.build();
    }
}
