package dev.ioexception.rarebid.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtUtil {
    public DecodedJWT decodeJwt(String token) {
        return JWT.decode(token);
    }
}