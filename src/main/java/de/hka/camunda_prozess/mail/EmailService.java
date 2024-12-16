package de.hka.camunda_prozess.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML-Unterstützung

            mailSender.send(mimeMessage);

            System.out.println("E-Mail erfolgreich gesendet!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fehler beim Senden der E-Mail: " + e.getMessage());
        }
    }
}
