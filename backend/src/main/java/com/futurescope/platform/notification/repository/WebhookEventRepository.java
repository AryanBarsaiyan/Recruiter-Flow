package com.futurescope.platform.notification.repository;

import com.futurescope.platform.notification.domain.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
}
