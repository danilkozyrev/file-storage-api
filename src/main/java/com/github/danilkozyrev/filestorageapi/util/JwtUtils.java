package com.github.danilkozyrev.filestorageapi.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class for generating and validating JSON Web Tokens.
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    /**
     * Generates JWT with the given claims and signed with the secret key.
     *
     * @param subject    the subject ("sub") value.
     * @param expiration token expiration date.
     * @param secret     the secret key.
     * @return a new token.
     */
    public static String generateToken(
            String subject, Instant expiration, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT
                .create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(subject)
                .withExpiresAt(java.sql.Date.from(expiration))
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    /**
     * Validates JWT.
     *
     * @param token  the token to verify.
     * @param secret the secret key.
     * @return the subject ("sub") value.
     * @throws JWTVerificationException if any of the verification steps fail.
     */
    public static String verifyToken(String token, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm).build().verify(token).getSubject();
    }

}
