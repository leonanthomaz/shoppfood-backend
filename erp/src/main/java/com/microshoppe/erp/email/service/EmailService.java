package com.microshoppe.erp.email.service;

import com.microshoppe.erp.email.enums.StatusEmail;
import com.microshoppe.erp.email.model.Email;
import com.microshoppe.erp.email.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log4j2
@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    EmailRepository repository;

    public EmailService() {
    }

    @Transactional
    public void sendEmail(Email emailModel) {
        log.info("E-MAIL VINDO DA REQUISIÇÃO: {}", emailModel);

        emailModel.setStatusEmail(StatusEmail.PROCESSING);
        emailModel.setSendDateEmail(LocalDateTime.now());

        log.info("E-MAIL ATUALIZADO: {}", emailModel);

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailModel.getEmailFrom());
            helper.setTo(emailModel.getEmailTo());
            helper.setSubject(emailModel.getSubject());

            String htmlMsg = formatHtmlEmail(emailModel.getSubject(), emailModel.getText());
            helper.setText(htmlMsg, true);

            log.info("E-MAIL PROCESSADO PARA ENVIO: {}", message);

            emailSender.send(message);
            emailModel.setStatusEmail(StatusEmail.SENT);

        } catch (MailException | MessagingException e) {
            log.error("FALHA AO ENVIAR EMAIL: " + e);
            emailModel.setStatusEmail(StatusEmail.ERROR);
        } finally {
            log.info("SALVANDO E-MAIL E FECHANDO REQUISIÇÃO: " + emailModel);
            repository.save(emailModel);
        }
    }

    private String formatHtmlEmail(String subject, String bodyText) {
        return "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <style>"
                + "        /* Estilos globais */"
                + "        body {"
                + "            font-family: 'Poppins', sans-serif;"
                + "            background-color: #f7f7f7;"
                + "            margin: 0;"
                + "            padding: 0;"
                + "            color: #333333;"
                + "        }"
                + "        .container {"
                + "            width: 100%;"
                + "            max-width: 600px;"
                + "            margin: 0 auto;"
                + "            background-color: #ffffff;"
                + "            border-radius: 8px;"
                + "            overflow: hidden;"
                + "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);"
                + "        }"
                + "        /* Cabeçalho */"
                + "        .email-header {"
                + "            background-color: #1a408b;"
                + "            padding: 20px;"
                + "            text-align: center;"
                + "            color: #ffffff;"
                + "        }"
                + "        .email-header img {"
                + "            margin-bottom: 10px;"
                + "        }"
                + "        .email-header h1 {"
                + "            margin: 0;"
                + "            font-size: 24px;"
                + "            font-weight: 600;"
                + "        }"
                + "        /* Corpo do email */"
                + "        .email-body {"
                + "            padding: 30px 20px;"
                + "            line-height: 1.8;"
                + "            color: #555555;"
                + "        }"
                + "        .email-body p {"
                + "            margin: 0 0 15px 0;"
                + "            font-size: 16px;"
                + "        }"
                + "        .email-body a {"
                + "            color: #1a408b;"
                + "            text-decoration: none;"
                + "            font-weight: bold;"
                + "        }"
                + "        .email-body a:hover {"
                + "            text-decoration: underline;"
                + "        }"
                + "        /* Botão de ação */"
                + "        .email-button {"
                + "            display: inline-block;"
                + "            padding: 12px 20px;"
                + "            margin: 20px 0;"
                + "            background-color: #1a408b;"
                + "            color: #ffffff;"
                + "            text-decoration: none;"
                + "            font-size: 16px;"
                + "            font-weight: bold;"
                + "            border-radius: 6px;"
                + "        }"
                + "        .email-button:hover {"
                + "            background-color: #163872;"
                + "        }"
                + "        /* Rodapé */"
                + "        .email-footer {"
                + "            background-color: #f0f0f0;"
                + "            padding: 20px;"
                + "            text-align: center;"
                + "            font-size: 12px;"
                + "            color: #888888;"
                + "        }"
                + "        .email-footer p {"
                + "            margin: 5px 0;"
                + "        }"
                + "        .email-footer img {"
                + "            max-width: 100px;"
                + "            margin-top: 10px;"
                + "        }"
                + "        /* Responsividade */"
                + "        @media (max-width: 600px) {"
                + "            .email-body {"
                + "                padding: 20px 10px;"
                + "            }"
                + "        }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <div class='container'>"
                + "        <!-- Cabeçalho do email -->"
                + "        <div class='email-header'>"
                + "            <img src='https://i.postimg.cc/XJtCVx2W/logo-sf-white.png' alt='Logo ShoppeFood' width='150'>"
                + "            <h1>" + subject + "</h1>"
                + "        </div>"
                + "        <!-- Corpo do email -->"
                + "        <div class='email-body'>"
                + "            <p>" + bodyText + "</p>"
                + "            <p>Se precisar de ajuda ou tiver alguma dúvida, não hesite em entrar em contato conosco.</p>"
                + "            <p>Obrigado por escolher a ShoppeFood!</p>"
                + "        </div>"
                + "        <!-- Rodapé do email -->"
                + "        <div class='email-footer'>"
                + "            <p>&copy; 2024 ShoppeFood. Todos os direitos reservados.</p>"
                + "            <p>Se você não deseja mais receber e-mails, <a href='#'>clique aqui para cancelar a inscrição</a>.</p>"
                + "            <img src='https://i.postimg.cc/XJtCVx2W/logo-sf-white.png' alt='Logo ShoppeFood Footer'>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }
}
