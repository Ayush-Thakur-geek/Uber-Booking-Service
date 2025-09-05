package com.uber.UberBookingService.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Log4j2
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    private Claims extractAllPayLoads(String token) {
        return Jwts
                .parser()
                .verifyWith(getSECRET())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSECRET() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String extractEmailFromToken(String token) {
        return extractAllPayLoads(token).get("email").toString();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllPayLoads(token);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            extractAllPayLoads(token);
            if (isTokenExpired(token)) {
                return false;
            }
        }  catch (Exception ex) {
            log.error(ex);
            return false;
        }
        return true;
    }
}
