package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.web.dto.CompanyMemberResponse;
import com.futurescope.platform.auth.web.dto.CompanyResponse;
import com.futurescope.platform.auth.web.dto.UpdateBrandingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final RbacService rbacService;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMemberRepository companyMemberRepository,
            RbacService rbacService
    ) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
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
    public List<CompanyMemberResponse> listMembers(UUID companyId, User currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        return companyMemberRepository.findByCompanyOrderByCreatedAtDesc(company).stream()
                .map(this::toMemberResponse)
                .toList();
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

    private CompanyMemberResponse toMemberResponse(CompanyMember m) {
        CompanyMemberResponse r = new CompanyMemberResponse();
        r.setId(m.getId());
        r.setUserId(m.getUser().getId());
        r.setUserEmail(m.getUser().getEmail());
        r.setRoleName(m.getRole().getName());
        r.setStatus(m.getStatus());
        return r;
    }
}
