package com.core.constella.api.mypage.service;

import com.core.constella.api.mypage.dto.MyPageResponse;
import com.core.constella.api.mypage.dto.MyPageUpdateRequest;
import com.core.constella.api.user.domain.User;
import com.core.constella.api.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    //로그인 확인용(session) 받아오기용
    private Long getLoginUserId(HttpSession session) {
        Object id = session.getAttribute("loginUserId");
        if (id == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return (Long) id;
    }

    //마이페이지 조회
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageInfo(HttpSession session) {
        Long userId = getLoginUserId(session);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return MyPageResponse.from(user);
    }

    //마이페이지 수정
    @Transactional
    public MyPageResponse updateMyPageInfo(MyPageUpdateRequest request, HttpSession session) {
        Long userId = getLoginUserId(session);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateUsername(request.getUsername());
        return MyPageResponse.from(user);
    }
}