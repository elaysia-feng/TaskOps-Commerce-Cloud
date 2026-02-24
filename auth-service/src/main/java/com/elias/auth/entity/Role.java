package com.elias.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("sys_role")
@Schema(name = "Role", description = "角色实体")
public class Role {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "角色编码", example = "USER")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;
}
