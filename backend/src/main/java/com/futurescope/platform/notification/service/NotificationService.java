package com.futurescope.platform.notification.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.notification.client.EmailSender;
import com.futurescope.platform.notification.domain.Notification;
import com.futurescope.platform.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailSender emailSender;

    public NotificationService(NotificationRepository notificationRepository, EmailSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.emailSender = emailSender;
    }

    @Transactional
    public void sendInterviewInviteEmail(Company company, User user, String inviteLink) {
        if (user == null) return;
        String payload = "{\"inviteLink\":\"" + inviteLink + "\"}";
        Notification n = new Notification();
        n.setId(UUID.randomUUID());
        n.setCompany(company);
        n.setUser(user);
        n.setChannel("email");
        n.setType("interview_invite");
        n.setTemplateKey("interview_invite");
        n.setPayloadJson(payload);
        n.setStatus("pending");
        n.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(n);
        boolean sent = emailSender.send(user.getEmail(), "Interview invitation", "Your interview link: " + inviteLink);
        n.setStatus(sent ? "sent" : "failed");
        n.setSentAt(OffsetDateTime.now());
        if (!sent) n.setErrorMessage("Mock sender returned false");
        notificationRepository.save(n);
    }
}
