package de.hka.camunda_prozess.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailConfigChecker {

    @Value("${SPRING_MAIL_USERNAME}")
    private String username;

    @Value("${SPRING_MAIL_PASSWORD}")
    private String password;

    public void printEmailConfig() {
        System.out.println("Username: " + username);
        System.out.println("Password: " + (password != null ? "****" : "not set"));
    }
}
