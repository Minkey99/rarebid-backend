package dev.ioexception.rarebid.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class

LoginResponse {

    private String nickname;
    private String email;
    private String jwtToken;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public LoginResponse(String nickname, String email, KakaoTokenResponse tokens) {

        this.nickname = nickname;
        this.email = email;
        this.accessToken = tokens.accessToken;
        this.refreshToken = tokens.refreshToken;
        this.expiresIn = tokens.expiresIn;
    }
    /* include jwt Token */
    public LoginResponse(String nickname, String email, String jwtToken, KakaoTokenResponse tokens) {

        this.nickname = nickname;
        this.email = email;
        this.jwtToken = jwtToken;
        this.accessToken = tokens.accessToken;
        this.refreshToken = tokens.refreshToken;
        this.expiresIn = tokens.expiresIn;
    }
}