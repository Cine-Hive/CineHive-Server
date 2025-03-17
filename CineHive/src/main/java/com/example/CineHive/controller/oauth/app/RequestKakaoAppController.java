package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
앱에서 SDK를 실행해서 인증 및 로그인이 성공된 후, Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보냄
이때, 신규 회원이면 201로 반환하여 추가 기입 정보를 반환, 200이면 바로 로그인 (앱에서 201과 200으로 처리하면 될 거 같음)

*** KakaoUser 테이블 -> User 테이블 하나로 관리하기 위해 관련 코드 수정 필요 ***
 */
@Tag(name = "Kakao User App Controller", description = "카카오 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestKakaoAppController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private JwtUtil jwtUtil;

}
