package com.iamnana.project.security.jwt;

import com.iamnana.project.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtils {

    private static final Logger log = LoggerFactory.getLogger(JWTUtils.class);

    @Value("${spring.app.jwtSecretKey}")
    private String jwtSecretKey;

    @Value("${spring.app.jwtExpirationTokenMs}")
    private int expirationTimeMs;

    @Value("${spring.application.jwtCookieName}")
    private String jwtCookie;


    // Method to get token from cookie
    public String getJwtFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        }else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
        String jwt = generateTokenFromUsernameForCookie(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();

        return cookie;
    }

    public ResponseCookie generateCleanCookie(){
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, "")
                .path("/api")
                .build();

        return cookie;
    }

    public String generateTokenFromUsernameForCookie(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + expirationTimeMs)))
                .signWith(key())
                .compact();
    }


    // Getting username from jwt token
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate Token
    public boolean validateToken(String authToken) {
        try{
            System.out.println("Validating!!");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        } catch (MalformedJwtException ex){
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex){
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex){
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex){
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }

        return false;
    }

    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecretKey)
        );
    }

}
