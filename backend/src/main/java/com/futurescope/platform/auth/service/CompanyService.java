package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.domain.Role;
import com.futurescope.platform.auth.repository.RoleRepository;
import com.futurescope.platform.auth.web.dto.CompanyMemberResponse;
import com.futurescope.platform.auth.web.dto.CompanyResponse;
import com.futurescope.platform.auth.web.dto.UpdateBrandingRequest;
import com.futurescope.platform.auth.web.dto.UpdateMemberRoleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CompanyService {

    private static final String ROLE_SCOPE_COMPANY = "company";

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final RoleRepository roleRepository;
    private final UserAvatarService userAvatarService;
    private final RbacService rbacService;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            RoleRepository roleRepository,
            UserAvatarService userAvatarService,
            RbacService rbacService
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.roleRepository = roleRepository;
        this.userAvatarService = userAvatarService;
        this.rbacService = rbacService;
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(UUID companyId, User currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public CompanyMemberResponse getCurrentMember(UUID companyId, User currentUser) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        CompanyMember member = companyMemberRepository.findActiveMembership(companyId, currentUser.getId(), "active")
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this company"));
        return toMemberResponse(member, companyId);
    }

    @Transactional(readOnly = true)
    public List<CompanyMemberResponse> listMembers(UUID companyId, User currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        return companyMemberRepository.findByCompanyOrderByCreatedAtDesc(company).stream()
                .map(m -> toMemberResponse(m, company.getId()))
                .toList();
    }

    @Transactional
    public CompanyMemberResponse updateMemberRole(UUID companyId, UUID memberId, UpdateMemberRoleRequest request, User currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        CompanyMember currentMember = rbacService.requireActiveCompanyMember(currentUser, company.getId());
        rbacService.requireAnyRole(currentMember, Set.of("SuperAdmin", "Admin"));

        CompanyMember targetMember = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (!targetMember.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Member not in this company");
        }

        Role newRole = roleRepository.findByNameAndScope(request.getRoleName().trim(), ROLE_SCOPE_COMPANY)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId(UUID.randomUUID());
                    r.setName(request.getRoleName().trim());
                    r.setScope(ROLE_SCOPE_COMPANY);
                    return roleRepository.save(r);
                });
        if (!Set.of("SuperAdmin", "Admin", "ReadOnly", "View").contains(newRole.getName())) {
            throw new IllegalArgumentException("Invalid role for company member");
        }

        targetMember.setRole(newRole);
        companyMemberRepository.save(targetMember);
        return toMemberResponse(targetMember, companyId);
    }

    @Transactional(readOnly = true)
    public Path getMemberAvatarPath(UUID companyId, UUID memberUserId, User currentUser) {
        rbacService.requireActiveCompanyMember(currentUser, companyId);
        CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, memberUserId)
                .orElse(null);
        if (member == null) return null;
        User user = member.getUser();
        if (user.getAvatarStoragePath() == null || user.getAvatarStoragePath().isBlank()) {
            return null;
        }
        return userAvatarService.resolveAvatarPath(user.getAvatarStoragePath());
    }

    @Transactional
    public CompanyResponse updateBranding(UUID companyId, User currentUser, UpdateBrandingRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, company.getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));
        if (request.getBrandingConfigJson() != null) {
            company.setBrandingConfigJson(request.getBrandingConfigJson());
            companyRepository.save(company);
        }
        return toResponse(company);
    }

    private CompanyResponse toResponse(Company c) {
        CompanyResponse r = new CompanyResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setSlug(c.getSlug());
        r.setBrandingConfigJson(c.getBrandingConfigJson());
        r.setActive(c.isActive());
        return r;
    }

    private CompanyMemberResponse toMemberResponse(CompanyMember m, UUID companyId) {
        CompanyMemberResponse r = new CompanyMemberResponse();
        r.setId(m.getId());
        r.setUserId(m.getUser().getId());
        r.setUserEmail(m.getUser().getEmail());
        r.setFullName(m.getUser().getFullName());
        r.setRoleName(m.getRole().getName());
        r.setStatus(m.getStatus());
        if (m.getUser().getAvatarStoragePath() != null && !m.getUser().getAvatarStoragePath().isBlank()) {
            r.setAvatarUrl("/companies/" + companyId + "/members/" + m.getUser().getId() + "/avatar");
        }
        return r;
    }
}
