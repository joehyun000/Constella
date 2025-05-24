package com.core.constella.api.mypage.dto;

import com.core.constella.api.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    private Long id;
    private String username;

    public static MyPageResponse from(User user) {
        return MyPageResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
