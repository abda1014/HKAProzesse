package de.hka.camunda_prozess.mail;

import de.hka.camunda_prozess.mail.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderRunner implements CommandLineRunner {

    private final EmailService emailService;

    public EmailSenderRunner(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) throws Exception {
        emailService.sendEmail(
            "caleb_g@outlook.de",
            "Testnachricht",
            "<h1>Hallo!</h1><p>Das ist eine Testnachricht.</p>"
        );
    }
}
