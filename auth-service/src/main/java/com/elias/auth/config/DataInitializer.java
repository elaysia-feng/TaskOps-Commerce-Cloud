package com.elias.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.auth.entity.Role;
import com.elias.auth.entity.User;
import com.elias.auth.entity.UserAuthIdentity;
import com.elias.auth.entity.UserRole;
import com.elias.auth.mapper.RoleMapper;
import com.elias.auth.mapper.UserAuthIdentityMapper;
import com.elias.auth.mapper.UserMapper;
import com.elias.auth.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String USERNAME_IDENTITY_TYPE = "USERNAME";
    private static final String REGISTER_SOURCE_SYSTEM = "SYSTEM";

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserAuthIdentityMapper userAuthIdentityMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN", "ADMIN_ROLE");
        ensureRole("USER", "USER_ROLE");

        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
                        .last("limit 1")
        );

        if (admin == null) {
            admin = createAdmin();
            userMapper.insert(admin);
        }

        ensureAdminIdentity(admin);
        ensureAdminRoleBinding(admin, adminRole);
    }

    private Role ensureRole(String roleCode, String roleName) {
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, roleCode)
                        .last("limit 1")
        );

        if (role != null) {
            return role;
        }

        LocalDateTime now = LocalDateTime.now();
        role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleScope("ADMIN".equals(roleCode) ? "ADMIN" : "PLATFORM");
        role.setStatus(ACTIVE_STATUS);
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        roleMapper.insert(role);
        return role;
    }

    private User createAdmin() {
        LocalDateTime now = LocalDateTime.now();
        User admin = new User();
        admin.setUserNo("USR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        admin.setUsername("admin");
        admin.setNickname("super_admin");
        admin.setStatus(ACTIVE_STATUS);
        admin.setRegisterSource(REGISTER_SOURCE_SYSTEM);
        admin.setPasswordSet(1);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        return admin;
    }

    private void ensureAdminIdentity(User admin) {
        UserAuthIdentity identity = userAuthIdentityMapper.selectOne(
                new LambdaQueryWrapper<UserAuthIdentity>()
                        .eq(UserAuthIdentity::getIdentityType, USERNAME_IDENTITY_TYPE)
                        .eq(UserAuthIdentity::getPrincipal, admin.getUsername())
                        .last("limit 1")
        );

        if (identity != null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        identity = new UserAuthIdentity();
        identity.setUserId(admin.getId());
        identity.setIdentityType(USERNAME_IDENTITY_TYPE);
        identity.setPrincipal(admin.getUsername());
        identity.setCredentialHash(passwordEncoder.encode("Admin@123456"));
        identity.setCredentialVersion(1);
        identity.setStatus(ACTIVE_STATUS);
        identity.setCreatedAt(now);
        identity.setUpdatedAt(now);
        userAuthIdentityMapper.insert(identity);
    }

    private void ensureAdminRoleBinding(User admin, Role adminRole) {
        UserRole link = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, admin.getId())
                        .eq(UserRole::getRoleId, adminRole.getId())
                        .last("limit 1")
        );

        if (link != null) {
            return;
        }

        link = new UserRole();
        link.setUserId(admin.getId());
        link.setRoleId(adminRole.getId());
        link.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(link);
    }
}