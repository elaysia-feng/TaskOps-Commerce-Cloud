package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
/**
 * 文件说明： UserRole.
 * 组件职责： 项目中的通用组件。
 */
public class UserRole {
    private Long userId;
    private Long roleId;
}
