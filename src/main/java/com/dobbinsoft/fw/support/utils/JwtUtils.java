package com.dobbinsoft.fw.support.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Map;

@Slf4j
public class JwtUtils {

    public static String createRSA256(
            Map<String, String> headers,
            Map<String, String> payload,
            Integer expireSeconds, String privateKey) throws InvalidKeySpecException {
        Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) getPrivateKey(privateKey));
        return createCommon(headers, payload, expireSeconds, algorithm);
    }

    public static JwtResult verifyRSA256(String token, String publicKey, ObjectMapper objectMapper) {
        try {
            return verifyCommon(token, Algorithm.RSA256((RSAPublicKey) getPublicKey(publicKey), null), objectMapper);
        } catch (InvalidKeySpecException e) {
            log.warn("[verifyRSA256] invalid public key");
            JwtResult jwtResult = new JwtResult();
            jwtResult.result = Result.INVALID;
            return jwtResult;
        }
    }

    public static String createHMAC256(
            Map<String, String> headers,
            Map<String, String> payload,
            Integer expireSeconds, String secret) {
        return createCommon(headers, payload, expireSeconds, Algorithm.HMAC256(secret));
    }

    public static JwtResult verifyHMAC256(String token, String secret, ObjectMapper objectMapper) {
        return verifyCommon(token, Algorithm.HMAC256(secret), objectMapper);
    }

    private static String createCommon(
            Map<String, String> headers,
            Map<String, String> payload,
            Integer expireSeconds, Algorithm algorithm) {
        // 过期时间，60s
        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.SECOND, expireSeconds);

        // 第一部分Header
        JWTCreator.Builder builder = JWT.create()
                .withHeader((Map)headers);

        // 第二部分Payload
        payload.forEach(builder::withClaim);

        // 第三部分Signature
        return builder
                .withExpiresAt(expires.getTime())
                .sign(algorithm);
    }

    private static JwtResult verifyCommon(String token, Algorithm algorithm, ObjectMapper objectMapper) {
        try {
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            DecodedJWT verify = jwtVerifier.verify(token);
            log.info(verify.getHeader());
            String headerString = new String(Base64.getUrlDecoder().decode(verify.getHeader()), StandardCharsets.UTF_8);
            String payloadString = new String(Base64.getUrlDecoder().decode(verify.getPayload()), StandardCharsets.UTF_8);
            JwtResult jwtResult = new JwtResult();
            if (StringUtils.isNotEmpty(headerString)) {
                jwtResult.header = objectMapper.readValue(headerString, new TypeReference<Map<String, String>>(){});
            }
            if (StringUtils.isNotEmpty(payloadString)) {
                jwtResult.payload = objectMapper.readValue(payloadString, new TypeReference<Map<String, String>>(){});
            }
            if (verify.getExpiresAt().getTime() < System.currentTimeMillis() / 1000) {
                jwtResult.result = Result.EXPIRED;
                return jwtResult;
            }
            jwtResult.result = Result.SUCCESS;
            return jwtResult;
        } catch (TokenExpiredException e) {
            // token 过期
            JwtResult jwtResult = new JwtResult();
            jwtResult.result = Result.EXPIRED;
            return jwtResult;
        } catch (JWTVerificationException e) {
            JwtResult jwtResult = new JwtResult();
            jwtResult.result = Result.INVALID;
            return jwtResult;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static PrivateKey getPrivateKey(String key) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = null;
        KeyFactory keyFactory = null;
        try {
            byte[] keyBytes;
            keyBytes = Base64.getDecoder().decode(key);
            keySpec = new PKCS8EncodedKeySpec(keyBytes);
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException");
        }
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String key) throws InvalidKeySpecException {
        X509EncodedKeySpec keySpec = null;
        KeyFactory keyFactory = null;
        try {
            byte[] keyBytes;
            keyBytes = Base64.getDecoder().decode(key);
            keySpec = new X509EncodedKeySpec(keyBytes);
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException");
        }
        return keyFactory.generatePublic(keySpec);
    }

    @Getter
    @Setter
    @ToString
    public static class JwtResult {

        private Result result;

        private Map<String, String> payload;

        private Map<String, String> header;

    }

    public enum Result {
        SUCCESS,
        EXPIRED,
        INVALID
    }

}
