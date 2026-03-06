package com.futurescope.platform.notification.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MockEmailSender.class);

    @Override
    public boolean send(String toEmail, String subject, String body) {
        log.info("MockEmailSender: would send to {} subject '{}'", toEmail, subject);
        return true;
    }
}
