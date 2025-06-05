package com.core.constella.api.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // Auto Increment 고유 ID

    @Column(nullable = false, unique = true, length = 30)
    private String username; // 사용자 로그인용 ID

    @Column(nullable = false, length = 255)
    private String password; // 해시된 비밀번호

    @Column(length = 30)
    private String constellation; // 별자리 정보

    public static User create(String username, String passwordHash) {
        return User.builder()
                .username(username)
                .password(passwordHash)
                .build();
    }
}