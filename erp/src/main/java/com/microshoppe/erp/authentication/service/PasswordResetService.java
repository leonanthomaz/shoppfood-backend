package com.microshoppe.erp.authentication.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.authentication.repository.UserRepository;
import com.microshoppe.erp.email.model.Email;
import com.microshoppe.erp.email.service.EmailService;
import com.microshoppe.erp.store.model.Store;
import com.microshoppe.erp.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    @Value("${jwt.secret}")
    private String secret;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final StoreService storeService;

    public void processingPasswordSetupEmail(String email) {
        log.info("Gerando token de redefinição de senha para recuperação de conta.");
        String subject = "Redefinição de Senha";
        String messageBody = "Clique no link para redefinir sua senha: ";
        sendPasswordResetEmail(email, subject, messageBody);
    }

    public void sendPasswordResetEmail(String email, String subject, String messageBody) {
        log.info("Gerando token de redefinição de senha para o e-mail - USUÁRIO EXISTENTE: {}", email);
        String token = tokenService.createPasswordResetToken(email);
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        User user = (User) userRepository.findByEmail(email);
        if (user != null) {
            user.setResetToken(token);
            user.setResetTokenExpiryDate(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
        }
        String completeMessage = formatResetEmail(messageBody, resetLink);
        Email emailModel = new Email();
        emailModel.setEmailFrom("leonan.thomaz@gmail.com");
        emailModel.setEmailTo(email);
        emailModel.setSubject(subject);
        emailModel.setText(completeMessage);

        emailService.sendEmail(emailModel);
    }

    private String formatResetEmail(String bodyText, String resetLink) {
        return "<p>" + bodyText + "</p>" +
                "<p><a href='" + resetLink + "'>Clique aqui para redefinir sua senha</a></p>";
    }

    public boolean resetPassword(String token, String newPassword) {
        log.info("PARAMETROS RECEBIDOS NO RESET-PASSWORD (SERVICE) - TOKEN: {} - SENHA NOVA: {}", token, newPassword);
        try {
            String email = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("password-reset")
                    .build()
                    .verify(token)
                    .getSubject();

            log.info("EXTRAÇÃO DO EMAIL: {}", email);

            User user = (User) userRepository.findByEmail(email);
            log.info("USUÁRIO ENCONTRADO: {}", user);

            if (user != null) {
                var encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
                user.setPassword(encryptedPassword);
                user.setResetToken(null);
                user.setResetTokenExpiryDate(null);
                userRepository.save(user);
                return true;
            }

            return false;

        } catch (JWTVerificationException exception) {
            log.error("Erro na validação do Token de redefinição de senha: {}", exception.getMessage());
            return false;
        }
    }

    public boolean resetPasswordNewStore(String token, String newPassword) {
        log.info("PARAMETROS RECEBIDOS NO RESET-PASSWORD (SERVICE) - EXTRAÇÃO DO TOKEN: {} - SENHA NOVA: {}", token, newPassword);
        try {
            // Decodifica o token sem verificar para obter informações
            DecodedJWT decodedJWT = JWT.decode(token);
            log.info("Issuer do token recebido: {}", decodedJWT.getIssuer());

            // Verificação do token com o issuer esperado
            String email = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("welcome-store")
                    .build()
                    .verify(token)
                    .getSubject();

            log.info("EXTRAÇÃO DO EMAIL PS: {}", email);

            User user = (User) userRepository.findByEmail(email);
            log.info("USUÁRIO ENCONTRADO PS: {}", user);

            if (user != null) {
                var encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
                user.setPassword(encryptedPassword);
                user.setResetToken(null);
                user.setResetTokenExpiryDate(null);
                userRepository.save(user);

                if (user.getAccessLevel().equals("ADMIN")) {
                    Store store = storeService.findByMerchantCode(user.getMerchantCode());
                    if (store != null && store.isNewStore()) {
                        store.setNewStore(false);
                        storeService.saveStore(store);
                        log.info("LOJA ATUALIZADA: Campo isNewStore setado para false.");
                    }
                }

                return true;
            }
            return false;
        } catch (JWTVerificationException exception) {
            log.error("Erro na validação do Token de redefinição de senha: {}", exception.getMessage());
            return false;
        }
    }

}
