package com.addamistry.addamistry.config;

import com.addamistry.addamistry.collection.RefreshToken;
import com.addamistry.addamistry.collection.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

;

@Service
@Log4j2
public class JwtService {
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;

    public JwtService(@Value("${refreshTokenExpirationDays}") int refreshTokenExpirationDays, @Value("${accessTokenExpirationDays}") int accessTokenExpirationDays) {
        accessTokenExpirationMs = (long) accessTokenExpirationDays * 24 * 60 * 60 * 1000;
//        accessTokenExpirationMs = (long) accessTokenExpirationMinutes * 60 * 1000;
        refreshTokenExpirationMs = (long) refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
//        accessTokenVerifier = JWT.require(accessTokenAlgorithm)
//                .withIssuer(issuer)
//                .build();
//        refreshTokenVerifier = JWT.require(refreshTokenAlgorithm)
//                .withIssuer(issuer)
//                .build();

    }

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }


    private Date extractExpiration(String token) {
        return getClaim(token,Claims::getExpiration);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {

        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    public String generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("PROVIDER"))) {
            claims.put("isProvider", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("CUSTOMER"))) {
            claims.put("isCustomer", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ADMIN"))) {
            claims.put("isAdmin", true);
        }
        return  generateToken(claims,userDetails);
    }


    public List<SimpleGrantedAuthority> getRolesFromToken(String authToken) {
        List<SimpleGrantedAuthority> roles = null;
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(authToken).getBody();
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isProvider = claims.get("isProvider", Boolean.class);
        Boolean isCustomer = claims.get("isCustomer", Boolean.class);
        if (isAdmin != null && isAdmin == true) {
            roles = Arrays.asList(new SimpleGrantedAuthority("ADMIN"));
        }
        if (isCustomer != null && isCustomer == true) {
            roles = Arrays.asList(new SimpleGrantedAuthority("CUSTOMER"));
        }
        if (isProvider != null && isProvider == true) {
            roles = Arrays.asList(new SimpleGrantedAuthority("PROVIDER"));
        }
        return roles;
    }
    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(new Date().getTime() + accessTokenExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public  String generateRefreshToken(Users userDetails, RefreshToken refreshToken){
        return generateRefreshToken(new HashMap<>(),userDetails,refreshToken);
    }

    public String generateRefreshToken(Map<String, Object> claims, Users userDetails,RefreshToken refreshToken) {
        claims.put("tokenId",refreshToken.getId());

        System.out.println(refreshToken.getId());
        return Jwts.builder().setClaims(claims)
        .setSubject(userDetails.getId()).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(new Date().getTime() + refreshTokenExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();

    }



    private Key getSignInKey() {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

    }

    public String getTokenIdFromRefreshToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody().get("tokenId").toString();
    }
    public String getUserIdFromRefreshToken(String token){
        System.out.println(getClaim(token,Claims::getSubject));
        return getClaim(token,Claims::getSubject);
    }
}
