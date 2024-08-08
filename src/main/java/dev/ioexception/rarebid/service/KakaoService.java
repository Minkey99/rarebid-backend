package dev.ioexception.rarebid.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ioexception.rarebid.dto.KakaoTokenResponse;
import dev.ioexception.rarebid.dto.LoginResponse;
import dev.ioexception.rarebid.entity.User;
import dev.ioexception.rarebid.repository.UserRepository;
import dev.ioexception.rarebid.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final UserRepository userRepository;
    private final WebClient webClient;

    @Value("${kakao.client_id}")
    private String clientId;

    private static final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private static final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";

    public KakaoTokenResponse getAccessTokenFromKakao(String code) {

        KakaoTokenResponse kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        response -> response.bodyToMono(String.class).map(RuntimeException::new))
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                        response -> response.bodyToMono(String.class).map(RuntimeException::new))
                .bodyToMono(KakaoTokenResponse.class)
                .block();

        log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto;
    }

//    public KakaoUserInfoResponse getUserInfo(String accessToken) {
//
//        KakaoUserInfoResponse userInfo = WebClient.create(KAUTH_USER_URL_HOST)
//                .get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/v2/user/me")
//                        .build())
//                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                .retrieve()
//                .onStatus(HttpStatus.BAD_REQUEST::equals,
//                        response -> response.bodyToMono(String.class).map(RuntimeException::new))
//                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
//                        response -> response.bodyToMono(String.class).map(RuntimeException::new))
//                .bodyToMono(KakaoUserInfoResponse.class)
//                .block();
//
//        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
//        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
//        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
//
//        return userInfo;
//    }

//    public LoginResponse kakaoLogin(String code) {
//        // 1. "인가 코드"로 "액세스 토큰" 요청
//        String accessToken = getAccessTokenFromKakao(code);
//        // 2. 토큰으로 카카오 API 호출
//        KakaoUserInfoResponse userInfo = getUserInfo(accessToken);
//        // 3. 카카오ID로 회원가입 & 로그인 처리
//        return login(userInfo);
//    }

    public LoginResponse kakaoLogin(String code) {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        KakaoTokenResponse kakaoTokenResponse = getAccessTokenFromKakao(code);
        // 2. 토큰으로 카카오 API 호출
        //KakaoUserInfoResponse userInfo = getUserInfo(kakaoTokenResponse.accessToken);
        // 3. 카카오ID로 회원가입 & 로그인 처리
        return kakaoUserLogin(kakaoTokenResponse);
    }

    private LoginResponse kakaoUserLogin(KakaoTokenResponse kakaoTokenResponse) {

        String idToken = kakaoTokenResponse.idToken;
        JwtUtil jwt = new JwtUtil();
        DecodedJWT decodedJWT = jwt.decodeJwt(idToken);

        String sub = decodedJWT.getClaim("sub").toString();
        String decodeEmail = decodedJWT.getClaim("email").toString();
        String decodeNickName = decodedJWT.getClaim("nickname").toString();

//        System.out.println(idToken);
        Optional<User> kakaoUser = userRepository.findByEmail(decodeEmail);

        Algorithm algorithm = Algorithm.HMAC256("123445342524543534525234344342343424");
        String token = JWT.create()
                        .withIssuer("lee")
                        .withAudience(decodeEmail)
                        .withSubject(decodeNickName)
                        .withIssuedAt(new Date())
                        .withExpiresAt(new Date(kakaoTokenResponse.expiresIn))
                        .sign(algorithm);

        LoginResponse loginJwtResponse = new LoginResponse(decodeEmail, decodeEmail, token, kakaoTokenResponse);

//        if (kakaoUser == null) { // 회원가입
//            kakaoUser = new User();
//            kakaoUser.setUid(uid);
//            kakaoUser.setEmail(kakaoEmail);
//            kakaoUser.setNickname(nickName);
//            kakaoUser.setLoginType("kakao");
//            userRepository.save(kakaoUser);
//        }

        // jwt 회원가입
        if (kakaoUser.isEmpty()) {
            User user = new User();
            user.setSub(sub);
            user.setEmail(decodeEmail);
            user.setNickname(decodeNickName);
            user.setLoginType("kakao");
            userRepository.save(user);
        }
//        System.out.println(token);
//        return new LoginResponse(uid, nickName, kakaoEmail, authTokens);
//        return new LoginResponse(decodeNickName, decodeEmail, kakaoTokenResponse);
        return loginJwtResponse;
    }

    public void logout(String JwtToken ,String accessToken) throws IOException {

        JwtUtil jwtUtil = new JwtUtil();
        DecodedJWT decodedJWT = jwtUtil.decodeJwt(JwtToken);

       // System.out.println(decodedJWT.getClaim("accessToken"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", BEARER_PREFIX + accessToken);
        headers.add("Content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        HttpEntity<Void> kakaoLogoutRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                LOGOUT_URL,
                HttpMethod.POST,
                kakaoLogoutRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        log.info("Logout response id: {}", id);
    }
}