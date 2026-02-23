package com.elias.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.dto.UserInfoDTO;
import com.elias.auth.entity.LoginLog;
import com.elias.auth.entity.Role;
import com.elias.auth.entity.User;
import com.elias.auth.entity.UserRole;
import com.elias.auth.mapper.LoginLogMapper;
import com.elias.auth.mapper.RoleMapper;
import com.elias.auth.mapper.UserMapper;
import com.elias.auth.mapper.UserRoleMapper;
import com.elias.auth.security.JwtTokenProvider;
import com.elias.auth.service.AuthAppService;
import com.elias.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * 文件说明：认证服务业务实现。
 * 组件职责：
 * 1) 用户注册与登录。
 * 2) 登录失败计数与临时锁定（Redis 风控）。
 * 3) 签发 JWT 并记录登录日志。
 * 4) 提供内部用户信息查询能力。
 */
public class AuthAppServiceImpl implements AuthAppService {

    private static final String FAIL_KEY_PREFIX = "auth:fail:";
    private static final String LOCK_KEY_PREFIX = "auth:lock:";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;


    @Override
    /**
     * 注册新用户：
     * 1) 校验用户名是否存在；
     * 2) 写入用户信息；
     * 3) 绑定默认 USER 角色。
     */
    public void register(RegisterRequest request) {
        User existed = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (existed != null) {
            throw new BizException(4091, "username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Role userRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, "USER")
                .last("limit 1"));
        if (userRole != null) {
            UserRole link = new UserRole();
            link.setUserId(user.getId());
            link.setRoleId(userRole.getId());
            userRoleMapper.insert(link);
        }
    }

    @Override
    /**
     * 登录流程：
     * 1) 先检查是否处于锁定状态；
     * 2) 校验用户名密码；
     * 3) 成功则签发 JWT；
     * 4) 失败则累计失败次数并可能锁定账号。
     */
    public LoginResponse login(LoginRequest request, String ip) {
        String lockKey = LOCK_KEY_PREFIX + request.getUsername();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BizException(4012, "account temporary locked");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            onLoginFail(request.getUsername(), ip);
            throw new BizException(4011, "username or password invalid");
        }
        List<String> roles = roleMapper.findRoleCodesByUserId(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), roles);
        redisTemplate.delete(FAIL_KEY_PREFIX + request.getUsername());
        writeLoginLog(request.getUsername(), ip, 1, "success");
        return new LoginResponse(token, user.getId(), user.getUsername(), roles);
    }

    @Override
    /**
     * 根据用户 ID 查询用户信息（供内部服务调用）。
     */
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setStatus(user.getStatus());
        dto.setRoles(roleMapper.findRoleCodesByUserId(user.getId()));
        return dto;
    }

    @Override
    /**
     * 查询最近登录日志（管理侧使用）。
     */
    public List<LoginLog> latestLoginLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(200, limit));
        return loginLogMapper.selectList(new LambdaQueryWrapper<LoginLog>()
                .orderByDesc(LoginLog::getCreatedAt)
                .last("limit " + safeLimit));
    }

    /**
     * 登录失败处理：失败计数 + 过期时间 + 阈值锁定 + 失败日志。
     */
    private void onLoginFail(String username, String ip) {
        String failKey = FAIL_KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(failKey);
        redisTemplate.expire(failKey, Duration.ofMinutes(10));
        if (count != null && count >= 5) {
            redisTemplate.opsForValue().set(LOCK_KEY_PREFIX + username, "1", Duration.ofMinutes(5));
        }
        writeLoginLog(username, ip, 0, "fail");
    }

    /**
     * 落库登录日志。
     */
    private void writeLoginLog(String username, String ip, int success, String message) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setSuccess(success);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}
