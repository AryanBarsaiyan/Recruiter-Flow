package com.futurescope.platform.notification.client;

/**
 * Abstraction for sending email. Implement with real SMTP/SES later.
 */
public interface EmailSender {

    /**
     * Send an email. Returns true if accepted for delivery.
     */
    boolean send(String toEmail, String subject, String body);
}
