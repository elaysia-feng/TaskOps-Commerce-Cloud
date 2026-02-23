package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
/**
 * 文件说明： Role.
 * 组件职责： 项目中的通用组件。
 */
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode;
    private String roleName;
}
