package com.core.constella.api.user.service;

import com.core.constella.api.user.domain.User;
import com.core.constella.api.user.dto.LoginRequest;
import com.core.constella.api.user.dto.LoginResponse;
import com.core.constella.api.user.dto.RegisterRequest;
import com.core.constella.api.user.dto.RegisterResponse;
import com.core.constella.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.create(request.getUsername(), encodedPassword);
        userRepository.save(user);

        return new RegisterResponse(user.getId(), user.getUsername(), "회원가입 성공");
    }

    /**
     * 로그인 검증
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return new LoginResponse(user.getId(), user.getUsername(), "로그인 성공");
    }

    /**
     * 아이디로 사용자 조회 (참조용)
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다: " + username));
    }
}
