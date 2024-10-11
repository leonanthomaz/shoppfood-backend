package com.microshoppe.erp.email.controller;

import com.microshoppe.erp.email.dto.EmailDTO;
import com.microshoppe.erp.email.model.Email;
import com.microshoppe.erp.email.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class EmailController {

    @Autowired
    EmailService service;

    @PostMapping("/sending-email")
    public ResponseEntity<Email> sendingEmail(@RequestBody @Valid EmailDTO emailDto) {
        Email emailModel = new Email();
        BeanUtils.copyProperties(emailDto, emailModel);
        service.sendEmail(emailModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(emailModel);
    }
}
