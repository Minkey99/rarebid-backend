package dev.ioexception.rarebid.client;

import dev.ioexception.rarebid.config.KakaoFeignConfiguration;
import dev.ioexception.rarebid.dto.KakaoInfo;
import dev.ioexception.rarebid.dto.KakaoToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@FeignClient(name = "kakaoClient", url = "https://kauth.kakao.com",configuration = KakaoFeignConfiguration.class)
public interface KakaoClient {

    @PostMapping
    KakaoInfo getInfo(URI baseUrl, @RequestHeader("Authorization") String accessToken);

    @PostMapping
    KakaoToken getToken(
                        URI baseUrl,
                        @RequestParam("client_id") String restApiKey,
                        @RequestParam("redirect_uri") String redirectUrl,
                        @RequestParam("code") String code,
                        @RequestParam("grant_type") String grantType
    );

}
