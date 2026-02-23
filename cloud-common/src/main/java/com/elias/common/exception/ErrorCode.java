package com.elias.common.exception;

/**
 * Unified business error codes for frontend/backend contract.
 */
public enum ErrorCode {
    OK(0, "成功"),

    SERVER_ERROR(5000, "服务器异常"),

    NOT_LOGGED_IN(4010, "未登录"),
    LOGIN_INVALID_CREDENTIALS(4011, "用户名或密码错误"),
    LOGIN_TEMP_LOCKED(4012, "账号已被临时锁定"),

    FORBIDDEN_ADMIN_ROLE_REQUIRED(4030, "需要管理员权限"),

    OWNER_NOT_FOUND(4004, "未找到任务所属用户"),

    TASK_NOT_FOUND(4041, "任务不存在"),

    USERNAME_ALREADY_EXISTS(4091, "用户名已存在"),

    USERID_NULL(-1, "用户id错误"),

    USER_CREATE_NULL(-2, "用户名或密码创建不能为空"),

    ROLE_NOT_FOUND(4042, "默认角色不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
