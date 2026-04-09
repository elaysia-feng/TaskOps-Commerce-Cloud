package com.elias.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elias.auth.dto.LoginRequest;
import com.elias.auth.dto.LoginResponse;
import com.elias.auth.dto.RegisterRequest;
import com.elias.auth.dto.UserInfoDTO;
import com.elias.auth.entity.LoginLog;
import com.elias.auth.entity.Role;
import com.elias.auth.entity.User;
import com.elias.auth.entity.UserAuthIdentity;
import com.elias.auth.entity.UserRole;
import com.elias.auth.mapper.LoginLogMapper;
import com.elias.auth.mapper.RoleMapper;
import com.elias.auth.mapper.UserAuthIdentityMapper;
import com.elias.auth.mapper.UserMapper;
import com.elias.auth.mapper.UserRoleMapper;
import com.elias.auth.security.JwtTokenProvider;
import com.elias.auth.service.AuthAppService;
import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAppServiceImpl implements AuthAppService {

    private static final String FAIL_KEY_PREFIX = "auth:fail:";
    private static final String LOCK_KEY_PREFIX = "auth:lock:";
    private static final String DEFAULT_USER_ROLE_CODE = "USER";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String REGISTER_SOURCE_WEB = "WEB";
    private static final String USERNAME_IDENTITY_TYPE = "USERNAME";
    private static final String LOGIN_TYPE_PASSWORD = "USERNAME_PASSWORD";

    private final LoginLogMapper loginLogMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserAuthIdentityMapper userAuthIdentityMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        validateUserId(userId);
        User user = loadUserById(userId);
        return buildUserInfoDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);
        RegisterContext context = prepareRegisterContext(request);
        executeUserRegistration(context);
        executeDefaultRoleBinding(context);
    }

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        validateLoginRequest(request);
        LoginContext context = prepareLoginContext(request, ip);
        validateLoginCredential(context);
        executeLoginSuccess(context);
        return buildLoginResponse(context);
    }

    @Override
    public List<LoginLog> latestLoginLogs(int limit) {
        return queryLatestLoginLogs(normalizeLoginLogLimit(limit));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException(ErrorCode.USERID_NULL);
        }
    }

    private User loadUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    private UserInfoDTO buildUserInfoDTO(User user) {
        if (user == null) {
            return null;
        }

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserId(user.getId());
        userInfoDTO.setUsername(user.getUsername());
        userInfoDTO.setNickname(user.getNickname());
        userInfoDTO.setStatus(ACTIVE_STATUS.equals(user.getStatus()) ? 1 : 0);
        userInfoDTO.setRoles(loadRoleCodes(user.getId()));
        return userInfoDTO;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())
                || !StringUtils.hasText(request.getNickname())) {
            throw new BizException(ErrorCode.USER_CREATE_NULL);
        }

        if (loadUserByUsername(request.getUsername()) != null) {
            throw new BizException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    private RegisterContext prepareRegisterContext(RegisterRequest request) {
        RegisterContext context = new RegisterContext();
        context.setRequest(request);
        context.setDefaultRole(loadDefaultRole());
        return context;
    }

    private Role loadDefaultRole() {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, DEFAULT_USER_ROLE_CODE)
                .eq(Role::getStatus, ACTIVE_STATUS)
                .last("limit 1"));
        if (role == null) {
            throw new BizException(ErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void executeUserRegistration(RegisterContext context) {
        LocalDateTime now = LocalDateTime.now();
        RegisterRequest request = context.getRequest();

        User user = new User();
        user.setUserNo(generateUserNo());
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setStatus(ACTIVE_STATUS);
        user.setRegisterSource(REGISTER_SOURCE_WEB);
        user.setPasswordSet(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        UserAuthIdentity identity = new UserAuthIdentity();
        identity.setUserId(user.getId());
        identity.setIdentityType(USERNAME_IDENTITY_TYPE);
        identity.setPrincipal(user.getUsername());
        identity.setCredentialHash(passwordEncoder.encode(request.getPassword()));
        identity.setCredentialVersion(1);
        identity.setStatus(ACTIVE_STATUS);
        identity.setCreatedAt(now);
        identity.setUpdatedAt(now);
        userAuthIdentityMapper.insert(identity);

        context.setUser(user);
    }

    private void executeDefaultRoleBinding(RegisterContext context) {
        UserRole userRole = new UserRole();
        userRole.setUserId(context.getUser().getId());
        userRole.setRoleId(context.getDefaultRole().getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword())) {
            throw new BizException(ErrorCode.USER_CREATE_NULL);
        }
    }

    private LoginContext prepareLoginContext(LoginRequest request, String ip) {
        String username = request.getUsername();
        ensureLoginNotLocked(username);

        LoginContext context = new LoginContext();
        context.setRequest(request);
        context.setUsername(username);
        context.setIp(resolveLoginIp(ip));
        context.setIdentity(loadIdentityByUsername(username));
        context.setUser(loadUserByIdentity(context.getIdentity()));
        return context;
    }

    private void ensureLoginNotLocked(String username) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildLockKey(username)))) {
            throw new BizException(ErrorCode.LOGIN_TEMP_LOCKED);
        }
    }

    private void validateLoginCredential(LoginContext context) {
        User user = context.getUser();
        UserAuthIdentity identity = context.getIdentity();
        if (user == null
                || identity == null
                || !ACTIVE_STATUS.equals(user.getStatus())
                || !ACTIVE_STATUS.equals(identity.getStatus())
                || !passwordEncoder.matches(context.getRequest().getPassword(), identity.getCredentialHash())) {
            recordLoginFailure(context);
            throw new BizException(ErrorCode.LOGIN_INVALID_CREDENTIALS);
        }
    }

    private void executeLoginSuccess(LoginContext context) {
        User user = context.getUser();
        List<String> roles = loadRoleCodes(user.getId());
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), roles);

        clearLoginFailureState(context.getUsername());
        markLoginSuccess(user, context.getIdentity(), context.getIp());
        saveLoginLog(user.getId(), context.getUsername(), context.getIp(), 1, null);

        context.setRoles(roles);
        context.setToken(token);
    }

    private LoginResponse buildLoginResponse(LoginContext context) {
        return new LoginResponse(
                context.getToken(),
                context.getUser().getId(),
                context.getUser().getUsername(),
                context.getRoles()
        );
    }

    private int normalizeLoginLogLimit(int limit) {
        return Math.max(1, Math.min(200, limit));
    }

    private List<LoginLog> queryLatestLoginLogs(int limit) {
        return loginLogMapper.selectList(new LambdaQueryWrapper<LoginLog>()
                .orderByDesc(LoginLog::getCreatedAt)
                .last("limit " + limit));
    }

    private User loadUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
    }

    private UserAuthIdentity loadIdentityByUsername(String username) {
        return userAuthIdentityMapper.selectOne(new LambdaQueryWrapper<UserAuthIdentity>()
                .eq(UserAuthIdentity::getIdentityType, USERNAME_IDENTITY_TYPE)
                .eq(UserAuthIdentity::getPrincipal, username)
                .last("limit 1"));
    }

    private User loadUserByIdentity(UserAuthIdentity identity) {
        if (identity == null || identity.getUserId() == null) {
            return null;
        }
        return userMapper.selectById(identity.getUserId());
    }

    private List<String> loadRoleCodes(Long userId) {
        return roleMapper.findRoleCodesByUserId(userId);
    }

    private void recordLoginFailure(LoginContext context) {
        String failKey = buildFailKey(context.getUsername());
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        stringRedisTemplate.expire(failKey, Duration.ofMinutes(10));

        if (count != null && count >= 5) {
            stringRedisTemplate.opsForValue().set(buildLockKey(context.getUsername()), "1", Duration.ofMinutes(5));
        }

        saveLoginLog(resolveFailedUserId(context), context.getUsername(), context.getIp(), 0, "login failed");
    }

    private void clearLoginFailureState(String username) {
        stringRedisTemplate.delete(buildFailKey(username));
    }

    private void saveLoginLog(Long userId, String username, String ip, int success, String message) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginType(LOGIN_TYPE_PASSWORD);
        log.setIp(ip);
        log.setSuccess(success);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    private void markLoginSuccess(User user, UserAuthIdentity identity, String ip) {
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setLastLoginIp(ip);
        user.setUpdatedAt(now);
        userMapper.updateById(user);

        identity.setLastVerifiedAt(now);
        identity.setUpdatedAt(now);
        userAuthIdentityMapper.updateById(identity);
    }

    private String resolveLoginIp(String ip) {
        return StringUtils.hasText(ip) ? ip : "unknown";
    }

    private Long resolveFailedUserId(LoginContext context) {
        return context.getUser() == null ? null : context.getUser().getId();
    }

    private String buildFailKey(String username) {
        return FAIL_KEY_PREFIX + username;
    }

    private String buildLockKey(String username) {
        return LOCK_KEY_PREFIX + username;
    }

    private String generateUserNo() {
        return "USR" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Data
    private static class RegisterContext {
        private RegisterRequest request;
        private Role defaultRole;
        private User user;
    }

    @Data
    private static class LoginContext {
        private LoginRequest request;
        private String username;
        private String ip;
        private User user;
        private UserAuthIdentity identity;
        private List<String> roles;
        private String token;
    }
}