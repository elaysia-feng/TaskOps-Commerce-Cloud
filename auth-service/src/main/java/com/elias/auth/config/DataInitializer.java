package com.elias.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.auth.entity.Role;
import com.elias.auth.entity.User;
import com.elias.auth.entity.UserRole;
import com.elias.auth.mapper.RoleMapper;
import com.elias.auth.mapper.UserMapper;
import com.elias.auth.mapper.UserRoleMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleMapper roleMapper, UserMapper userMapper, UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN", "ADMIN_ROLE");
        ensureRole("USER", "USER_ROLE");

        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "admin")
                .last("limit 1"));
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setNickname("super_admin");
            admin.setPassword(passwordEncoder.encode("Admin@123456"));
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(admin);
        }

        UserRole link = userRoleMapper.selectOne(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, admin.getId())
                .eq(UserRole::getRoleId, adminRole.getId())
                .last("limit 1"));
        if (link == null) {
            link = new UserRole();
            link.setUserId(admin.getId());
            link.setRoleId(adminRole.getId());
            userRoleMapper.insert(link);
        }
    }

    private Role ensureRole(String roleCode, String roleName) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)
                .last("limit 1"));
        if (role == null) {
            role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            roleMapper.insert(role);
        }
        return role;
    }
}
