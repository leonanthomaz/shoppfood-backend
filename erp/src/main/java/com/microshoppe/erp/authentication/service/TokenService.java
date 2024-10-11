package com.microshoppe.erp.authentication.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Log4j2
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private UserRepository userRepository;

    public String createToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("login")
                    .withSubject(user.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            log.error("Erro na geração do Token para o usuário: {}", exception.getMessage());
            throw new RuntimeException("Erro na geração do Token para o usuário.", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("login")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            log.error("Erro na validação do Token: {}", exception.getMessage());
            return "";
        }
    }

    public User getUserDetailsFromGoogleToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            String email = decodedJWT.getClaim("email").asString();
            String name = decodedJWT.getClaim("name").asString();

            if (email != null && name != null) {
                User user = (User) userRepository.findByEmail(email);
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setRole(UserRole.USER);
                    userRepository.save(user);
                }
                return user;
            } else {
                log.error("O token não contém as informações necessárias (email, name).");
                return null;
            }
        } catch (JWTVerificationException exception) {
            log.error("Erro na validação do Token do Google: {}", exception.getMessage());
            return null;
        }
    }

    public User getUserDetailsFromToken(String token) {
        try {
            String email = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("login")
                    .build()
                    .verify(token)
                    .getSubject();
            return (User) userRepository.findByEmail(email);
        } catch (JWTVerificationException exception) {
            log.error("Erro na validação do Token: {}", exception.getMessage());
            return null;
        }
    }

    public String createPasswordResetToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("password-reset")
                    .withSubject(email)
                    .withExpiresAt(LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00")))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            log.error("Erro na geração do Token de redefinição de senha: {}", exception.getMessage());
            throw new RuntimeException("Erro na geração do Token de redefinição de senha.", exception);
        }
    }

    public String welcomeNewStoreResetPassword(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("welcome-store")
                    .withSubject(email)
                    .withExpiresAt(LocalDateTime.now().plusHours(3).toInstant(ZoneOffset.of("-03:00")))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            log.error("Erro na geração do Token de redefinição de senha: {}", exception.getMessage());
            throw new RuntimeException("Erro na geração do Token de redefinição de senha.", exception);
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.of("-03:00"));
    }
}
