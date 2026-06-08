package com.example.rentlyauth.service;

import com.example.rentlyauth.service.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender; // Narazie nie ustawiamy mailSendera
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String from;
    @Async
    public void AccountVerificationMessage(String name, String to, String token) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("New User Account Verification");
            message.setTo(to);
           // message.setTo(from);//tutaj sobie możecie swój mail wstawić do testów by nie wysyłać potwierdzenia na nowy za każdym razem
            message.setFrom(from);
            message.setText(EmailUtils.getEmail(name,host,token));
            emailSender.send(message);
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    @Async
    public void PasswordResetMessage(String to, String token) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("Password Reset");
            message.setTo(to);
            //message.setTo(from);
            message.setFrom(from);
            message.setText(EmailUtils.getPassword(host,token));
            emailSender.send(message);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
