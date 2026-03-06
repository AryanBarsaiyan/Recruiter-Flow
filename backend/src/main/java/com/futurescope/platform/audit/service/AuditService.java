package com.futurescope.platform.audit.service;

import com.futurescope.platform.audit.domain.AuditLog;
import com.futurescope.platform.audit.repository.AuditLogRepository;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(Company company, User actorUser, String actorRole, String action, String entityType, UUID entityId, String metadataJson) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID());
        log.setCompany(company);
        log.setActorUser(actorUser);
        log.setActorRole(actorRole);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadataJson(metadataJson);
        log.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(String action, String entityType, UUID entityId, String metadataJson) {
        log(null, null, null, action, entityType, entityId, metadataJson);
    }
}
