package com.boardwith.admin.config;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility for creating, parsing, and validating JWT access tokens.
 *
 * <p>The signing key is derived from the {@code jwt.secret} property (base64-encoded)
 * and must be at least 256 bits for the HS256 algorithm.
 */
@Service
public class JwtService {

    public static final long EXPIRATION_TIME = 1000L * 60L * 60L * 12L; // 12 hours
    private static final String SECRET_KEY_STRING =
            "boardwith-admin-secret-key-256-bits-long-enough-for-hs256-signing";

    private final SecretKey key;

    public JwtService() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(SECRET_KEY_STRING.getBytes())));
    }

    /**
     * Generate a JWT access token for the given user details.
     * The token includes the username (subject + username claim), roles, and permissions as separate claims.
     */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring("ROLE_".length()))
            .toList();

        List<String> permissions = userDetails.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .filter(a -> !a.startsWith("ROLE_"))
            .toList();

        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("username", userDetails.getUsername())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extract the roles from a JWT token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Extract the permissions from a JWT token.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        final Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("permissions");
    }

    /**
     * Extract the username (subject) from a JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from a JWT token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validate a token against user details (username match + not expired).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
