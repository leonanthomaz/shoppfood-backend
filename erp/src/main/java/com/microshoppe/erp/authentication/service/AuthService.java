package com.microshoppe.erp.authentication.service;

import com.microshoppe.erp.authentication.dto.LoginDTO;
import com.microshoppe.erp.authentication.dto.RegisterDTO;
import com.microshoppe.erp.authentication.dto.TokenDTO;
import com.microshoppe.erp.authentication.enums.AccessLevel;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.authentication.exception.AuthException;
import com.microshoppe.erp.authentication.model.User;
import com.microshoppe.erp.email.model.Email;
import com.microshoppe.erp.email.service.EmailService;
import com.microshoppe.erp.store.dto.StoreRegisterUserDTO;
import com.microshoppe.erp.store.model.Store;
import com.microshoppe.erp.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final EmailService emailService;

    private String generateRandomMerchantCode() {
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        return uuid.substring(0, Math.min(uuid.length(), 32)).toUpperCase();
    }

    public TokenDTO login(LoginDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        if (auth.getPrincipal() instanceof User user) {
            var token = tokenService.createToken(user);
            return new TokenDTO(token);
        } else {
            throw new AuthException("Falha na autenticação: usuário inválido.");
        }
    }

    public TokenDTO register(RegisterDTO data) {
        if (userService.findByEmail(data.getEmail()) != null) {
            throw new AuthException("EMAIL JÁ CADASTRADO: " + data.getEmail());
        }
        var encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

        User newUser = User.builder()
                .merchantCode(data.getMerchantCode())
                .name(data.getName())
                .email(data.getEmail())
                .password(encryptedPassword)
                .accessLevel(AccessLevel.USER)
                .role(UserRole.USER)
                .build();
        userService.save(newUser);

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        if (auth.getPrincipal() instanceof User user) {
            var token = tokenService.createToken(user);
            return new TokenDTO(token);
        } else {
            throw new AuthException("Falha na autenticação: usuário inválido.");
        }
    }

    public TokenDTO googleAuthLogin(Map<String, String> payload) {
        String token = payload.get("token");

        if (token == null) {
            throw new AuthException("TOKEN NÃO INFORMADO");
        }

        User user = tokenService.getUserDetailsFromGoogleToken(token);

        if (user == null) {
            throw new AuthException("NÃO FOI POSSÍVEL RECUPERAR DETALHES DO USUÁRIO");
        }

        var existingUser = userService.findByEmail(user.getEmail());

        if (existingUser == null) {
            User newUser = User.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .password(null)
                    .role(UserRole.USER)
                    .build();
            var savedUser = userService.save(newUser);
            var appToken = tokenService.createToken(savedUser);
            return new TokenDTO(appToken);
        } else {
            var appToken = tokenService.createToken((User) existingUser);
            return new TokenDTO(appToken);
        }
    }

    public User registerStoreUser(StoreRegisterUserDTO data) {
        if (userService.findByEmail(data.getEmail()) != null) {
            throw new AuthException("EMAIL JÁ CADASTRADO: " + data.getEmail());
        }
        String password = "shoppefood123";

        var encryptedPassword = new BCryptPasswordEncoder().encode(password);

        User newUser = User.builder()
                .merchantCode(generateRandomMerchantCode())
                .name(data.getName())
                .email(data.getEmail())
                .password(encryptedPassword)
                .accessLevel(AccessLevel.ADMIN)
                .role(UserRole.ADMIN)
                .build();

        User savedUser = userService.save(newUser);
        processingEmailWelcomeNewStore(savedUser.getEmail(), savedUser.getName());
        return savedUser;
    }

    public void processingEmailWelcomeNewStore(String email, String name) {
        log.info("Enviando e-mail de boas-vindas para: {}", email);
        String subject = "Bem-vindo, novo comerciante!";

        String messageBody =
                "<p>Olá " + name + ",</p>" +
                "<p>Estamos muito contentes com sua presença no nosso sistema!</p>" +
                "<p>Para começar a aproveitar todos os recursos disponíveis, clique no botão abaixo para acessar sua loja e personalizá-la conforme seu gosto.</p>" +
                "<p style='text-align: center;'>"
                + "<a href='http://localhost:3000' style='display: inline-block; margin: 10px; padding: 10px 20px; background-color: #c50000; color: #ffffff; border-radius: 5px; text-decoration: none;'>Acessar minha Loja</a>"
                + "<a href='http://localhost:5173' style='display: inline-block; margin: 10px; padding: 10px 20px; background-color: #1a408b; color: #ffffff; border-radius: 5px; text-decoration: none;'>Acessar o ERP</a>"
                + "</p>"
                + "<p>Para acessar o ERP, utilize a seguinte senha padrão: <strong>shoppefood123</strong></p>"
                + "<p>Recomendamos que você altere sua senha assim que fizer login.</p>";

        sendEmailWelcomeNewStore(email, name, subject, messageBody);
    }

    private void sendEmailWelcomeNewStore(String email, String name, String subject, String messageBody) {
        log.info("Gerando token de redefinição de senha para o e-mail - NOVA LOJA: {}", email);

        String messageModel = formatHTMLNewStore(messageBody, name);

        Email emailModel = new Email();
        emailModel.setEmailFrom("leonan.thomaz@gmail.com");
        emailModel.setEmailTo(email);
        emailModel.setSubject(subject);
        emailModel.setText(messageModel);

        emailService.sendEmail(emailModel);
    }

    private String formatHTMLNewStore(String bodyText, String name) {
        return "<p>" + bodyText + "</p>";
    }

}
