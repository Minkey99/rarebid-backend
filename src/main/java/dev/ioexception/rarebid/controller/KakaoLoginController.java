package dev.ioexception.rarebid.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.ioexception.rarebid.dto.LoginResponse;
import dev.ioexception.rarebid.service.KakaoService;
import dev.ioexception.rarebid.utils.CookieUtil;
import dev.ioexception.rarebid.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {

        CookieUtil cookieUtil = new CookieUtil();
        JwtUtil jwtUtil = new JwtUtil();

        String location = "https://kauth.kakao.com/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + redirectUri + "&response_type=code";
        model.addAttribute("location", location);
        String userInfo = cookieUtil.getCookieValue(request, "userInfo");
        String JwtToken = cookieUtil.getCookieValue(request, "JwtToken");

        if (userInfo != null) {
            DecodedJWT decodedJWT = jwtUtil.decodeJwt(JwtToken);
            String subject = decodedJWT.getSubject();
            List<String> emailList = decodedJWT.getAudience();
            
            model.addAttribute("name", subject);
            model.addAttribute("email", emailList.get(0));

            return "redirect:/dashboard";
        } else {
            model.addAttribute("isLoggedIn", false);
            return "login";
        }
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {

        CookieUtil cookieUtil = new CookieUtil();

        log.info("Callback received with code: {}", code);
        LoginResponse loginResponse = kakaoService.kakaoLogin(code);

        log.info("LoginResponse: {}", loginResponse);
        cookieUtil.setCookie(response, "userInfo", loginResponse.getNickname(), 60 * 60); // 1 hour
        cookieUtil.setCookie(response, "accessToken", loginResponse.getAccessToken(), 60 * 60);
        cookieUtil.setCookie(response, "JwtToken", loginResponse.getJwtToken(), 60 * 60);

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpServletRequest request, Model model) {

        CookieUtil cookieUtil = new CookieUtil();
        JwtUtil jwtUtil = new JwtUtil();

        String JwtToken = cookieUtil.getCookieValue(request, "JwtToken");

        if (JwtToken != null) {
            DecodedJWT decodedJWT = jwtUtil.decodeJwt(JwtToken);
            String name = decodedJWT.getSubject();
            String email = decodedJWT.getAudience().get(0);

            model.addAttribute("name", name);
            model.addAttribute("email", email);

        } else {
            return "redirect:/login";
        }
        return "dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CookieUtil cookieUtil = new CookieUtil();
        String JwtToken = cookieUtil.getCookieValue(request, "JwtToken");
        String accessToken = cookieUtil.getCookieValue(request, "accessToken");

        if (accessToken != null) {

            log.info("accessToken is : {}" , accessToken);
            log.info("JwtToken is : {}" , JwtToken);

            kakaoService.logout(JwtToken, accessToken);
            cookieUtil.deleteCookie(response, "userInfo");
            cookieUtil.deleteCookie(response, "accessToken");
            log.info("Logout success");
        } else {
            log.info("Access token is null");
        }
        return "redirect:/login";
    }
}