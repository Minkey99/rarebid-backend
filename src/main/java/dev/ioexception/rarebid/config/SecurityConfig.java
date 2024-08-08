package dev.ioexception.rarebid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        //                                .logoutSuccessUrl("/login")  // 리디렉션 URL 설정
        //                                .invalidateHttpSession(true)
        ////                                .deleteCookies("JSESSIONID")
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll()
                )
                .logout(AbstractHttpConfigurer::disable
                );
        return http.build();
    }
}