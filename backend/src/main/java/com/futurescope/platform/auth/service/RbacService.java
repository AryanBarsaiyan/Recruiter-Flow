package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class RbacService {

    private final CompanyMemberRepository companyMemberRepository;

    public RbacService(CompanyMemberRepository companyMemberRepository) {
        this.companyMemberRepository = companyMemberRepository;
    }

    public CompanyMember requireActiveCompanyMember(User user, UUID companyId) {
        return companyMemberRepository.findActiveMembership(companyId, user.getId(), "active")
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this company"));
    }

    public void requireAnyRole(CompanyMember member, Set<String> allowedRoles) {
        String roleName = member.getRole().getName();
        if (!allowedRoles.contains(roleName)) {
            throw new IllegalArgumentException("Insufficient permissions");
        }
    }
}

