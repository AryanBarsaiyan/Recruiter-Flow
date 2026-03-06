package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.Role;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.domain.UserSession;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.EmailVerificationRepository;
import com.futurescope.platform.auth.repository.PasswordResetRepository;
import com.futurescope.platform.auth.repository.RoleRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.auth.repository.UserSessionRepository;
import com.futurescope.platform.auth.web.dto.InviteRequest;
import com.futurescope.platform.auth.web.dto.InviteResponse;
import com.futurescope.platform.auth.web.dto.LoginRequest;
import com.futurescope.platform.auth.web.dto.RefreshRequest;
import com.futurescope.platform.auth.web.dto.SignupSuperAdminRequest;
import com.futurescope.platform.audit.service.AuditService;
import com.futurescope.platform.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final String ROLE_SUPER_ADMIN = "SuperAdmin";
    private static final String ROLE_SCOPE_COMPANY = "company";

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final RbacService rbacService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            CompanyMemberRepository companyMemberRepository,
            UserSessionRepository userSessionRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordResetRepository passwordResetRepository,
            RbacService rbacService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userSessionRepository = userSessionRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.rbacService = rbacService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthTokens signupSuperAdmin(SignupSuperAdminRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName(request.getCompanyName());
        company.setSlug(generateSlug(request.getCompanyName()));
        company.setActive(true);
        company.setCreatedAt(OffsetDateTime.now());
        companyRepository.save(company);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType("recruiter");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        Role superAdminRole = roleRepository
                .findByNameAndScope(ROLE_SUPER_ADMIN, ROLE_SCOPE_COMPANY)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setId(UUID.randomUUID());
                    role.setName(ROLE_SUPER_ADMIN);
                    role.setScope(ROLE_SCOPE_COMPANY);
                    return roleRepository.save(role);
                });

        CompanyMember member = new CompanyMember();
        member.setId(UUID.randomUUID());
        member.setCompany(company);
        member.setUser(user);
        member.setRole(superAdminRole);
        member.setStatus("active");
        member.setCreatedAt(OffsetDateTime.now());
        companyMemberRepository.save(member);

        auditService.log(company, user, ROLE_SUPER_ADMIN, "company_created", "company", company.getId(), "{\"name\":\"" + company.getName() + "\"}");
        return createSessionAndTokens(user);
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(authenticationToken);

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        return createSessionAndTokens(user);
    }

    @Transactional
    public AuthTokens refresh(RefreshRequest request) {
        UserSession session = userSessionRepository.findBySessionToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (!session.isActive() || session.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = session.getUser();
        String accessToken = jwtService.generateAccessToken(user, session.getId());
        return new AuthTokens(accessToken, session.getSessionToken());
    }

    @Transactional
    public void logout(User currentUser) {
        userSessionRepository.deactivateAllActiveSessions(currentUser);
    }

    @Transactional
    public InviteResponse invite(InviteRequest request, User invitedByUser) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        CompanyMember inviterMember = rbacService.requireActiveCompanyMember(invitedByUser, company.getId());
        rbacService.requireAnyRole(inviterMember, java.util.Set.of("SuperAdmin", "Admin"));

        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseGet(() -> {
                    User u = new User();
                    u.setId(UUID.randomUUID());
                    u.setEmail(request.getEmail().trim());
                    u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                    u.setUserType("recruiter");
                    u.setActive(false);
                    u.setCreatedAt(OffsetDateTime.now());
                    return userRepository.save(u);
                });

        if (companyMemberRepository.findByCompanyAndUser(company, user).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this company");
        }

        Role role = roleRepository.findByNameAndScope(request.getRoleName(), ROLE_SCOPE_COMPANY)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId(UUID.randomUUID());
                    r.setName(request.getRoleName());
                    r.setScope(ROLE_SCOPE_COMPANY);
                    return roleRepository.save(r);
                });

        CompanyMember member = new CompanyMember();
        member.setId(UUID.randomUUID());
        member.setCompany(company);
        member.setUser(user);
        member.setRole(role);
        member.setInvitedBy(invitedByUser);
        member.setStatus("invited");
        member.setCreatedAt(OffsetDateTime.now());
        companyMemberRepository.save(member);

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(7, ChronoUnit.DAYS);
        var ev = new com.futurescope.platform.auth.domain.EmailVerification();
        ev.setId(UUID.randomUUID());
        ev.setUser(user);
        ev.setToken(token);
        ev.setExpiresAt(expiresAt);
        ev.setCreatedAt(OffsetDateTime.now());
        emailVerificationRepository.save(ev);

        return new InviteResponse(token, expiresAt);
    }

    @Transactional
    public AuthTokens acceptInvite(String token, String password) {
        var ev = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invite token"));
        if (ev.getUsedAt() != null) throw new IllegalArgumentException("Invite already used");
        if (ev.getExpiresAt().isBefore(OffsetDateTime.now())) throw new IllegalArgumentException("Invite token expired");

        User user = ev.getUser();
        var invitedMemberships = companyMemberRepository.findByUserAndStatusOrderByCreatedAtDesc(user, "invited");
        if (invitedMemberships.isEmpty()) throw new IllegalArgumentException("No pending invite found");
        CompanyMember member = invitedMemberships.get(0);
        member.setStatus("active");
        companyMemberRepository.save(member);
        user.setActive(true);
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        userRepository.save(user);
        ev.setUsedAt(OffsetDateTime.now());
        emailVerificationRepository.save(ev);

        return createSessionAndTokens(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        var ev = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        if (ev.getUsedAt() != null) return;
        if (ev.getExpiresAt().isBefore(OffsetDateTime.now())) throw new IllegalArgumentException("Token expired");
        ev.setUsedAt(OffsetDateTime.now());
        emailVerificationRepository.save(ev);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            var pr = new com.futurescope.platform.auth.domain.PasswordReset();
            pr.setId(UUID.randomUUID());
            pr.setUser(user);
            pr.setToken(token);
            pr.setExpiresAt(OffsetDateTime.now().plus(1, ChronoUnit.HOURS));
            pr.setCreatedAt(OffsetDateTime.now());
            passwordResetRepository.save(pr);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        var pr = passwordResetRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
        if (pr.getUsedAt() != null) throw new IllegalArgumentException("Token already used");
        if (pr.getExpiresAt().isBefore(OffsetDateTime.now())) throw new IllegalArgumentException("Token expired");
        User user = pr.getUser();
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
        pr.setUsedAt(OffsetDateTime.now());
        passwordResetRepository.save(pr);
    }

    private String generateSlug(String companyName) {
        String base = companyName.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        String candidate = base;
        int counter = 1;
        while (companyRepository.existsBySlug(candidate)) {
            candidate = base + "-" + counter;
            counter++;
        }
        return candidate;
    }

    private AuthTokens createSessionAndTokens(User user) {
        userSessionRepository.deactivateAllActiveSessions(user);

        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setSessionToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        session.setActive(true);
        session.setCreatedAt(OffsetDateTime.now());
        session.setExpiresAt(OffsetDateTime.now().plus(30, ChronoUnit.DAYS));
        userSessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(user, session.getId());
        return new AuthTokens(accessToken, session.getSessionToken());
    }

    public record AuthTokens(String accessToken, String refreshToken) {
    }
}

