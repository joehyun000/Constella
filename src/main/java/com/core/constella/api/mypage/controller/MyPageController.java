package com.core.constella.api.mypage.controller;

import com.core.constella.api.mypage.dto.MyPageResponse;
import com.core.constella.api.mypage.dto.MyPageUpdateRequest;
import com.core.constella.api.mypage.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    //마이페이지 조회
    @GetMapping
    public MyPageResponse getMyPageInfo(HttpSession session) {
        return myPageService.getMyPageInfo(session);
    }

    //마이페이지 수정
    @PatchMapping
    public MyPageResponse updateMyPageInfo(@RequestBody MyPageUpdateRequest request, HttpSession session) {
        return myPageService.updateMyPageInfo(request, session);
    }
}
