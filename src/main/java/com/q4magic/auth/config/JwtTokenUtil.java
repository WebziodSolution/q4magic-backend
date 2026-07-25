package com.q4magic.auth.config;

import com.q4magic.common.models.Customers;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${app.JWT.SECRET_KEY}")
    public String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return Integer.parseInt(claims.get("userId").toString());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(Customers user) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", user.getId());
        if (user.getRole() != null) {
            claims.put("roleId", user.getRole().getId());
        }
        claims.put("email", user.getEmailAddress());
        claims.put("username", user.getUsername());

        return createToken(claims, user.getEmailAddress());
    }

//    private String createToken(Map<String, Object> claims, String subject) {
//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
//    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Boolean validateToken(String token, Map<String, Object> user) {
        final String username = extractUsername(token);
        return (username.equals(user.get("email")) && !isTokenExpired(token));
    }
}
