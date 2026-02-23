package com.elias.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.auth.entity.Role;
import com.elias.auth.entity.User;
import com.elias.auth.entity.UserRole;
import com.elias.auth.mapper.RoleMapper;
import com.elias.auth.mapper.UserMapper;
import com.elias.auth.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化组件
 * 在 Spring Boot 启动完成后自动执行
 *
 * 作用：
 * 1. 确保存在 ADMIN 和 USER 两个角色
 * 2. 确保存在默认 admin 用户
 * 3. 确保 admin 用户绑定 ADMIN 角色
 */
@Component
@RequiredArgsConstructor
/**
 * 文件说明： DataInitializer.
 * 组件职责： 项目中的通用组件。
 */
public class DataInitializer implements CommandLineRunner {

    // 角色表操作
    private final RoleMapper roleMapper;

    // 用户表操作
    private final UserMapper userMapper;

    // 用户角色关联表操作
    private final UserRoleMapper userRoleMapper;

    // 密码加密器
    private final PasswordEncoder passwordEncoder;

    /**
     * 应用启动完成后执行
     */
    @Override
    public void run(String... args) {

        // 确保 ADMIN 角色存在
        Role adminRole = ensureRole("ADMIN", "ADMIN_ROLE");

        // 确保 USER 角色存在
        ensureRole("USER", "USER_ROLE");

        // 查询 admin 用户是否存在
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
                        .last("limit 1")
        );

        // 如果不存在则创建
        if (admin == null) {
            admin = new User();
            createAdmin(admin);
            userMapper.insert(admin);
        }

        // 查询 admin 是否已经绑定 ADMIN 角色
        UserRole link = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, admin.getId())
                        .eq(UserRole::getRoleId, adminRole.getId())
                        .last("limit 1")
        );

        // 如果没有绑定则建立关联
        if (link == null) {
            link = new UserRole();
            link.setUserId(admin.getId());
            link.setRoleId(adminRole.getId());
            userRoleMapper.insert(link);
        }
    }

    /**
     * 确保角色存在
     * 如果数据库中不存在该角色则创建
     */
    private Role ensureRole(String roleCode, String roleName) {

        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, roleCode)
                        .last("limit 1")
        );

        if (role == null) {
            role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            roleMapper.insert(role);
        }

        return role;
    }

    private User createAdmin(User admin) {
        admin.setUsername("admin");
        admin.setNickname("super_admin");
        admin.setPassword(passwordEncoder.encode("Admin@123456"));
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }
}